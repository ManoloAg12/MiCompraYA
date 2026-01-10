package com.micompra.micompraya.controllers;

import com.google.cloud.dialogflow.v2.WebhookRequest;
import com.google.cloud.dialogflow.v2.WebhookResponse;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.micompra.micompraya.models.Producto;
import com.micompra.micompraya.services.ChatbotService;
import com.micompra.micompraya.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import com.google.protobuf.Value;

@RestController // No es @Controller, es @RestController
@RequestMapping("/webhook") // La ruta base será /webhook
@RequiredArgsConstructor
public class WebhookController {

    private final ChatbotService chatbotService;

    private final ProductoService productoService;

    @PostMapping // <-- CRUCIAL: Mapea el método POST a la ruta base "/webhook"
    public ResponseEntity<String> handleWebhook(@RequestBody String requestBody) {
        try {
            // 1. Parsear el JSON de Dialogflow a un WebhookRequest
            WebhookRequest.Builder requestBuilder = WebhookRequest.newBuilder();
            JsonFormat.parser().merge(requestBody, requestBuilder);
            WebhookRequest request = requestBuilder.build();

            // 2. Delegar al servicio para obtener la respuesta
            WebhookResponse response = chatbotService.manejarSolicitud(request);

            // 3. Convertir la respuesta a JSON y enviarla de vuelta
            String jsonResponse = com.google.protobuf.util.JsonFormat.printer().print(response);
            return ResponseEntity.ok(jsonResponse);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al parsear la solicitud de Dialogflow");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    // ... (Tu método manejarSolicitud sin cambios) ...
    public WebhookResponse manejarSolicitud(WebhookRequest request) {
        String intentNombre = request.getQueryResult().getIntent().getDisplayName();
        String respuestaTexto;

        if ("RecomendarProducto".equals(intentNombre)) {
            Map<String, Value> parametros = request.getQueryResult().getParameters().getFieldsMap();

            if (parametros.containsKey("tipoProducto")) {
                String tipoProducto = parametros.get("tipoProducto").getStringValue();
                // Pasamos el tipo de producto al método de recomendación
                respuestaTexto = generarRespuestaRecomendacion(tipoProducto);
            } else {
                respuestaTexto = "No entendí qué categoría de producto buscas. ¿Puedes ser más específico?";
            }
        } else {
            respuestaTexto = "Lo siento, no entendí tu pregunta. Solo puedo recomendar productos.";
        }

        return WebhookResponse.newBuilder()
                .setFulfillmentText(respuestaTexto)
                .build();
    }


    /**
     * Lógica de negocio para generar la recomendación.
     * AHORA USA NORMALIZACIÓN PARA COMPARAR.
     */
    private String generarRespuestaRecomendacion(String tipoProducto) {
        // Normaliza la entrada de Dialogflow (ej: "lácteos" -> "lacteos")
        String tipoProductoNormalizado = normalizarTexto(tipoProducto);

        List<Producto> productos = productoService.findAll();

        List<Producto> filtrados = productos.stream()
                .filter(p -> {
                    // Normaliza el nombre de la categoría de la BD (ej: "Lácteos" -> "lacteos")
                    String categoriaNormalizada = normalizarTexto(p.getCategoria().getCategoria());
                    // Compara los textos normalizados
                    return categoriaNormalizada.equalsIgnoreCase(tipoProductoNormalizado);
                })
                .limit(2) // Tomamos los 2 primeros que encuentre
                .collect(Collectors.toList());

        if (filtrados.isEmpty()) {
            // Esta es la respuesta que estás recibiendo
            return "¡Ups! Parece que no encontré productos de la categoría '" + tipoProducto + "' para recomendarte ahora mismo.";
        }

        // Construimos la respuesta de éxito
        String nombresProductos = filtrados.stream()
                .map(Producto::getNombre)
                .collect(Collectors.joining(" y "));

        return "¡Claro! En '" + tipoProducto + "' te recomiendo probar: " + nombresProductos + ". ¡Tengo varios en stock!";
    }

    /**
     * Función de ayuda para quitar tildes, diéresis y convertir a minúsculas.
     */
    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        // Reemplaza todos los caracteres diacríticos (tildes, etc.)
        return textoNormalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase();
    }
}