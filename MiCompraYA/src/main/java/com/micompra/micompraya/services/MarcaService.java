package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Marca;
import com.micompra.micompraya.repositories.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public List<Marca> findAll() {
        return marcaRepository.findAll();
    }

    //buscar marca por id
    public Marca findById(Integer id) {
        return marcaRepository.findById(id).orElse(null);
    }

}
