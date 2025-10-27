package com.micompra.micompraya.controllers;

import com.micompra.micompraya.models.Cliente;
import com.micompra.micompraya.models.Estado;
import com.micompra.micompraya.models.Rol;
import com.micompra.micompraya.models.Usuario;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.micompra.micompraya.services.UsuarioService;
import com.micompra.micompraya.services.RolService;
import com.micompra.micompraya.services.EstadoService;
import com.micompra.micompraya.services.ClienteService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/")
@RequiredArgsConstructor
class UsuarioController {

    private final UsuarioService usuarioService;
    private final RolService rolService;
    private final EstadoService estadoService;
    private final ClienteService clienteService;

    @GetMapping("/usuarios")
    public String listUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarUsuariosActivos());
        model.addAttribute("roles", rolService.listarRoles());
        model.addAttribute("estados", estadoService.listarEstados());
        model.addAttribute("nuevoUsuario", new Usuario());
        model.addAttribute("usuarioEditar", new Usuario());
        model.addAttribute("view", "usuarios/usuario_view");
        return "layout/layout";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        // --- LÓGICA ELIMINADA ---
        // El servicio se encarga de:
        // 1. Setear fecha de registro
        // 2. Cargar Rol y Estado completos
        // 3. Generar contraseña, enviar email y guardar.
        usuarioService.agregarUsuario(usuario);

        return "redirect:/usuarios";
    }

    //para el js para traer en json el usuari
    @GetMapping("/usuarios/{id}")
    @ResponseBody
    public Usuario obtenerUsuario(@PathVariable Integer id) {
        return usuarioService.obtenerUsuarioPorId(id);
    }

    @PostMapping("/usuarios/editar")
    @ResponseBody
    public Map<String, Object> editarUsuario(@RequestBody Usuario usuario) {
        // --- LÓGICA ELIMINADA ---
        // El servicio se encarga de cargar Rol y Estado
        // y de actualizar la entidad.
        usuarioService.actualizarUsuario(usuario, usuario.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    //login
    @PostMapping("/login")
    public String login(@RequestParam String usuario, @RequestParam String contrasena, RedirectAttributes redirectAttributes, HttpSession session) {
        // Esta lógica está bien aquí, es manejo de sesión y redirección.
        Usuario user = usuarioService.autenticarUsuario(usuario, contrasena);
        if (user != null) {
            session.setAttribute("usuario", user);
            return "redirect:/home";
        } else {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error, contraseña o usuario incorrectos");
            return "redirect:/login";
        }
    }

    //logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @PostMapping("/cambiarContrasena")
    public String cambiarContrasena(@RequestParam String confirmar, @RequestParam Integer idUsuario, RedirectAttributes redirectAttributes) {
        // Esta lógica de try/catch está bien aquí para manejar la respuesta al usuario.
        try {
            usuarioService.cambiarContrasena(confirmar, idUsuario);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña cambiada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar la contraseña");
        }
        return "redirect:/";
    }

    //Registrar usuario y cliente
    @PostMapping("/registrar")
    public String registrarUsuarioCliente(
            @RequestParam String nombreCompleto,
            @RequestParam String nombreUsuario,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            RedirectAttributes redirectAttributes) {

        try {
            // --- LÓGICA DE NEGOCIO MOVIDA ---
            // Simplemente llamamos al nuevo método del servicio
            usuarioService.registrarNuevoCliente(nombreCompleto, nombreUsuario, correo, telefono, direccion);

            // 3️⃣ Mensaje de éxito
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Cuenta creada correctamente");
            return "redirect:/login";

        } catch (Exception e) {
            e.printStackTrace(); // Es bueno loguear el error
            redirectAttributes.addFlashAttribute("tipo", "error");
            // Pasamos el mensaje de la excepción (ej: "Rol no encontrado", "Email ya existe")
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear la cuenta: " + e.getMessage());
            return "redirect:/registro";
        }
    }

    //Eliminar el usuario (ponerlo inactivo)
    @PostMapping("/usuarios/inactivar/{id}")
    @ResponseBody
    public Map<String, Object> inactivarUsuario(@PathVariable Integer id) {
        // Esta lógica try/catch para respuesta JSON también está bien en el controlador.
        Map<String, Object> response = new HashMap<>();
        try {
            usuarioService.inactivarUsuario(id);
            response.put("success", true);
            response.put("message", "Usuario inactivado correctamente");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error al inactivar el usuario: " + e.getMessage());
        }
        return response;
    }
}