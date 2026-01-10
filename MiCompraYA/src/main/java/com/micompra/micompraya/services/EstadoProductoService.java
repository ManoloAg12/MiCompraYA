package com.micompra.micompraya.services;

import com.micompra.micompraya.models.EstadoProducto;
import com.micompra.micompraya.repositories.EstadoProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoProductoService {

    private final EstadoProductoRepository estadoProductoRepository;

    //buscar todos los estados de productos
    public List<EstadoProducto> findAll() {
        return estadoProductoRepository.findAll();
    }

    //buscar estado de producto por id
    public EstadoProducto findById(Integer id) {
        return estadoProductoRepository.findById(id).orElse(null);
    }

    @Transactional
    public void guardar(EstadoProducto estado) {
        estadoProductoRepository.save(estado);
    }

}
