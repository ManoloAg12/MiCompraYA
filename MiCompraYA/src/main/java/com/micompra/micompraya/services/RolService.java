package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Rol;
import com.micompra.micompraya.repositories.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    public final RolRepository rolRepository;
    @Transactional(readOnly = true)
    public List<Rol> listarRoles() {
        return rolRepository.findAll();
    }

    //obtener un rol por id
    public Rol obtenerRolPorId(Integer id) {
        return rolRepository.findById(id).orElse(null);
    }
}
