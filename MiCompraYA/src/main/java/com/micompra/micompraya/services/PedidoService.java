package com.micompra.micompraya.services;

import com.beust.jcommander.Parameter;
import com.micompra.micompraya.models.*;
import com.micompra.micompraya.repositories.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;
import com.micompra.micompraya.services.QrCodeService;

@Service
@RequiredArgsConstructor
public class PedidoService {

    // Repositorios necesarios para las operaciones
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ClienteRepository clienteRepository;
    private final EstadoPedidoRepository estadoPedidoRepository;
    private final TipoPagoRepository tipoPagoRepository;
    private final QrCodeService qrCodeService;

    // Servicios de apoyo
    private final CarritoService carritoService;
    private final EmailService emailService;

    @Transactional // ¡Crucial! Si algo falla (ej. al guardar un detalle), toda la operación se revierte.
    public Pedido procesarPedido(HttpSession session, Integer tipoPagoId, Usuario usuarioLogueado) {

        // 1. Buscar el perfil de Cliente asociado al Usuario logueado.
        Cliente cliente = clienteRepository.findByUsuario_Id(usuarioLogueado.getId())
                .orElseThrow(() -> new RuntimeException("No se encontró un perfil de cliente para este usuario."));

        List<Producto> productosEnCarrito = carritoService.getProductosEnCarrito(session);
        Map<Integer, Integer> cantidades = carritoService.getCantidades(session);

        if (productosEnCarrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío, no se puede procesar el pedido.");
        }

        // --- LÓGICA PASO A PASO ---

        // 2. Crear y guardar el Pedido principal PRIMERO (con total temporal)
        Pedido nuevoPedido = new Pedido();
        nuevoPedido.setCliente(cliente);
        nuevoPedido.setCodigo(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        // Asignar estado y tipo de pago buscando sus entidades por ID
        EstadoPedido estadoPendiente = estadoPedidoRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("El estado de pedido 'Pendiente' (ID 1) no está configurado."));
        TipoPago tipoPago = tipoPagoRepository.findById(tipoPagoId)
                .orElseThrow(() -> new RuntimeException("El tipo de pago con ID " + tipoPagoId + " no está configurado."));

        nuevoPedido.setEstadoPedido(estadoPendiente);
        nuevoPedido.setTipoPago(tipoPago);
        nuevoPedido.setTotal(BigDecimal.ZERO); // Total temporal
        //seteamos la fecha
        nuevoPedido.setFechaPedido(LocalDateTime.now());
        // Guardamos el pedido para que la base de datos le asigne un ID
        Pedido pedidoGuardado = pedidoRepository.save(nuevoPedido);

        // 3. Crear y guardar cada Detalle del Pedido, uno por uno
        BigDecimal totalGeneral = BigDecimal.ZERO;
        List<DetallePedido> detallesGuardados = new ArrayList<>();
        for (Producto producto : productosEnCarrito) {
            int cantidad = cantidades.get(producto.getId());

            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedidoGuardado); // Se asigna el pedido que ya tiene un ID
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());

            BigDecimal subtotal = producto.getPrecio().multiply(new BigDecimal(cantidad));
            detalle.setSubtotal(subtotal);

            detallesGuardados.add(detallePedidoRepository.save(detalle)); // Guardamos el detalle

            totalGeneral = totalGeneral.add(subtotal); // Sumamos al total general
        }

        // 4. Actualizar el Pedido con el total final correcto
        pedidoGuardado.setTotal(totalGeneral);
        pedidoRepository.save(pedidoGuardado);

        // 5. Limpiar el carrito y enviar correo de confirmación
        session.removeAttribute("carrito");
        session.removeAttribute("cantidades");

        try {
            // Se le pasa el pedido ya actualizado con el total y los detalles
            emailService.enviarCorreoConfirmacion(pedidoGuardado, detallesGuardados);
        } catch (Exception e) {
            // Si el correo falla, no revertimos la compra, solo lo registramos.
            System.err.println("¡ATENCIÓN! Pedido " + pedidoGuardado.getCodigo() + " guardado, pero falló el envío del correo: " + e.getMessage());
        }

        return pedidoGuardado;
    }

    public List<Pedido> obtenerPedidosPorCliente(Cliente cliente) {
        // Si el cliente no existe, devuelve una lista vacía para evitar errores.
        if (cliente == null) {
            return Collections.emptyList();
        }
        // Llama al nuevo método que crearemos en el repositorio.
        return pedidoRepository.findByClienteOrderByFechaPedidoDesc(cliente);
    }

    public List<DetallePedido> obtenerDetallesPorPedidoId(Integer pedidoId) {
        // Primero, verificamos que el pedido exista para dar un error claro.
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new RuntimeException("Pedido no encontrado con el ID: " + pedidoId);
        }
        // Llama al método del repositorio para encontrar todos los detalles asociados a ese ID de pedido.
        return detallePedidoRepository.findByPedido_Id(pedidoId);
    }

    @Transactional
    public Pedido cancelarPedido(Integer pedidoId) {
        // 1. Buscar el pedido por su ID.
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con el ID: " + pedidoId));

        // 2. Regla de negocio: Solo se pueden cancelar pedidos pendientes (ID 1).
        if (pedido.getEstadoPedido().getId() != 1) {
            throw new IllegalStateException("Este pedido ya no puede ser cancelado. Su estado es: " + pedido.getEstadoPedido().getEstadoPedido());
        }

        // 3. Buscar el estado "Cancelado" (ID 3).
        EstadoPedido estadoCancelado = estadoPedidoRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("El estado 'Cancelado' (ID 3) no está configurado en el sistema."));

        // 4. Actualizar el estado del pedido.
        pedido.setEstadoPedido(estadoCancelado);

        // 5. Guardar los cambios en la base de datos.
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedidoPorCodigo(String codigo) {
        return pedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con el código: " + codigo));
    }

    public String generarQrBase64(String texto, int ancho, int alto) {
        byte[] qrCodeBytes = qrCodeService.generarQrCode(texto, ancho, alto);
        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }
}