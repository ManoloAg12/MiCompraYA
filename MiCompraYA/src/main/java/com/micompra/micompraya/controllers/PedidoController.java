package com.micompra.micompraya.controllers;

import com.micompra.micompraya.models.Cliente;
import com.micompra.micompraya.models.DetallePedido;
import com.micompra.micompraya.models.Pedido;
import com.micompra.micompraya.models.Usuario;
import com.micompra.micompraya.repositories.DetallePedidoRepository;
import com.micompra.micompraya.repositories.PedidoRepository;
import com.micompra.micompraya.services.PedidoService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.micompra.micompraya.services.QrCodeService;
import com.micompra.micompraya.services.ClienteService;

import java.util.*;


@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final QrCodeService qrCodeService;
    private final ClienteService clienteService;

    // Clase interna para recibir los datos del JSON de forma limpia.
    @Data
    static class PedidoRequest {
        private Integer tipoPagoId;
    }

    @PostMapping("/finalizar")
    @ResponseBody
    public ResponseEntity<?> finalizarCompra(@RequestBody PedidoRequest request, HttpSession session) {

        // 1. Recuperar el usuario que guardaste en la sesión al hacer login
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");

        // 2. Validar que el usuario exista en la sesión (que haya iniciado sesión)
        if (usuarioLogueado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Tu sesión ha expirado. Por favor, inicia sesión de nuevo."));
        }

        try {
            // 3. Llamar al servicio para que haga todo el trabajo pesado
            Pedido nuevoPedido = pedidoService.procesarPedido(session, request.getTipoPagoId(), usuarioLogueado);

            // 4. Si todo sale bien, devolver una respuesta exitosa
            return ResponseEntity.ok(Map.of("success", true, "codigoPedido", nuevoPedido.getCodigo()));

        } catch (IllegalStateException e) { // Captura el error si el carrito está vacío
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (RuntimeException e) { // Captura errores como "cliente no encontrado" o "estado no encontrado"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) { // Captura cualquier otro error inesperado
            e.printStackTrace(); // Esencial para ver el error completo en la consola del servidor
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Ocurrió un error inesperado en el servidor."));
        }
    }

    // Usamos una URL limpia y descriptiva
    @GetMapping("/pedido/exito")
    public String mostrarPaginaExito(@RequestParam("codigo") String codigoPedido, Model model) {

        // 1. Buscar el pedido (Lógica en el servicio)
        Pedido pedido = pedidoService.obtenerPedidoPorCodigo(codigoPedido);

        // 2. Buscar los detalles (Lógica en el servicio)
        // (Reutilizamos el método que ya tenías en tu servicio)
        List<DetallePedido> detalles = pedidoService.obtenerDetallesPorPedidoId(pedido.getId());

        // 3. Generar el QR (Lógica en el servicio)
        String qrCodeBase64 = pedidoService.generarQrBase64(pedido.getCodigo(), 200, 200);

        // 4. Agregar todo al modelo para la vista
        model.addAttribute("pedido", pedido);
        model.addAttribute("detalles", detalles);
        model.addAttribute("qrCodeBase64", qrCodeBase64);

        // 5. Definir la vista
        model.addAttribute("view", "pedido/exito_view");
        return "layout/layout";
    }

    @GetMapping("/pedido/pedidos_view")
    public String verMisPedidos(HttpSession session, Model model) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null) {
            return "redirect:/login";
        }

        // ✅ 3. Llama al ClienteService para obtener el cliente. ¡Mucho más limpio!
        Cliente cliente = clienteService.obtenerClientePorUsuario(usuarioLogueado);

        List<Pedido> pedidos;
        if (cliente != null) {
            // 4. Si se encontró el cliente, busca sus pedidos.
            pedidos = pedidoService.obtenerPedidosPorCliente(cliente);
        } else {
            // Si no hay cliente, envía una lista vacía para evitar errores.
            pedidos = Collections.emptyList();
        }

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("view", "pedido/pedidos_view");
        return "layout/layout";
    }

    @GetMapping("/pedidos/detalles/{id}")
    @ResponseBody // Asegura que este método devuelva JSON
    public ResponseEntity<?> obtenerDetallesPedido(@PathVariable("id") Integer id) {
        try {
            // 1. El controlador solo llama al servicio. No contiene lógica de búsqueda.
            List<DetallePedido> detalles = pedidoService.obtenerDetallesPorPedidoId(id);

            // 2. Devuelve la lista de detalles directamente. Spring la convertirá a JSON.
            return ResponseEntity.ok(detalles);
        } catch (RuntimeException e) {
            // Si el servicio lanza una excepción (ej. pedido no encontrado), la capturamos.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PostMapping("/pedidos/cancelar/{id}") // Usamos POST porque modifica datos
    @ResponseBody
    public ResponseEntity<?> cancelarPedido(@PathVariable("id") Integer id) {
        try {
            pedidoService.cancelarPedido(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Pedido cancelado correctamente."));
        } catch (RuntimeException e) {
            // Captura errores como "Pedido no encontrado" o "Ya no se puede cancelar"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", e.getMessage()));
        }
    }





}