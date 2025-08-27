package com.micompra.micompraya.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.micompra.micompraya.repositories.UsuarioRepository;
import com.micompra.micompraya.repositories.EstadoRepository;
import com.micompra.micompraya.repositories.RolRepository;
import com.micompra.micompraya.models.Usuario;
import com.micompra.micompraya.models.Rol;
import com.micompra.micompraya.models.Usuario;
import com.micompra.micompraya.models.Estado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService {

    private final  UsuarioRepository usuarioRepo;
    private final RolRepository rolRepo;
    private final EstadoRepository estadoRepo;
    private final PasswordEncoder passwordEncoder;

    //listar todos los usuarios.
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios()
    {
        return  usuarioRepo.findAll();
    }

    //Listar usuarios activos
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuariosActivos()
    {
        return  usuarioRepo.findByEstado("ACTIVO");
    }

    //Buscar usuario por email
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarUsuarioPorEmail(String correo){
        Assert.hasText(correo, "El correo es incorrecto");
        return  usuarioRepo.findByCorreo(correo);
    }

    //Eliminar usuario por id
    public void eliminarUsuario(Integer id){
        Assert.notNull(id, "El id es requerido");
        if (!usuarioRepo.existsById(id)){
            throw new  IllegalArgumentException("El usuario no existe");
        }
        usuarioRepo.deleteById(id);
    }

    //Cambiar contraseña de usuario
    public Usuario cambiarContrasena(Integer id, String contrasena) {
        Assert.notNull(id, "El id es requerido");
        Assert.hasText(contrasena, "El contrasena es requerido");

        if (!usuarioRepo.existsById(id)) {
            throw  new  IllegalArgumentException("El usuario no existe");
        }
        Usuario usuario = usuarioRepo.findById(id).get();
        String contraActual = usuario.getContrasena();
        if (passwordEncoder.matches(contraActual, contrasena)) {
            throw new IllegalArgumentException("La contrasena no puede ser igual a la anterior");
        }

        if (contraActual.length() < 8)
        {
            throw new IllegalArgumentException("La contrasena deber tener mas de 8 caracteres");
        }

        usuario.setContrasena(passwordEncoder.encode(contrasena));
        return usuarioRepo.save(usuario);
    }



}
