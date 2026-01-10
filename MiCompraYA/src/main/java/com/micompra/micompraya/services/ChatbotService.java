package com.micompra.micompraya.services;

import com.google.cloud.dialogflow.v2.WebhookRequest;
import com.google.cloud.dialogflow.v2.WebhookResponse;
import com.google.protobuf.Value;
import com.micompra.micompraya.models.Producto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.text.NumberFormat;
import java.util.Locale;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    // Inyectamos el servicio que ya tienes
    private final ProductoService productoService;

    /**
     * Procesa la solicitud de Webhook de Dialogflow y genera una respuesta.
     */
    public WebhookResponse manejarSolicitud(WebhookRequest request) {
        String intentNombre = request.getQueryResult().getIntent().getDisplayName();
        String respuestaTexto = "";

        // CASO 1: Recomendaciones (El que ya tenías)
        if ("RecomendarProducto".equals(intentNombre)) {
            // ... (tu código existente de recomendar) ...
            Map<String, Value> parametros = request.getQueryResult().getParameters().getFieldsMap();
            if (parametros.containsKey("tipoProducto")) {
                String tipoProducto = parametros.get("tipoProducto").getStringValue();
                respuestaTexto = generarRespuestaRecomendacion(tipoProducto);
            }
        }

        // CASO 2: Consultar Precios Extremos (NUEVO)
        else if ("ConsultarPrecioExtremo".equals(intentNombre)) {
            Map<String, Value> parametros = request.getQueryResult().getParameters().getFieldsMap();

            // Dialogflow nos enviará un parámetro "tipoExtremo" (caro o barato)
            if (parametros.containsKey("tipoExtremo")) {
                String tipoExtremo = parametros.get("tipoExtremo").getStringValue();
                respuestaTexto = generarRespuestaPrecioExtremo(tipoExtremo);
            } else {
                respuestaTexto = "¿Te refieres al más caro o al más barato?";
            }
        }

        // CASO DEFAULT
        else {
            respuestaTexto = "Lo siento, no entendí tu pregunta. Puedo recomendarte productos o decirte cuál es el más barato/caro.";
        }

        return WebhookResponse.newBuilder()
                .setFulfillmentText(respuestaTexto)
                .build();
    }

    // --- NUEVO MÉTODO DE LÓGICA ---
    private String generarRespuestaPrecioExtremo(String tipoExtremo) {
        Producto p = null;
        String adjetivo = "";

        if ("caro".equalsIgnoreCase(tipoExtremo) || "alto".equalsIgnoreCase(tipoExtremo)) {
            p = productoService.obtenerProductoMasCaro();
            adjetivo = "más costoso";
        } else if ("barato".equalsIgnoreCase(tipoExtremo) || "bajo".equalsIgnoreCase(tipoExtremo)) {
            p = productoService.obtenerProductoMasBarato();
            adjetivo = "más económico";
        }

        if (p != null) {
            // Formatear precio bonito (ej: $12.50)
            NumberFormat formatoDinero = NumberFormat.getCurrencyInstance(Locale.US);
            String precioBonito = formatoDinero.format(p.getPrecio());

            return "El producto " + adjetivo + " es: " + p.getNombre() + " con un precio de " + precioBonito + ".";
        } else {
            return "Actualmente no tengo productos registrados para comparar.";
        }
    }

    /**
     * Lógica de negocio para generar la recomendación.
     * Usa el ProductoService existente.
     */
    private String generarRespuestaRecomendacion(String tipoProducto) {
        List<Producto> productos = productoService.findAll(); //

        // Filtramos por el nombre de la categoría (simple)
        List<Producto> filtrados = productos.stream()
                .filter(p -> p.getCategoria().getCategoria().equalsIgnoreCase(tipoProducto)) //
                .limit(2)
                .collect(Collectors.toList());

        if (filtrados.isEmpty()) {
            return "¡Ups! Parece que no encontré productos de la categoría '" + tipoProducto + "' para recomendarte ahora mismo.";
        }

        // Construimos la respuesta
        String nombresProductos = filtrados.stream()
                .map(Producto::getNombre)
                .collect(Collectors.joining(" y "));

        return "¡Claro! En '" + tipoProducto + "' te recomiendo probar: " + nombresProductos;
    }
}