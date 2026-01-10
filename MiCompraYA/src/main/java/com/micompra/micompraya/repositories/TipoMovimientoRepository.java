package com.micompra.micompraya.repositories;

import com.micompra.micompraya.models.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoMovimientoRepository extends JpaRepository<TipoMovimiento, Integer> {
}
