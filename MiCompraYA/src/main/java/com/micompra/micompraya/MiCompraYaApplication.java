package com.micompra.micompraya;

import com.micompra.micompraya.repositories.UsuarioRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

//prueba
import com.micompra.micompraya.models.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;


import java.util.Optional;

@SpringBootApplication
@ServletComponentScan
public class MiCompraYaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiCompraYaApplication.class, args);
    }



}
