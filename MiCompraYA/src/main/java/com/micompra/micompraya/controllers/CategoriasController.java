package com.micompra.micompraya.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
class CategoriasController {

    @GetMapping("/categorias")
    public String categorias(Model model) {
        model.addAttribute("view", "categorias/categorias_view");
        return "layout/layout";
    }
}
