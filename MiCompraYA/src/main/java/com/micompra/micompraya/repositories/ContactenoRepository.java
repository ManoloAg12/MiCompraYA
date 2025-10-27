package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.Contacteno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactenoRepository extends JpaRepository<Contacteno, Integer> {
}
