package com.micompra.micompraya.controllers;


import com.micompra.micompraya.models.Categoria;
import com.micompra.micompraya.models.EstadoProducto;
import com.micompra.micompraya.models.Marca;
import com.micompra.micompraya.models.Producto;
import com.micompra.micompraya.services.CategoriaService;
import com.micompra.micompraya.services.EstadoProductoService;
import com.micompra.micompraya.services.MarcaService;
import com.micompra.micompraya.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import com.micompra.micompraya.repositories.EstadoProductoRepository;
import com.micompra.micompraya.repositories.MarcaRepository;
import com.micompra.micompraya.repositories.CategoriaRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final EstadoProductoService estadoProductoService;

    @GetMapping("/producto")
    public String productosView(Model model) {
        model.addAttribute("productos", productoService.findAll());
        model.addAttribute("view", "productos/productosView");
        return "layout/layout";
    }

    @GetMapping("/agregarProducto")
    public String agregarProductoView(Model model) {
        model.addAttribute("view", "productos/agregarProductoView");
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", categoriaService.findAll());
        model.addAttribute("marcas", marcaService.findAll());
        model.addAttribute("estados", estadoProductoService.findAll());
        return "layout/layout";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(
            @ModelAttribute Producto producto,
            @RequestParam("imagen") MultipartFile imagen,
            RedirectAttributes redirectAttributes) {
        try {
            // Única llamada al servicio. Toda la lógica de negocio está encapsulada.
            productoService.agregarProductoConImagen(producto, imagen);

            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Producto guardado exitosamente");
        } catch (Exception e) {
            // Si el servicio lanza una excepción, la capturamos aquí.
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el producto: " + e.getMessage());
        }
        return "redirect:/agregarProducto";
    }





}
