package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Estado;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.micompra.micompraya.repositories.EstadoRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EstadoService {

    private final EstadoRepository estadoRepository;
    @Transactional(readOnly = true)
    public List<Estado> listarEstados() {
        return estadoRepository.findAll();
    }

    //obtener un estado por id
    public Estado obtenerEstadoPorId(Integer id) {
        return estadoRepository.findById(id).orElse(null);
    }
}
