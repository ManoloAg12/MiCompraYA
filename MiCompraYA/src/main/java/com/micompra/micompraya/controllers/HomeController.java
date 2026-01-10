package com.micompra.micompraya.controllers;

import com.micompra.micompraya.models.Producto;
import com.micompra.micompraya.services.CarritoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.micompra.micompraya.services.ProductoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {

    // ✅ Solo necesitamos el servicio del carrito aquí para las operaciones del carrito
    private final CarritoService carritoService;
    private final ProductoService productoService;

    // --- MÉTODOS DE NAVEGACIÓN (SIN CAMBIOS) ---
    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Obtenemos los 4 productos con más stock desde el servicio
        List<Producto> productosDestacados = productoService.obtenerProductosConMasStock(4);
        model.addAttribute("productosDestacados", productosDestacados);
        model.addAttribute("view", "home/home_view");
        return "layout/layout";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("view", "login/login_view");
        return "login/login_view";
    }

    @GetMapping("/acercaDe")
    public String acercaDe(Model model) {
        model.addAttribute("view", "home/acercaDe_view");
        return "layout/layout";
    }

    @GetMapping("/accesoNegado")
    public String accesoNegado(Model model) {
        // 1. Añade el nombre del fragmento al modelo
        model.addAttribute("view", "home/accesoNegado_view");

        // 2. Devuelve la plantilla principal (layout)
        return "layout/layout";
    }

    // --- MÉTODOS DEL CARRITO (AHORA SIMPLIFICADOS) ---

    @GetMapping("/carrito")
    public String carrito(HttpSession session, Model model) {
        // ✨ La lógica de obtener/crear el carrito está en el servicio
        model.addAttribute("carrito", carritoService.getProductosEnCarrito(session));
        model.addAttribute("cantidades", carritoService.getCantidades(session));
        model.addAttribute("view", "carrito/carrito_view");
        return "layout/layout";
    }

    @PostMapping("/agregarCarrito/{id}")
    @ResponseBody
    public String agregarCarrito(@PathVariable Integer id, HttpSession session) {
        // ✨ Simplemente llamamos al servicio
        carritoService.agregarAlCarrito(id, session);
        return "ok";
    }

    @PostMapping("/eliminarDelCarrito/{id}")
    @ResponseBody
    public String eliminarDelCarrito(@PathVariable Integer id, HttpSession session) {
        // ✨ Simplemente llamamos al servicio
        carritoService.eliminarDelCarrito(id, session);
        return "ok";
    }

    @GetMapping("/carrito/cantidad")
    @ResponseBody
    public Map<String, Integer> cantidadCarrito(HttpSession session) {
        // ✨ Simplemente llamamos al servicio
        int total = carritoService.getTotalItems(session);
        Map<String, Integer> response = new HashMap<>();
        response.put("total", total);
        return response;
    }

    @PostMapping("/actualizarCantidad/{id}/{cantidad}")
    @ResponseBody
    public String actualizarCantidad(@PathVariable Integer id,
                                     @PathVariable Integer cantidad,
                                     HttpSession session) {
        // ✨ Simplemente llamamos al servicio
        carritoService.actualizarCantidad(id, cantidad, session);
        return "ok";
    }


}