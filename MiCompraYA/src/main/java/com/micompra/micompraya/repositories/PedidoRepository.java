package com.micompra.micompraya.repositories;


import com.micompra.micompraya.models.Cliente;
import com.micompra.micompraya.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findByCodigo(String codigo);

    List<Pedido> findByClienteOrderByFechaPedidoDesc(Cliente cliente);
}
