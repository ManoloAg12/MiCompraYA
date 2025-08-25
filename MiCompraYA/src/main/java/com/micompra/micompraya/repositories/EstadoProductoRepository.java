package com.micompra.micompraya.repositories;


import com.micompra.micompraya.models.EstadoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EstadoProductoRepository extends JpaRepository<EstadoProducto, Integer> {
}
