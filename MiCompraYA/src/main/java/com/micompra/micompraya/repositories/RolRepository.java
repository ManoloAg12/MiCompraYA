package com.micompra.micompraya.repositories;


import com.micompra.micompraya.models.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {
    //obtener un rol por id
    Optional<Rol> findById(Integer id);



}
