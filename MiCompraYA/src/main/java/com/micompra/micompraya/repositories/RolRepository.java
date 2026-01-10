package com.micompra.micompraya.repositories;


import com.micompra.micompraya.models.Rol;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    // Contar productos agotados (stock = 0)

    //obtener un rol por id
    Optional<Rol> findById(Integer id);

    // Para el Gráfico de Pastel: Suma de cantidades agrupadas por tipo de movimiento




}
