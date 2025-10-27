package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Integer> {

    //Listar estado
    List<Estado> findAll();

    //obtener un estado por id
    Optional<Estado> findById(Integer id);
}
