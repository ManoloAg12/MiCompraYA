package com.micompra.micompraya.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.micompra.micompraya.models.DetallePedido;
import com.micompra.micompraya.models.Pedido;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("va224100093@uma.edu.sv");

        mailSender.send(message);
    }

    // 📎 Nuevo método: enviar correo con imagen adjunta (QR)
    public void sendEmailConAdjunto(String to, String subject, String body,
                                    byte[] archivoAdjunto, String nombreArchivo)
            throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body);
        helper.setFrom("va224100093@uma.edu.sv");

        // Agregar el archivo (por ejemplo, el QR)
        helper.addAttachment(nombreArchivo, new ByteArrayResource(archivoAdjunto));

        mailSender.send(message);
    }


    public void enviarCorreoConfirmacion(Pedido pedido, List<DetallePedido> detalles) throws MessagingException {
        byte[] qrImage = generarQrCode(pedido.getCodigo(), 250, 250);

        Context context = new Context();
        context.setVariable("pedido", pedido);
        // ✅ Añadimos la lista de detalles al contexto para que Thymeleaf la use.
        context.setVariable("detalles", detalles);

        String htmlBody = templateEngine.process("email/confirmacion-pedido", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(pedido.getCliente().getUsuario().getCorreo());
        helper.setSubject("Confirmación de tu pedido: " + pedido.getCodigo());
        helper.setText(htmlBody, true);
        helper.setFrom("va224100093@uma.edu.sv"); // Tu correo
        helper.addInline("qrCodeImage", new ByteArrayResource(qrImage), "image/png");

        mailSender.send(message);
    }

    private byte[] generarQrCode(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el código QR", e);
        }
    }
}
