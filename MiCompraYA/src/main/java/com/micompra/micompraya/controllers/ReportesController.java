package com.micompra.micompraya.controllers;

import com.micompra.micompraya.services.ReportesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReportesController {

    private final ReportesService reportesService;

    // 1. Muestra la vista HTML
    @GetMapping
    public String verReportes(Model model) {
        model.addAttribute("view", "transacciones/reportes_view");
        return "layout/layout";
    }

    // 2. Endpoint API para que el JavaScript obtenga los datos
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerDatosGraficos() {
        return ResponseEntity.ok(reportesService.obtenerDatosDashboard());
    }
}