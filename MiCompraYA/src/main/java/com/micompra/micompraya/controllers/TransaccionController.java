package com.micompra.micompraya.controllers;

import com.micompra.micompraya.models.Usuario;
import com.micompra.micompraya.services.CategoriaService;
import com.micompra.micompraya.services.ProductoService;
import com.micompra.micompraya.services.TransaccionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;

@Controller
@RequestMapping("/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final ProductoService productoService;
    private final CategoriaService categoriaService;

    // Vista Principal: Historial
    @GetMapping
    public String verHistorial(Model model) {
        model.addAttribute("transacciones", transaccionService.listarTodas());

        // Datos para el modal de registro
        model.addAttribute("productos", productoService.listarProductosActivos());
        model.addAttribute("categorias", categoriaService.findAll());
        model.addAttribute("tiposMovimiento", transaccionService.listarTiposMovimiento());

        model.addAttribute("view", "transacciones/transacciones_view");
        return "layout/layout";
    }

    // Procesar Movimiento Manual
    @PostMapping("/guardar")
    public String guardarMovimiento(
            @RequestParam Integer productoId,
            @RequestParam Integer tipoMovimientoId,
            @RequestParam Integer cantidad,
            @RequestParam String descripcion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null) return "redirect:/login";

        try {
            transaccionService.registrarMovimiento(productoId, tipoMovimientoId, cantidad, descripcion, usuarioLogueado);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Movimiento registrado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
        }

        return "redirect:/transacciones";
    }

    @PostMapping("/reporte/pdf")
    public ResponseEntity<byte[]> descargarReportePdf(
            @RequestParam LocalDate fechaInicio,
            @RequestParam LocalDate fechaFin) {

        byte[] pdfBytes = transaccionService.generarReportePdf(fechaInicio, fechaFin);

        String nombreArchivo = "Reporte_Kardex_" + LocalDate.now() + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + nombreArchivo)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}