package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Contacteno;
import com.micompra.micompraya.repositories.ContactenoRepository;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import com.micompra.micompraya.services.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactenoService {

    private final ContactenoRepository contactenoRepository;
    private final EmailService emailService;

    @Transactional // Asegura que la operación de guardado sea atómica
    public Contacteno guardarMensaje(Contacteno mensaje) {
        // Validaciones básicas de negocio
        Assert.notNull(mensaje, "El objeto de mensaje no puede ser nulo.");
        Assert.hasText(mensaje.getNombre(), "El nombre es requerido.");
        Assert.hasText(mensaje.getCorreo(), "El correo es requerido.");
        Assert.hasText(mensaje.getAsunto(), "El asunto es requerido.");
        Assert.hasText(mensaje.getMensaje(), "El mensaje es requerido.");
        // Opcional: Validar formato de correo, longitud de teléfono, etc.

        // Guardar en la base de datos usando el repositorio
        return contactenoRepository.save(mensaje);
    }

    public List<Contacteno> obtenerTodosLosMensajes() {
        return contactenoRepository.findAll();
    }

    //enviar y borrar mensaje
    @Transactional
    public void responderYBorrarMensaje(Integer mensajeId, String to, String subject, String body) throws MessagingException {
        Assert.notNull(mensajeId, "El ID del mensaje es requerido.");
        Assert.hasText(to, "El destinatario es requerido.");
        Assert.hasText(subject, "El asunto es requerido.");
        Assert.hasText(body, "El cuerpo de la respuesta es requerido.");

        if (!contactenoRepository.existsById(mensajeId)) {
            throw new IllegalArgumentException("El mensaje original (ID: " + mensajeId + ") no fue encontrado.");
        }

        emailService.sendEmail(to, subject, body);

        contactenoRepository.deleteById(mensajeId);
    }

}
