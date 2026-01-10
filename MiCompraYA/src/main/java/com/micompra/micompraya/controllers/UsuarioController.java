package com.micompra.micompraya.controllers;

import com.micompra.micompraya.models.Cliente;
import com.micompra.micompraya.models.Estado;
import com.micompra.micompraya.models.Rol;
import com.micompra.micompraya.models.Usuario;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
class   UsuarioController {

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
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.agregarUsuario(usuario);
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado correctamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el usuario");
            return "redirect:/usuarios";
        }
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
            return "redirect:/login";
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


    @GetMapping("/recuperar-contrasena")
    public String mostrarFormularioRecuperar() {
        return "login/recuperar_contrasena_view"; // Devuelve el nombre de la nueva vista HTML
    }

    @PostMapping("/verificar-correo")
    @ResponseBody // Indica que la respuesta será JSON directamente
    public Map<String, Boolean> verificarCorreoExistente(@RequestBody Map<String, String> payload) {
        String correo = payload.get("correo");
        Map<String, Boolean> response = new HashMap<>();

        // Llamamos al servicio para la lógica de negocio (solo verificar)
        boolean existe = usuarioService.existeCorreo(correo);
        response.put("existe", existe);

        return response;
    }

    @PostMapping("/recuperar-contrasena/enviar")
    public String procesarRecuperacionContrasena(@RequestParam String correo,
                                                 RedirectAttributes redirectAttributes,
                                                 Model model) { // Añadimos Model para errores
        try {
            // Llama al nuevo método del servicio
            usuarioService.recuperarContrasenaPorCorreo(correo);

            // Si tiene éxito, redirige al login con mensaje de éxito
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Se ha enviado una nueva contraseña a tu correo.");
            return "redirect:/login";

        } catch (IllegalArgumentException e) {
            // Si el correo no existe (lanzado por el servicio)
            model.addAttribute("error", e.getMessage()); // Añade el mensaje de error al modelo
            model.addAttribute("correo", correo); // Devuelve el correo ingresado a la vista
            return "login/recuperar_contrasena_view"; // Vuelve a mostrar el formulario de recuperación

        } catch (Exception e) {
            // Para otros errores (ej. fallo al enviar email)
            e.printStackTrace(); // Loguea el error completo
            model.addAttribute("error", "Ocurrió un error inesperado al procesar tu solicitud.");
            model.addAttribute("correo", correo);
            return "login/recuperar_contrasena_view";
        }
    }

    @PostMapping("/login/firebase")
    @ResponseBody
    public ResponseEntity<?> loginFirebase(@RequestBody Map<String, String> body, HttpSession session) {
        try {
            String idToken = body.get("token");

            // 1. Delegamos TODA la lógica de negocio al servicio
            Map<String, Object> resultado = usuarioService.procesarLoginGoogle(idToken);

            String status = (String) resultado.get("status");

            // 2. El Controlador gestiona la sesión basándose en la respuesta del servicio
            if ("success".equals(status)) {
                Usuario usuario = (Usuario) resultado.get("usuarioEncontrado");
                session.setAttribute("usuario", usuario); // Gestión de sesión

                // Limpiamos el objeto usuario de la respuesta JSON por seguridad
                resultado.remove("usuarioEncontrado");

            } else if ("new_user".equals(status)) {
                // Gestión de sesión temporal para el registro
                session.setAttribute("temp_google_email", resultado.get("googleEmail"));
                session.setAttribute("temp_google_nombre", resultado.get("googleNombre"));
            }

            // 3. Devolvemos la respuesta limpia al frontend
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of(
                    "status", "error",
                    "message", "Error de autenticación: " + e.getMessage()
            ));
        }
    }


    @GetMapping("/completar-registro") // Usaremos esta URL limpia
    public String mostrarCompletarRegistro(HttpSession session, Model model) {
        // Verificamos que tengamos los datos temporales de Google en sesión
        if (session.getAttribute("temp_google_email") == null) {
            return "redirect:/login"; // Si no hay datos, no debería estar aquí
        }

        // Renderizamos la vista que creaste
        // Asegúrate que el archivo se llame 'completar_registro_view.html' en la carpeta 'login'
        return "login/completar_registro_view";
    }

    @PostMapping("/registrar-google")
    public String registrarUsuarioGoogle(
            @RequestParam String telefono,
            @RequestParam String direccion,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 1. Recuperar los datos de Google guardados en sesión
        String email = (String) session.getAttribute("temp_google_email");
        String nombreGoogle = (String) session.getAttribute("temp_google_nombre");

        if (email == null || nombreGoogle == null) {
            return "redirect:/login";
        }

        try {
            // 2. Generar datos automáticos
            // Usamos el correo como base para el nombre de usuario
            String nombreUsuarioAuto = email.split("@")[0];
            // Si quieres asegurar unicidad, podrías agregarle números random:
            // String nombreUsuarioAuto = email.split("@")[0] + "_" + new Random().nextInt(1000);

            // 3. Llamar al servicio (reutilizamos el método existente)
            // Nota: El servicio 'registrarNuevoCliente' ya genera una contraseña y envía correo.
            // Si quieres evitar enviar el correo con la contraseña (ya que usan Google),
            // deberíamos crear un método específico en el servicio, pero para mantenerlo simple
            // podemos usar el que ya tienes. El usuario recibirá una contraseña que puede ignorar.

            usuarioService.registrarNuevoCliente(
                    nombreGoogle,       // Nombre Completo (viene de Google)
                    nombreUsuarioAuto,  // Nombre Usuario (generado)
                    email,              // Correo (viene de Google)
                    telefono,           // Del formulario
                    direccion           // Del formulario
            );

            // 4. Limpiar sesión temporal y loguear al usuario real
            session.removeAttribute("temp_google_email");
            session.removeAttribute("temp_google_nombre");

            // Buscamos el usuario recién creado para meterlo en sesión
            Usuario usuarioCreado = usuarioService.buscarUsuarioPorEmail(email).get();
            session.setAttribute("usuario", usuarioCreado);

            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "¡Registro completado con éxito!");

            return "redirect:/home";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al finalizar registro: " + e.getMessage());
            return "redirect:/completar-registro";
        }
    }


    // Endpoint exclusivo para que el ADMIN cree Clientes desde el panel
    @PostMapping("/usuarios/guardarCliente")
    public String guardarClienteDesdeAdmin(
            @RequestParam String nombreCompleto,
            @RequestParam String nombreUsuario,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String direccion,
            RedirectAttributes redirectAttributes) {

        try {
            // Reutilizamos tu servicio existente que ya hace todo (crear usuario + crear cliente)
            usuarioService.registrarNuevoCliente(nombreCompleto, nombreUsuario, correo, telefono, direccion);

            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Cliente registrado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al registrar cliente: " + e.getMessage());
        }
        return "redirect:/usuarios";
    }


    // Obtener datos del perfil (Usuario + Cliente)
    @GetMapping("/perfil/datos")
    @ResponseBody
    public ResponseEntity<?> obtenerDatosPerfil(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) return ResponseEntity.status(401).build();

        try {
            // Delegamos al servicio la obtención de los datos combinados
            Map<String, Object> datos = usuarioService.obtenerDatosPerfilCompleto(usuario.getId());
            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Guardar cambios del perfil
    @PostMapping("/perfil/actualizar")
    @ResponseBody
    public ResponseEntity<?> actualizarPerfil(@RequestBody Map<String, String> datos, HttpSession session) {
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) return ResponseEntity.status(401).build();

        try {
            // El servicio actualiza y nos devuelve el Usuario actualizado para refrescar la sesión
            Usuario usuarioActualizado = usuarioService.actualizarPerfilCompleto(usuarioSesion.getId(), datos);

            // Actualizamos la sesión con los nuevos datos (importante si cambió el nombre o correo)
            session.setAttribute("usuario", usuarioActualizado);

            return ResponseEntity.ok(Map.of("success", true, "message", "Perfil actualizado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}