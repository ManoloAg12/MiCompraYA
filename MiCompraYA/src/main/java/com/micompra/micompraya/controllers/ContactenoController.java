package com.micompra.micompraya.controllers;

import com.micompra.micompraya.models.Contacteno;
import com.micompra.micompraya.services.ContactenoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor

public class ContactenoController {

    private final ContactenoService contactenoService;

   @GetMapping("/contactenos")
   public String mostrarContactenos(Model model) {
       model.addAttribute("view", "contactenos/contactenos_view");
       return "layout/layout";
   }

    // ✅ NUEVO MÉTODO para PROCESAR el envío del formulario
    @PostMapping("/contacto/enviar") // Debe coincidir con el th:action del form
    public String procesarFormularioContacto(
            @ModelAttribute Contacteno contacteno, // Spring mapea los campos del form a este objeto
            RedirectAttributes redirectAttributes) {

        try {
            // Llama al servicio para guardar el mensaje (la lógica está en el servicio)
            contactenoService.guardarMensaje(contacteno);

            // Mensaje de éxito para mostrar después de redirigir (usando tu sistema de toasts)
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "¡Gracias! Tu mensaje ha sido enviado correctamente.");

            // Redirige de vuelta a la página de contacto (o a donde prefieras)
            return "redirect:/contactenos";

        } catch (IllegalArgumentException e) {
            // Captura errores de validación del servicio
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error: " + e.getMessage());
            // Guarda los datos ingresados para mostrarlos de nuevo en el formulario
            redirectAttributes.addFlashAttribute("contacteno", contacteno);
            return "redirect:/contactenos"; // Vuelve a mostrar el formulario con el error
        } catch (Exception e) {
            // Captura cualquier otro error inesperado
            e.printStackTrace(); // Es bueno loggear el error
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Ocurrió un error inesperado al enviar tu mensaje.");
            redirectAttributes.addFlashAttribute("contacteno", contacteno);
            return "redirect:/contactenos";
        }
    }

    @GetMapping("/mensajes_view")
    public String mostrarMensajes(Model model) {
        model.addAttribute("mensajes", contactenoService.obtenerTodosLosMensajes());
        model.addAttribute("view", "contactenos/mensajes_view");
        return "layout/layout";
    }

    //enviar y borrar
    // ✅ MÉTODO MODIFICADO: Procesa la respuesta y redirige con toast
    @PostMapping("/mensajes/responder")
    public String procesarRespuesta(
            @RequestParam("mensajeId") Integer mensajeId,
            @RequestParam("to") String to,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            RedirectAttributes redirectAttributes // Para enviar mensajes flash (toasts)
    ) {
        try {
            // Llama al servicio que envía el correo y borra el mensaje original
            contactenoService.responderYBorrarMensaje(mensajeId, to, subject, body);
            // Prepara el mensaje de éxito para el toast
            redirectAttributes.addFlashAttribute("tipo", "success");
            redirectAttributes.addFlashAttribute("mensaje", "Respuesta enviada y mensaje original eliminado correctamente.");
        } catch (IllegalArgumentException e) {
            // Error si el mensaje original no se encontró
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
        } catch (Exception e) { // Captura MessagingException u otros
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("tipo", "error");
            redirectAttributes.addFlashAttribute("mensaje", "Error al procesar la respuesta. Revisa la consola.");
        }
        // Redirige siempre a la lista de mensajes
        return "redirect:/mensajes_view";
    }



}
