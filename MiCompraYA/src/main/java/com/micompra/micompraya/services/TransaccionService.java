package com.micompra.micompraya.services;

import com.micompra.micompraya.models.*;
import com.micompra.micompraya.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.micompra.micompraya.models.Transaccion;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final ProductoRepository productoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final EstadoProductoRepository estadoProductoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    public List<Transaccion> listarTodas() {
        return transaccionRepository.findAllByOrderByFechaTransaccionDesc();

    }

    public List<TipoMovimiento> listarTiposMovimiento() {
        return tipoMovimientoRepository.findAll();
    }

    private String[] obtenerCorreosStaff() {
        // Buscar Admins (Rol 1) y Supervisores (Rol 4)
        List<Usuario> admins = usuarioRepository.buscarPorRolSql(1);
        List<Usuario> supervisores = usuarioRepository.buscarPorRolSql(4);

        List<String> correos = new ArrayList<>();
        admins.forEach(u -> correos.add(u.getCorreo()));
        supervisores.forEach(u -> correos.add(u.getCorreo()));

        return correos.toArray(new String[0]);
    }


    @Transactional
    public void registrarMovimiento(Integer productoId, Integer tipoMovimientoId, Integer cantidad, String descripcion, Usuario usuario) {

        // 1. Validaciones básicas
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor a 0.");

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // Si el producto está DESCONTINUADO (ID 3), prohibimos mover stock.
        if (producto.getEstadoProducto().getId() == 3) {
            throw new IllegalStateException("ERROR: El producto '" + producto.getNombre() + "' está DESCONTINUADO. " +
                    "No se pueden registrar movimientos. " +
                    "Si desea reactivarlo, debe editar su información primero.");
        }

        TipoMovimiento tipo = tipoMovimientoRepository.findById(tipoMovimientoId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de movimiento no válido"));

        // 2. Calcular nuevo stock
        int stockAnterior = producto.getStock();
        int stockNuevo;

        if ("SUMA".equalsIgnoreCase(tipo.getOperacion())) {
            stockNuevo = stockAnterior + cantidad;
        } else if ("RESTA".equalsIgnoreCase(tipo.getOperacion())) {
            if (stockAnterior < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para realizar esta salida. Stock actual: " + stockAnterior);
            }
            stockNuevo = stockAnterior - cantidad;
        } else {
            throw new IllegalArgumentException("Operación de movimiento desconocida: " + tipo.getOperacion());
        }

        // 3. Actualizar Producto
        producto.setStock(stockNuevo);
        // A. REACTIVAR: Si ahora hay stock (> 0) y estaba Agotado (2), lo pasamos a Activo (1)
        // Nota: No tocamos si estaba Descontinuado (3), eso es manual.
        if (stockNuevo > 0 && producto.getEstadoProducto().getId() == 2) {
            EstadoProducto estadoActivo = estadoProductoRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Estado Activo (ID 1) no encontrado"));
            producto.setEstadoProducto(estadoActivo);
        }

        // B. AGOTAR: Si el movimiento (ej. Pérdida/Uso) dejó el stock en 0, lo pasamos a Agotado (2)
        if (stockNuevo == 0 && producto.getEstadoProducto().getId() == 1) {
            EstadoProducto estadoAgotado = estadoProductoRepository.findById(2)
                    .orElseThrow(() -> new RuntimeException("Estado Agotado (ID 2) no encontrado"));
            producto.setEstadoProducto(estadoAgotado);
        }
        productoRepository.save(producto);

        if (stockNuevo <= 5) {
            try {
                String[] correosStaff = obtenerCorreosStaff();
                if (correosStaff.length > 0) {
                    emailService.enviarAlertaStock(correosStaff, producto, stockNuevo);
                }
            } catch (Exception e) {
                System.err.println("No se pudo enviar la alerta de stock en movimiento manual: " + e.getMessage());
            }
        }

        // 4. Guardar Transacción (Auditoría)
        Transaccion transaccion = new Transaccion();
        transaccion.setProducto(producto);
        transaccion.setTipoMovimiento(tipo);
        transaccion.setUsuario(usuario);
        transaccion.setCantidad(cantidad);
        transaccion.setStockAnterior(stockAnterior);
        transaccion.setStockNuevo(stockNuevo);
        transaccion.setDescripcion(descripcion);
        transaccion.setFechaTransaccion(LocalDateTime.now());

        transaccionRepository.save(transaccion);
    }

    @Transactional
    public void registrarAuditoriaVenta(Producto producto, Integer cantidadVendida, Integer stockAnterior, Integer stockNuevo, Usuario cajero, String codigoPedido) {

        // Buscamos el tipo de movimiento "Venta" (Asumimos ID 2 según tu script SQL)
        TipoMovimiento tipoVenta = tipoMovimientoRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("El tipo de movimiento 'Venta' (ID 2) no existe."));

        Transaccion transaccion = new Transaccion();
        transaccion.setProducto(producto);
        transaccion.setTipoMovimiento(tipoVenta);
        transaccion.setUsuario(cajero); // El usuario que procesó la venta (Cajero/Admin)
        transaccion.setCantidad(cantidadVendida);
        transaccion.setStockAnterior(stockAnterior);
        transaccion.setStockNuevo(stockNuevo);
        transaccion.setFechaTransaccion(LocalDateTime.now());

        // Descripción automática
        transaccion.setDescripcion("Se realizó una venta - Pedido: " + codigoPedido);

        transaccionRepository.save(transaccion);
    }


    @Transactional
    public void registrarAuditoriaAltaProducto(Producto producto, Usuario usuario) {

        // Buscamos el tipo de movimiento "Compra / Entrada" (Asumimos ID 1 según tu script)
        TipoMovimiento tipoEntrada = tipoMovimientoRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("El tipo de movimiento 'Compra/Entrada' (ID 1) no existe."));

        Transaccion transaccion = new Transaccion();
        transaccion.setProducto(producto);
        transaccion.setTipoMovimiento(tipoEntrada);
        transaccion.setUsuario(usuario);

        // En un producto nuevo, la cantidad inicial es todo lo que hay
        transaccion.setCantidad(producto.getStock());
        transaccion.setStockAnterior(0); // No existía antes
        transaccion.setStockNuevo(producto.getStock());

        transaccion.setFechaTransaccion(LocalDateTime.now());
        transaccion.setDescripcion("Compra inicial del producto");

        transaccionRepository.save(transaccion);
    }


    /*Genera un PDF con las transacciones en un rango de fechas.*/
    public byte[] generarReportePdf(LocalDate fechaInicio, LocalDate fechaFin) {
        // 1. Ajustar fechas para cubrir todo el día (00:00:00 a 23:59:59)
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        // 2. Obtener datos
        List<Transaccion> transacciones = transaccionRepository.findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(inicio, fin);

        // 3. Generar PDF (Lógica OpenPDF)
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Horizontal para que quepa la tabla
            PdfWriter.getInstance(document, baos);
            document.open();

            // Título
            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph titulo = new Paragraph("Reporte de Movimientos de Inventario", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("Desde: " + fechaInicio + "  Hasta: " + fechaFin));
            document.add(Chunk.NEWLINE);

            // Tabla
            PdfPTable tabla = new PdfPTable(7); // 7 Columnas
            tabla.setWidthPercentage(100);
            // Encabezados
            String[] headers = {"Fecha", "Producto", "Tipo", "Usuario", "Cant.", "Stock Ant", "Stock Nuevo"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                tabla.addCell(cell);
            }

            // Datos
            Font fontDatos = FontFactory.getFont(FontFactory.HELVETICA, 9);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (Transaccion t : transacciones) {
                tabla.addCell(new Phrase(t.getFechaTransaccion().format(formatter), fontDatos));
                tabla.addCell(new Phrase(t.getProducto().getNombre(), fontDatos));
                tabla.addCell(new Phrase(t.getTipoMovimiento().getNombre(), fontDatos));
                tabla.addCell(new Phrase(t.getUsuario().getNombreUsuario(), fontDatos));
                tabla.addCell(new Phrase(String.valueOf(t.getCantidad()), fontDatos));
                tabla.addCell(new Phrase(String.valueOf(t.getStockAnterior()), fontDatos));
                tabla.addCell(new Phrase(String.valueOf(t.getStockNuevo()), fontDatos));
            }

            document.add(tabla);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el PDF de transacciones");
        }
    }
}