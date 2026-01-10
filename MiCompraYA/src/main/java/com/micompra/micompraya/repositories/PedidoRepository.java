package com.micompra.micompraya.repositories;


import com.micompra.micompraya.models.Cliente;
import com.micompra.micompraya.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; // 1. IMPORTAR
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findByCodigo(String codigo);

    List<Pedido> findByClienteOrderByFechaPedidoDesc(Cliente cliente);


    // --- AÑADIR ESTOS DOS MÉTODOS ---

    /**
     * Busca pedidos pendientes (por ID de estado) que fueron creados ANTES de una fecha/hora límite.
     * (Usaremos esto para encontrar pedidos de MÁS de 24 horas).
     */
    List<Pedido> findByEstadoPedido_IdAndFechaPedidoBefore(Integer estadoId, LocalDateTime fechaLimite);

    /**
     * Busca pedidos pendientes (por ID de estado) que fueron creados ENTRE dos fechas/horas.
     * (Usaremos esto para encontrar pedidos de EXACTAMENTE 23 horas de antigüedad).
     */
    List<Pedido> findByEstadoPedido_IdAndFechaPedidoBetween(Integer estadoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);

    List<Pedido> findByEstadoPedido_IdAndFechaPedidoBetweenAndAdvertenciaEnviadaFalse(
            Integer estadoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
