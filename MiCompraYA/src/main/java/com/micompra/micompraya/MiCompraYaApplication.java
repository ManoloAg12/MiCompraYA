package com.micompra.micompraya;

import com.micompra.micompraya.repositories.UsuarioRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling; // 1. IMPORTAR

//prueba
import com.micompra.micompraya.models.Usuario;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;


import java.util.Optional;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling // 2. ANOTAR
public class MiCompraYaApplication {



    public static void main(String[] args) {
        SpringApplication.run(MiCompraYaApplication.class, args);
    }





}
