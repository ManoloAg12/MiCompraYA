package com.micompra.micompraya.repositories;


import com.micompra.micompraya.models.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    long countByStock(Integer stock);
    long countByStockLessThanEqual(Integer stock);

    List<Producto> findFirst4ByOrderByStockDesc();

    // Busca todos los productos que tengan un estado específico (ej. 1)
    List<Producto> findByEstadoProducto_IdIn(List<Integer> estados);

    // Obtener el producto con el precio más alto (Orden Descendente, toma el 1ro)
    Producto findTopByOrderByPrecioDesc();

    // Obtener el producto con el precio más bajo (Orden Ascendente, toma el 1ro)
    Producto findTopByOrderByPrecioAsc();
}
