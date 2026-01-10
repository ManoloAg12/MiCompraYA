package com.micompra.micompraya.controllers;


import com.micompra.micompraya.models.*;
import com.micompra.micompraya.services.CategoriaService;
import com.micompra.micompraya.services.EstadoProductoService;
import com.micompra.micompraya.services.MarcaService;
import com.micompra.micompraya.services.ProductoService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import com.micompra.micompraya.repositories.EstadoProductoRepository;
import com.micompra.micompraya.repositories.MarcaRepository;
import com.micompra.micompraya.repositories.CategoriaRepository;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
class ProductoController {

    private final ProductoService productoService;
    private final CategoriaService categoriaService;
    private final MarcaService marcaService;
    private final EstadoProductoService estadoProductoService;

    @GetMapping("/producto")
    public String productosView(Model model,
                                @RequestParam(required = false) Integer categoriaId,
                                @RequestParam(required = false) String busqueda,
                                @RequestParam(required = false, defaultValue = "nombre-asc") String orden) {

        // 1. Obtenemos TODOS los datos necesarios desde los servicios
        List<Producto> todosLosProductos = productoService.findAll();
        List<Categoria> categorias = categoriaService.findAll(); //

        // 2. Calculamos los conteos (lógica simple, permitida en el controlador)
        long conteoTotal = todosLosProductos.size();
        Map<Integer, Long> conteoPorCategoria = todosLosProductos.stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().getId(), Collectors.counting()));

        // 3. Delegamos al SERVICIO la lógica de filtrar y ordenar
        List<Producto> productosFiltrados = productoService.filtrarYOrdenar(todosLosProductos, categoriaId, busqueda, orden);

        // 4. Enviamos todos los datos a la VISTA
        model.addAttribute("productos", productosFiltrados);
        model.addAttribute("categorias", categorias);
        model.addAttribute("conteoTotal", conteoTotal);
        model.addAttribute("conteoCategorias", conteoPorCategoria);

        // 5. (Opcional) Enviamos los parámetros actuales de vuelta a la vista para que los recuerde
        model.addAttribute("paramCategoriaId", categoriaId);
        model.addAttribute("paramBusqueda", busqueda);
        model.addAttribute("paramOrden", orden);


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
            RedirectAttributes redirectAttributes, HttpSession session) {

        Usuario usuarioCreador = (Usuario) session.getAttribute("usuario");

        try {
            // Única llamada al servicio. Toda la lógica de negocio está encapsulada.
            productoService.agregarProductoConImagen(producto, imagen, usuarioCreador);

            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Producto guardado exitosamente");
        } catch (Exception e) {
            // Si el servicio lanza una excepción, la capturamos aquí.
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el producto: " + e.getMessage());
        }
        return "redirect:/agregarProducto";
    }


    @GetMapping("/productos/detalles/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerDetallesProducto(@PathVariable Integer id) {
        Producto p = productoService.obtenerPorId(id);
        if (p != null) {
            Map<String, Object> detalles = Map.of(
                    "nombreProducto", p.getNombre(),
                    "descripcion", p.getDescripcion(), // Nuevo
                    "caducidad", p.getCaducidad() != null ? p.getCaducidad().toString() : "", // Nuevo
                    "urlImagen", p.getUrlImagen(), // Nuevo
                    "stockActual", p.getStock(),
                    "estadoIdActual", p.getEstadoProducto().getId(),
                    "marcaId", p.getMarca().getId(),
                    "categoriaId", p.getCategoria().getId()
            );
            return ResponseEntity.ok(detalles);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/productos/editar-info")
    public String editarInformacionProducto(
            @RequestParam Integer productoId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam(required = false) LocalDate caducidad,
            @RequestParam(required = false) MultipartFile imagen,
            @RequestParam(required = false, defaultValue = "false") boolean descontinuar,
            @RequestParam Integer marcaId,
            @RequestParam Integer categoriaId,
            RedirectAttributes redirectAttributes) {
        try {
            productoService.editarInformacionProducto(productoId, nombre, descripcion, caducidad, imagen, descontinuar, marcaId, categoriaId);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Información del producto actualizada.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
        }
        return "redirect:/agregarProducto";
    }

    @PostMapping("/productos/actualizar-stock-estado")
    public String actualizarStockEstado(
            @RequestParam("productoId") Integer productoId,
            @RequestParam("estadoProductoId") Integer estadoProductoId,
            @RequestParam(value = "cantidadAgregar", required = false, defaultValue = "0") Integer cantidadAgregar, // Valor por defecto 0
            RedirectAttributes redirectAttributes) {

        try {
            // Delegamos toda la lógica al servicio
            productoService.actualizarStockYEstado(productoId, estadoProductoId, cantidadAgregar);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al actualizar: " + e.getMessage());
        }
        // Redirige de vuelta a la página de agregar producto
        return "redirect:/agregarProducto";
    }


    @PostMapping("/marcas/guardar")
    public String guardarMarcaRapida(Marca marca, RedirectAttributes redirectAttributes) {
        try {
            marcaService.guardar(marca);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Marca agregada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar marca: " + e.getMessage());
        }
        return "redirect:/agregarProducto"; // Volvemos al formulario
    }

    @PostMapping("/categorias/guardar")
    public String guardarCategoriaRapida(Categoria categoria, RedirectAttributes redirectAttributes) {
        try {
            categoriaService.guardar(categoria);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Categoría agregada correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar categoría: " + e.getMessage());
        }
        return "redirect:/agregarProducto";
    }

    @PostMapping("/estados/guardar")
    public String guardarEstadoRapido(EstadoProducto estadoProducto, RedirectAttributes redirectAttributes) {
        try {
            estadoProductoService.guardar(estadoProducto);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Estado agregado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar estado: " + e.getMessage());
        }
        return "redirect:/agregarProducto";
    }





}
