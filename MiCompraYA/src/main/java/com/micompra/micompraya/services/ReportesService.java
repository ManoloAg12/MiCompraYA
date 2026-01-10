package com.micompra.micompraya.services;

import com.micompra.micompraya.repositories.ProductoRepository;
import com.micompra.micompraya.repositories.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportesService {

    private final ProductoRepository productoRepository;
    private final TransaccionRepository transaccionRepository;

    public Map<String, Object> obtenerDatosDashboard() {
        Map<String, Object> data = new HashMap<>();

        // 1. Tarjetas de Resumen (KPIs)
        data.put("stockAgotado", productoRepository.countByStock(0));
        data.put("stockCritico", productoRepository.countByStockLessThanEqual(5));

        // 2. Datos para Gráfico de Pastel (Movimientos)
        List<Object[]> movimientos = transaccionRepository.obtenerResumenPorTipoMovimiento();
        data.put("movimientosLabel", movimientos.stream().map(obj -> obj[0]).toArray());
        data.put("movimientosData", movimientos.stream().map(obj -> obj[1]).toArray());

        // 3. Datos para Gráfico de Barras (Top 5 Vendidos)
        List<Object[]> topVentas = transaccionRepository.obtenerTopProductosVendidos(PageRequest.of(0, 5));
        data.put("topVentasLabel", topVentas.stream().map(obj -> obj[0]).toArray());
        data.put("topVentasData", topVentas.stream().map(obj -> obj[1]).toArray());

        return data;
    }
}