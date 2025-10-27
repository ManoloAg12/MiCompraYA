package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {


    //listar todas las categorias
    List<Categoria> findAll();
}