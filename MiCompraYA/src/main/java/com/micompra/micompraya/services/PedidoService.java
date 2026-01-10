package com.micompra.micompraya.services;

import com.beust.jcommander.Parameter;
import com.micompra.micompraya.models.*;
import com.micompra.micompraya.repositories.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled; // 1. IMPORTAR
import com.micompra.micompraya.repositories.ProductoRepository;

import java.util.List;

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
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoProductoRepository estadoProductoRepository;
    private final TransaccionService transaccionService;

    // Definimos los IDs de estado (basado en tu código)
    private static final int ESTADO_PENDIENTE = 1;
    private static final int ESTADO_COMPLETADO = 2;
    private static final int ESTADO_CANCELADO = 3;
    private final FacturaPdfService facturaPdfService;

    // Tiempos para pruebas (Advertencia a 30s, Cancelación a 90s)
    private static final int SEGUNDOS_PARA_CANCELAR = 240; // 1 minuto y medio
    private static final int SEGUNDOS_PARA_ADVERTIR_FIN = 120; // Límite superior de la ventana de advertencia (30s de antigüedad)
    private static final int SEGUNDOS_PARA_ADVERTIR_INICIO = SEGUNDOS_PARA_CANCELAR; // Límite inferior (igual a cancelación)

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

    /**
     * Tarea programada para verificar y gestionar pedidos pendientes.
     * Se ejecuta automáticamente cada hora (3,600,000 milisegundos).
     *
     * (fixedRate = 3600000) = 1 hora
     * (Para probar en desarrollo, puedes usar 60000 = 1 minuto)
     */
    // Ejecutamos cada 30 segundos para pruebas
    @Scheduled(fixedRate = 60000) // Ejecutar cada 30 segundos
    @Transactional // Cubre toda la ejecución del método
    public void gestionarVencimientoDePedidos() {

        System.out.println("TAREA PROGRAMADA (CON FLAG): Verificando vencimiento... " + LocalDateTime.now());
        LocalDateTime ahora = LocalDateTime.now();

        // --- 1. CANCELAR PEDIDOS VENCIDOS (>= 90 segundos) ---
        LocalDateTime limiteCancelacion = ahora.minusSeconds(SEGUNDOS_PARA_CANCELAR);
        List<Pedido> pedidosParaCancelar = pedidoRepository.findByEstadoPedido_IdAndFechaPedidoBefore(
                ESTADO_PENDIENTE,
                limiteCancelacion
        );

        if (!pedidosParaCancelar.isEmpty()) {
            EstadoPedido estadoCancelado = estadoPedidoRepository.findById(ESTADO_CANCELADO)
                    .orElseThrow(() -> new RuntimeException("El estado 'Cancelado' (ID " + ESTADO_CANCELADO + ") no está configurado."));

            for (Pedido pedido : pedidosParaCancelar) {
                // Cambiar estado y guardar primero
                pedido.setEstadoPedido(estadoCancelado);
                Pedido pedidoCancelado = pedidoRepository.save(pedido);

                // Intentar enviar correo después
                try {
                    emailService.enviarCorreoCancelacion(pedidoCancelado);
                    System.out.println("Pedido " + pedidoCancelado.getCodigo() + " CANCELADO y correo enviado.");
                } catch (Exception e) {
                    System.err.println("ERROR al enviar correo de cancelación para " + pedidoCancelado.getCodigo() + " (Pedido ya cancelado en BD): " + e.getMessage());
                }
            }
        }

        // --- 2. ADVERTIR SOBRE PEDIDOS PRÓXIMOS A VENCER (entre 30 y < 90 segundos) Y SIN ADVERTENCIA PREVIA ---
        LocalDateTime limiteAdvertenciaInicio = ahora.minusSeconds(SEGUNDOS_PARA_ADVERTIR_INICIO); // Límite inferior (90s)
        LocalDateTime limiteAdvertenciaFin = ahora.minusSeconds(SEGUNDOS_PARA_ADVERTIR_FIN);   // Límite superior (30s)

        // ✅ USA EL MÉTODO DEL REPOSITORIO QUE FILTRA POR advertenciaEnviada = false
        List<Pedido> pedidosParaAdvertir = pedidoRepository.findByEstadoPedido_IdAndFechaPedidoBetweenAndAdvertenciaEnviadaFalse(
                ESTADO_PENDIENTE,
                limiteAdvertenciaInicio,
                limiteAdvertenciaFin
        );

        for (Pedido pedido : pedidosParaAdvertir) {
            try {
                // Intentamos enviar el correo PRIMERO
                emailService.enviarCorreoAdvertenciaVencimiento(pedido);
                System.out.println("Advertencia de vencimiento enviada para pedido " + pedido.getCodigo());

                // ✅ SI el correo se envió con éxito, MARCAMOS el pedido y guardamos
                pedido.setAdvertenciaEnviada(true); // Establece el flag
                pedidoRepository.save(pedido); // Guarda el cambio del flag

            } catch (Exception e) {
                // Si el correo falla, NO marcamos el pedido. Lo intentará de nuevo en la próxima ejecución.
                System.err.println("ERROR al enviar advertencia para " + pedido.getCodigo() + ". Se reintentará. Causa: " + e.getMessage());
            }
        }
    }

    //obtener el codigo del pedido
    @Transactional(readOnly = true) // Solo lectura
    public Pedido obtenerPedidoPendientePorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        Optional<Pedido> pedidoOpt = pedidoRepository.findByCodigo(codigo);

        // Verifica si existe Y si su estado es PENDIENTE (ID 1)
        if (pedidoOpt.isPresent() && pedidoOpt.get().getEstadoPedido().getId() == ESTADO_PENDIENTE) {
            return pedidoOpt.get();
        }
        return null; // No encontrado o no está pendiente
    }

    //Confirmar pedido a entregado
    @Transactional // ¡MUY IMPORTANTE! Si falla el descuento de stock, se revierte el cambio de estado.
    public Pedido marcarPedidoComoCompletado(Integer pedidoId,Usuario cajero) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + pedidoId));


        if (pedido.getEstadoPedido().getId() != ESTADO_PENDIENTE) {
            throw new IllegalStateException("Solo se pueden procesar pedidos que están pendientes. Estado actual: " + pedido.getEstadoPedido().getEstadoPedido());
        }

        // --- LÓGICA: DESCONTAR STOCK ---
        // 3. Obtener los detalles del pedido (Necesario para stock Y para el PDF/Email)
        List<DetallePedido> detalles = detallePedidoRepository.findByPedido_Id(pedidoId);
        if (detalles.isEmpty()) {
            System.err.println("Advertencia: El pedido " + pedido.getCodigo() + " no tiene detalles para descontar stock.");
        }
        String[] correosStaff = null;

        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();
            Integer cantidadVendida = detalle.getCantidad();


            // Refrescar producto desde BD
            Producto productoActual = productoRepository.findById(producto.getId())
                    .orElseThrow(() -> new RuntimeException("Producto con ID " + producto.getId() + " no encontrado en el pedido " + pedido.getCodigo()));

            int stockActual = productoActual.getStock();
            int stockAnterior = productoActual.getStock();
            if (stockActual < cantidadVendida) {
                // La transacción hará rollback aquí
                throw new RuntimeException("Stock insuficiente para '" + productoActual.getNombre() + "' (ID: " + productoActual.getId() + "). Stock: " + stockActual + ", Solicitado: " + cantidadVendida);
            }

            // Actualizar stock
            int nuevoStock = stockActual - cantidadVendida;
            productoActual.setStock(stockActual - cantidadVendida);

            if (nuevoStock == 0) {
                EstadoProducto estadoAgotado = estadoProductoRepository.findById(2)
                        .orElseThrow(() -> new RuntimeException("El estado de producto 'Agotado' (ID 2) no está configurado."));

                productoActual.setEstadoProducto(estadoAgotado);
                System.out.println("Producto '" + productoActual.getNombre() + "' marcado como AGOTADO (Estado 2).");
            }

            productoRepository.save(productoActual);

            // --- 📝 NUEVO: REGISTRAR TRANSACCIÓN (AUDITORÍA) ---
            transaccionService.registrarAuditoriaVenta(
                    productoActual,
                    cantidadVendida,
                    stockAnterior,
                    nuevoStock,
                    cajero,
                    pedido.getCodigo()
            );

            // --- 🔔 LÓGICA DE ALERTA DE STOCK ---
            if (nuevoStock <= 5) { // Si quedan 5 o menos (incluyendo 0)
                if (correosStaff == null) {
                    correosStaff = obtenerCorreosStaff(); // Obtener correos solo si es necesario
                }
                if (correosStaff.length > 0) {
                    // Enviamos la alerta de forma asíncrona o directa
                    emailService.enviarAlertaStock(correosStaff, productoActual, nuevoStock);
                }
            }
            System.out.println("Stock actualizado para '" + productoActual.getNombre() + "': " + stockActual + " -> " + productoActual.getStock());
        }
        // --- FIN DESCONTAR STOCK ---

        // 4. Buscar el estado "Completado"
        EstadoPedido estadoCompletado = estadoPedidoRepository.findById(ESTADO_COMPLETADO)
                .orElseThrow(() -> new RuntimeException("El estado 'Completado' (ID " + ESTADO_COMPLETADO + ") no está configurado."));

        // 5. Actualizar estado del pedido y GUARDARLO
        pedido.setEstadoPedido(estadoCompletado);
        Pedido pedidoGuardado = pedidoRepository.save(pedido); // Guardamos para confirmar la transacción principal

        // --- LÓGICA POST-TRANSACCIÓN (Notificaciones) ---
        // Estas operaciones se intentan DESPUÉS de que el pedido se guardó como completado.
        // Si fallan, no revierten el estado del pedido, solo se loguea el error.

        byte[] facturaPdfBytes = null;
        try {
            // 6. Generar el PDF
            facturaPdfBytes = facturaPdfService.generarFacturaPdf(pedidoGuardado, detalles);
            System.out.println("PDF generado para pedido " + pedidoGuardado.getCodigo());
        } catch (Exception e) {
            // Si falla la generación del PDF, lo registramos pero continuamos para enviar el email sin adjunto.
            System.err.println("ERROR al generar PDF para pedido " + pedidoGuardado.getCodigo() + ": " + e.getMessage());
            e.printStackTrace(); // Imprime el stack trace completo para depuración
        }

        try {
            // 7. Enviar el correo de agradecimiento (con o sin PDF)
            emailService.enviarCorreoPedidoCompletado(pedidoGuardado, detalles, facturaPdfBytes);
            System.out.println("Correo de pedido completado enviado para " + pedidoGuardado.getCodigo());
        } catch (Exception e) {
            // Si falla el envío del correo, solo registramos el error. El pedido ya está completado.
            System.err.println("ERROR al enviar correo de pedido completado para " + pedidoGuardado.getCodigo() + ": " + e.getMessage());
            e.printStackTrace();
        }

        // 8. Devolver el pedido actualizado
        return pedidoGuardado;
    }

    private String[] obtenerCorreosStaff() {
        // Buscar Admins (Rol 1) y Supervisores (Rol 4)
        // Usamos buscarPorRolSql o findByRol si tienes la entidad Rol.
        // Asumiendo que tienes buscarPorRolSql que devuelve lista de usuarios por ID de rol:
        List<Usuario> admins = usuarioRepository.buscarPorRolSql(1);
        List<Usuario> supervisores = usuarioRepository.buscarPorRolSql(4);

        List<String> correos = new ArrayList<>();
        admins.forEach(u -> correos.add(u.getCorreo()));
        supervisores.forEach(u -> correos.add(u.getCorreo()));

        return correos.toArray(new String[0]);
    }


}