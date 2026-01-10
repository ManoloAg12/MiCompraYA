package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.Transaccion;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Integer> {
    List<Transaccion> findAllByOrderByFechaTransaccionDesc();

    // Para el Gráfico de Pastel: Suma de cantidades agrupadas por tipo de movimiento
    @Query("SELECT t.tipoMovimiento.nombre, SUM(t.cantidad) FROM Transaccion t GROUP BY t.tipoMovimiento.nombre")
    List<Object[]> obtenerResumenPorTipoMovimiento();

    // Para el Gráfico de Barras: Top productos más vendidos (Filtramos por nombre de movimiento 'Venta')
    // Usaremos Pageable para limitar a los top 5 o 10
    @Query("SELECT t.producto.nombre, SUM(t.cantidad) FROM Transaccion t WHERE t.tipoMovimiento.nombre = 'Venta' GROUP BY t.producto.nombre ORDER BY SUM(t.cantidad) DESC")
    List<Object[]> obtenerTopProductosVendidos(Pageable pageable);

    // Buscar transacciones en un rango de fechas (ordenadas por fecha)
    List<Transaccion> findByFechaTransaccionBetweenOrderByFechaTransaccionDesc(LocalDateTime inicio, LocalDateTime fin);
}
