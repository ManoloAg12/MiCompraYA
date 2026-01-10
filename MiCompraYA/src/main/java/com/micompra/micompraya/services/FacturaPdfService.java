package com.micompra.micompraya.services;

import com.lowagie.text.*;
import com.lowagie.text.Font; // Asegúrate de importar com.lowagie.text.Font
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.micompra.micompraya.models.DetallePedido;
import com.micompra.micompraya.models.Pedido;
import org.springframework.stereotype.Service;

import java.awt.*; // Para Color
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class FacturaPdfService {

    // Formateadores y fuentes reutilizables
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(Locale.US); // Formato $ El Salvador
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
    private static final Font FONT_SUBTITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.GRAY);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

    public byte[] generarFacturaPdf(Pedido pedido, List<DetallePedido> detalles) throws DocumentException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);

        document.open();

        // --- Encabezado ---
        Paragraph titulo = new Paragraph("Factura MiCompraYA", FONT_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        document.add(titulo);

        Paragraph subtitulo = new Paragraph("Pedido: " + pedido.getCodigo(), FONT_SUBTITULO);
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitulo);
        document.add(Chunk.NEWLINE); // Espacio

        // --- Datos del Cliente y Pedido ---
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.addCell(crearCeldaInfo("Cliente:", pedido.getCliente().getNombreCompleto()));
        infoTable.addCell(crearCeldaInfo("Fecha:", pedido.getFechaPedido().format(DATE_TIME_FORMATTER)));
        infoTable.addCell(crearCeldaInfo("Dirección:", pedido.getCliente().getDireccion() != null ? pedido.getCliente().getDireccion() : "N/A"));
        infoTable.addCell(crearCeldaInfo("Método Pago:", pedido.getTipoPago().getTipoPago()));
        document.add(infoTable);
        document.add(Chunk.NEWLINE);

        // --- Tabla de Detalles ---
        PdfPTable table = new PdfPTable(4); // 4 columnas: Producto, Cant, P. Unit, Subtotal
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4f, 1f, 1.5f, 1.5f}); // Ancho relativo columnas

        // Cabeceras de la tabla
        table.addCell(crearCeldaHeader("Producto"));
        table.addCell(crearCeldaHeader("Cant."));
        table.addCell(crearCeldaHeader("P. Unitario"));
        table.addCell(crearCeldaHeader("Subtotal"));

        // Filas con productos
        for (DetallePedido detalle : detalles) {
            table.addCell(new Paragraph(detalle.getProducto().getNombre(), FONT_NORMAL));
            table.addCell(crearCeldaAlineada(String.valueOf(detalle.getCantidad()), Element.ALIGN_CENTER));
            table.addCell(crearCeldaAlineada(CURRENCY_FORMATTER.format(detalle.getPrecioUnitario()), Element.ALIGN_RIGHT));
            table.addCell(crearCeldaAlineada(CURRENCY_FORMATTER.format(detalle.getSubtotal()), Element.ALIGN_RIGHT));
        }
        document.add(table);
        document.add(Chunk.NEWLINE);

        // --- Total ---
        Paragraph totalParagraph = new Paragraph("Total: " + CURRENCY_FORMATTER.format(pedido.getTotal()), FONT_TOTAL);
        totalParagraph.setAlignment(Element.ALIGN_RIGHT);
        document.add(totalParagraph);
        document.add(Chunk.NEWLINE);

        // --- Pie de página (Opcional) ---
        Paragraph gracias = new Paragraph("¡Gracias por su compra!", FONT_SUBTITULO);
        gracias.setAlignment(Element.ALIGN_CENTER);
        document.add(gracias);

        document.close();
        return baos.toByteArray();
    }

    // --- Métodos de ayuda para crear celdas ---
    private PdfPCell crearCeldaInfo(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + " ", FONT_BOLD));
        p.add(new Chunk(value, FONT_NORMAL));
        cell.addElement(p);
        return cell;
    }

    private PdfPCell crearCeldaHeader(String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FONT_BOLD));
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }

    private PdfPCell crearCeldaAlineada(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, FONT_NORMAL));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(4);
        return cell;
    }
}