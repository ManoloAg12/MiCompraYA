package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Producto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final ProductoService productoService;

    // Constantes para los nombres de los atributos de sesión
    private static final String CARRITO_SESSION_KEY = "carrito";
    private static final String CANTIDADES_SESSION_KEY = "cantidades";

    // Método para obtener la lista de productos del carrito
    @SuppressWarnings("unchecked")
    public List<Producto> getProductosEnCarrito(HttpSession session) {
        List<Producto> carrito = (List<Producto>) session.getAttribute(CARRITO_SESSION_KEY);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(CARRITO_SESSION_KEY, carrito);
        }
        return carrito;
    }

    // Método para obtener el mapa de cantidades
    @SuppressWarnings("unchecked")
    public Map<Integer, Integer> getCantidades(HttpSession session) {
        Map<Integer, Integer> cantidades = (Map<Integer, Integer>) session.getAttribute(CANTIDADES_SESSION_KEY);
        if (cantidades == null) {
            cantidades = new HashMap<>();
            session.setAttribute(CANTIDADES_SESSION_KEY, cantidades);
        }
        return cantidades;
    }

    // Lógica para agregar un producto
    public void agregarAlCarrito(Integer productoId, HttpSession session) {
        Producto producto = productoService.obtenerPorId(productoId);
        if (producto == null) {
            // Opcional: lanzar una excepción si el producto no se encuentra
            return;
        }

        //  OBTENEMOS LA CANTIDAD ACTUAL
        Map<Integer, Integer> cantidades = getCantidades(session);
        int cantidadActual = cantidades.getOrDefault(productoId, 0);

        //  VALIDACIÓN DE STOCK
        if (cantidadActual + 1 > producto.getStock()) {
            System.out.println("STOCK LIMIT: No se puede agregar más del producto " + producto.getNombre());
            return; // Detenemos la ejecución si se excede el stock
        }
        List<Producto> carrito = getProductosEnCarrito(session);

        //  AGREGAMOS EL PRODUCTO AL CARRITO
        if (cantidades.containsKey(productoId)) {
            cantidades.put(productoId, cantidades.get(productoId) + 1);
        } else {
            carrito.add(producto);
            cantidades.put(productoId, 1);
        }
    }

    // Lógica para eliminar un producto
    public void eliminarDelCarrito(Integer productoId, HttpSession session) {
// 1. Obtenemos las listas actuales
        List<Producto> carritoActual = getProductosEnCarrito(session);
        Map<Integer, Integer> cantidades = getCantidades(session);

        // ✅ LA SOLUCIÓN: Creamos una copia segura de la lista de productos
        List<Producto> carritoCopia = new ArrayList<>(carritoActual);

        // 2. Modificamos LA COPIA, no la original
        carritoCopia.removeIf(p -> p.getId().equals(productoId));
        cantidades.remove(productoId); // El mapa no suele dar este problema, pero es buena práctica

        // 3. Guardamos las colecciones actualizadas de vuelta en la sesión
        session.setAttribute("carrito", carritoCopia);
        session.setAttribute("cantidades", cantidades);
    }

    // Lógica para actualizar la cantidad de un producto
    public void actualizarCantidad(Integer productoId, int cantidad, HttpSession session) {
        Producto producto = productoService.obtenerPorId(productoId);
        if (producto == null) return;

        if (cantidad <= 0) {
            eliminarDelCarrito(productoId, session);
            return;
        }

        // ✅ VALIDACIÓN DE STOCK: Asegura que la cantidad no supere el stock
        int cantidadValidada = Math.min(cantidad, producto.getStock());

        Map<Integer, Integer> cantidades = getCantidades(session);
        List<Producto> carrito = getProductosEnCarrito(session);

        boolean existe = carrito.stream().anyMatch(p -> p.getId().equals(productoId));
        if (!existe) {
            carrito.add(producto);
        }
        cantidades.put(productoId, cantidadValidada);
    }

    // Lógica para obtener el total de items
    public int getTotalItems(HttpSession session) {
        Map<Integer, Integer> cantidades = getCantidades(session);
        return cantidades.values().stream().mapToInt(Integer::intValue).sum();
    }
}