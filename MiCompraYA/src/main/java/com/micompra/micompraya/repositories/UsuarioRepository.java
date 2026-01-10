package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.Rol;
import com.micompra.micompraya.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    //Buscar por nombre de usuario
    Optional<Usuario> findByNombreUsuario(String usuario);

    //Buscar por correo
    Optional<Usuario> findByCorreo(String correo);

    List<Usuario> findByRol(Rol rol);

    //Buscar usuarios registrados entre dos fechas
    List<Usuario> findByFechaRegistroBetween(LocalDate fechaInicio, LocalDate fechaFin);

    //buscar un usuario por nombre del rol
    List<Usuario> findByRol_Rol(String rol);

    //buscar un usuario por nombre del rol en lenguaje jpq
    @Query("SELECT u FROM Usuario u WHERE u.rol.rol = :rol")
    List<Usuario> buscarPorRol(@Param("rol") String rol);

    //buscar un usuario por nombre del rol en lenguaje sql nativo;
    @Query(value = "SELECT * FROM usuarios u WHERE u.id_rol = :rol", nativeQuery = true)
    List<Usuario> buscarPorRolSql(@Param("rol") Integer rol);

    //llamar una funcion de la base de datos usando select para las funciones
    @Query(nativeQuery = true, value = "SELECT * FROM obtener_usuarios_activos()")
    List<Usuario> obtenerUsuariosActivos();

    
    //llamar un procedimiento almacenado
    @Procedure(name = "eliminar_usuario_inactivos")
    void eliminarUsuariosInactivos();

    boolean existsByCorreo(String correo);

    boolean existsByNombreUsuario(String nombreUsuario);

    @Query("SELECT u FROM Usuario u WHERE u.estado.id = 1")
    List<Usuario> findUsuariosActivos();





}
