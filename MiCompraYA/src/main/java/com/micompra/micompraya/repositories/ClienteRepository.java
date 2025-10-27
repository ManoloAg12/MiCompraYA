package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    // Busca un cliente por el ID del usuario
    Optional<Cliente> findByUsuario_Id(Integer usuarioId);
}