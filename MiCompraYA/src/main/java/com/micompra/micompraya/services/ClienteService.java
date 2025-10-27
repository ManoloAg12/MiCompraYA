package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Cliente;
import com.micompra.micompraya.models.Usuario;
import com.micompra.micompraya.repositories.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class ClienteService {
    private final ClienteRepository clienteRepository;

    public void agregarCliente(Cliente cliente) {
        clienteRepository.save(cliente);
    }

    public Cliente obtenerClientePorUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            return null;
        }
        // Llama al método del repositorio para encontrar al cliente por el ID del usuario.
        return clienteRepository.findByUsuario_Id(usuario.getId()).orElse(null);
    }

}
