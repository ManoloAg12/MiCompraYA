package com.micompra.micompraya;

import com.micompra.micompraya.repositories.UsuarioRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//prueba
import com.micompra.micompraya.models.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class MiCompraYaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiCompraYaApplication.class, args);
    }

    @Bean
    CommandLineRunner demo(UsuarioRepository usuarioRepository) {
        return (args) -> {
            //Sirve para buscar todos los usuarios
            //for (Usuario u : usuarioRepository.findAll()) {
                //System.out.println(" - " + u.getId() + " ; " + u.getNombre() + " ; " + u.getRol() );
            //}

            //sirve para buscar por nombre del usuario de tipo Optional
            Optional<Usuario> u = usuarioRepository.findByUsuario("Carlos");
            if (u.isPresent()) {
                System.out.println(" - " + u.get().getId() + " ; " + u.get().getUsuario() + " ; " + u.get().getRol() );
            }



            //sirve para usar save
            //Usuario u = new Usuario();
            //u.setNombre("Pepe");
            //u.setCorreo("pepe@pepe");
            //u.setContrasena("1234");
            //u.setRol(new Rol(1, "admin"));
            //u.setFechaRegistro(LocalDate.now());
            //u.setEstado(new Estado(1, "activo"));
            //u = usuarioRepository.save(u);
            //System.out.println(" - " + u.getId() + " - " + u.getNombre());
            //System.out.println("Prueba completa" );

            //sirve para eliminar el usuario

            //usuarioRepository.deleteById(11);

            //sirve para buscar por usuario
        };
    }

}
