package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarcaRepository extends JpaRepository<Marca, Integer> {

    //listar todas las marcas
    List<Marca> findAll();
}
