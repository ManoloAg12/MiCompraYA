package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Categoria;
import com.micompra.micompraya.repositories.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    //listar todas las categoriras
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    //buscar categoria por id
    public Categoria findById(Integer id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    @Transactional
    public void guardar(Categoria categoria) {
        categoriaRepository.save(categoria);
    }

}
