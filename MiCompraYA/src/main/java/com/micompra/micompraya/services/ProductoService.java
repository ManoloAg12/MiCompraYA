package com.micompra.micompraya.services;

import com.micompra.micompraya.models.*;
import com.micompra.micompraya.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;
    private final EstadoProductoService estadoProductoService;
    private final TransaccionService transaccionService;


    public Producto agregarProductoConImagen(Producto producto, MultipartFile imagen, Usuario usuarioCreador) {
        Assert.notNull(producto, "El producto es requerido");

        // Validaciones básicas
        Assert.hasText(producto.getNombre(), "El nombre del producto es requerido");
        Assert.hasText(producto.getDescripcion(), "La descripción es requerida");
        Assert.notNull(producto.getCategoria() != null && producto.getCategoria().getId() != null, "La categoría es requerida");
        Assert.notNull(producto.getMarca() != null && producto.getMarca().getId() != null, "La marca es requerida");

        // --- LÓGICA MOVIDA DESDE EL CONTROLADOR ---
        // 1. Buscar las entidades completas usando los IDs que vienen del formulario
        Categoria categoria = categoriaService.findById(producto.getCategoria().getId());
        Marca marca = marcaService.findById(producto.getMarca().getId());
        EstadoProducto estado = estadoProductoService.findById(1);

        // Es una buena práctica validar que las entidades existen
        Assert.notNull(categoria, "La categoría especificada no existe.");
        Assert.notNull(marca, "La marca especificada no existe.");
        if (estado == null) {
            throw new RuntimeException("Error del sistema: El estado de producto por defecto (ID 1) no existe.");
        }

        // 2. Asignar las entidades gestionadas (obtenidas de la BD) al producto
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        producto.setEstadoProducto(estado);
        // --- FIN DE LA LÓGICA MOVIDA ---

        // 3. Guardar la imagen si existe
        if (imagen != null && !imagen.isEmpty()) {
            try {
                // 1. Normalizar el nombre para quitar acentos y caracteres especiales
                String nombreNormalizado = Normalizer.normalize(producto.getNombre(), Normalizer.Form.NFD);
                nombreNormalizado = nombreNormalizado.replaceAll("[\\p{InCombiningDiacriticalMarks}]", ""); // Quita acentos
                nombreNormalizado = nombreNormalizado.replaceAll("\\s+", ""); // Quita espacios

                // 2. Usar el nombre normalizado para crear el nombre del archivo
                String nombreArchivo = nombreNormalizado
                        + "_" + System.currentTimeMillis()
                        + getExtension(imagen.getOriginalFilename());

                String rutaCarpeta = "C:/micompraya-images/images/productos/";
                File carpeta = new File(rutaCarpeta);
                if (!carpeta.exists()) {
                    carpeta.mkdirs();
                }

                File archivoDestino = new File(carpeta, nombreArchivo);
                imagen.transferTo(archivoDestino);
                producto.setUrlImagen("/images/productos/" + nombreArchivo);

            } catch (Exception e) {
                // En un caso real, sería bueno loggear el error
                // y lanzar una excepción personalizada.
                e.printStackTrace();
                throw new RuntimeException("Error al guardar la imagen del producto", e);
            }
        }

        // 4. Asignar la fecha de creación
        producto.setFechaAgregado(LocalDate.now());

        // 5. Guardar el producto
        Producto productoGuardado = productoRepository.save(producto);

        // --- 📝 NUEVO: REGISTRAR EN AUDITORÍA SOLO SI HAY STOCK INICIAL ---
        if (productoGuardado.getStock() > 0) {
            try {
                transaccionService.registrarAuditoriaAltaProducto(productoGuardado, usuarioCreador);
            } catch (Exception e) {
                // Logueamos el error pero no detenemos la creación del producto, es un proceso secundario
                System.err.println("Error al registrar auditoría de alta de producto: " + e.getMessage());
            }
        }

        // 5. Guardar el producto completamente ensamblado en la base de datos
        return productoGuardado;
    }


    private String getExtension(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.lastIndexOf(".") == -1) {
            return ""; // No hay extensión
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf("."));
    }

    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }


    public List<Producto> filtrarYOrdenar(List<Producto> todosLosProductos, Integer categoriaId, String busqueda, String orden) {

        // 1. Empezamos con un Stream de todos los productos
        java.util.stream.Stream<Producto> streamProductos = todosLosProductos.stream();

        // --- NUEVO FILTRO: Solo mostrar productos ACTIVOS (ID 1) ---
        streamProductos = streamProductos.filter(p -> p.getEstadoProducto().getId() == 1 || p.getEstadoProducto().getId() == 2);

        // 2. Aplicamos el filtro de Categoría (si existe)
        if (categoriaId != null) {
            streamProductos = streamProductos.filter(p -> p.getCategoria().getId().equals(categoriaId));
        }

        // 3. Aplicamos el filtro de Búsqueda (si existe)
        if (busqueda != null && !busqueda.isBlank()) {
            String busquedaLower = busqueda.toLowerCase();
            streamProductos = streamProductos.filter(p -> p.getNombre().toLowerCase().contains(busquedaLower));
        }

        // 4. Aplicamos el Ordenamiento
        Comparator<Producto> comparador;
        switch (orden != null ? orden : "nombre-asc") { // Default a "nombre-asc"
            case "nombre-desc":
                comparador = Comparator.comparing(Producto::getNombre).reversed();
                break;
            case "precio-asc":
                comparador = Comparator.comparing(Producto::getPrecio);
                break;
            case "precio-desc":
                comparador = Comparator.comparing(Producto::getPrecio).reversed();
                break;
            case "nombre-asc":
            default:
                comparador = Comparator.comparing(Producto::getNombre);
                break;
        }
        streamProductos = streamProductos.sorted(comparador);

        // 5. Retornamos la lista final
        return streamProductos.collect(Collectors.toList());
    }

    //agregar marca

    @Transactional // Asegura atomicidad
    public Producto actualizarStockYEstado(Integer productoId, Integer nuevoEstadoId, Integer cantidadAgregar) {
        Assert.notNull(productoId, "El ID del producto es requerido.");
        Assert.notNull(nuevoEstadoId, "El ID del estado es requerido.");
        Assert.notNull(cantidadAgregar, "La cantidad a agregar es requerida (puede ser 0).");
        Assert.isTrue(cantidadAgregar >= 0, "La cantidad a agregar no puede ser negativa.");

        // 1. Buscar el producto existente
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        // 2. Buscar y validar el nuevo estado
        EstadoProducto nuevoEstado = estadoProductoService.findById(nuevoEstadoId);
        Assert.notNull(nuevoEstado, "El estado especificado no existe.");

        // 3. Actualizar el estado
        producto.setEstadoProducto(nuevoEstado);

        // 4. Actualizar el stock (si se indicó cantidad > 0)
        if (cantidadAgregar > 0) {
            int stockActual = producto.getStock();
            producto.setStock(stockActual + cantidadAgregar);
        }

        // 5. Guardar los cambios
        return productoRepository.save(producto);
    }

    @Transactional(readOnly = true) // Es una consulta
    public List<Producto> obtenerProductosConMasStock(int cantidad) {
        Assert.isTrue(cantidad > 0, "La cantidad debe ser mayor que cero.");
        // Llama al nuevo método del repositorio
        return productoRepository.findFirst4ByOrderByStockDesc();
    }

    @Transactional(readOnly = true)
    public List<Producto> listarProductosActivos() {
        // Buscamos productos con estado 1 (Activo) y 2 (Agotado)
        return productoRepository.findByEstadoProducto_IdIn(List.of(1, 2));
    }


    @Transactional
    public void editarInformacionProducto(Integer id, String nuevoNombre, String nuevaDescripcion, LocalDate nuevaCaducidad, MultipartFile nuevaImagen, boolean descontinuar, Integer marcaId, Integer categoriaId) {

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        // Actualizamos datos básicos
        producto.setNombre(nuevoNombre);
        producto.setDescripcion(nuevaDescripcion);
        producto.setCaducidad(nuevaCaducidad);

        // 2. Actualizar Marca y Categoría
        Marca marca = marcaService.findById(marcaId);
        Categoria categoria = categoriaService.findById(categoriaId);

        if(marca != null) producto.setMarca(marca);
        if(categoria != null) producto.setCategoria(categoria);

        if (descontinuar) {
            // Si el usuario marca "Descontinuar", forzamos estado 3
            EstadoProducto estadoDescontinuado = estadoProductoService.findById(3);
            if (estadoDescontinuado != null) {
                producto.setEstadoProducto(estadoDescontinuado);
            }
        } else {
            // Si NO está descontinuado, el estado depende del stock actual
            // Si tiene stock (>0) -> Activo (1)
            // Si no tiene stock (0) -> Agotado (2)
            int idEstado = (producto.getStock() > 0) ? 1 : 2;
            EstadoProducto estadoAutomatico = estadoProductoService.findById(idEstado);
            if (estadoAutomatico != null) {
                producto.setEstadoProducto(estadoAutomatico);
            }
        }

        // Lógica de Imagen (Solo si se sube una nueva)
        if (nuevaImagen != null && !nuevaImagen.isEmpty()) {
            try {
                String nombreNormalizado = Normalizer.normalize(nuevoNombre, Normalizer.Form.NFD)
                        .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                        .replaceAll("\\s+", "");

                String nombreArchivo = nombreNormalizado + "_" + System.currentTimeMillis()
                        + getExtension(nuevaImagen.getOriginalFilename());

                // Asegúrate de usar TU ruta configurada en application.properties
                String rutaCarpeta = "C:/micompraya-images/images/productos/";

                File carpeta = new File(rutaCarpeta);
                if (!carpeta.exists()) carpeta.mkdirs();

                File archivoDestino = new File(carpeta, nombreArchivo);
                nuevaImagen.transferTo(archivoDestino);

                producto.setUrlImagen("/images/productos/" + nombreArchivo);

            } catch (Exception e) {
                throw new RuntimeException("Error al actualizar la imagen", e);
            }
        }

        productoRepository.save(producto);
    }

    @Transactional(readOnly = true)
    public Producto obtenerProductoMasCaro() {
        return productoRepository.findTopByOrderByPrecioDesc();
    }

    @Transactional(readOnly = true)
    public Producto obtenerProductoMasBarato() {
        return productoRepository.findTopByOrderByPrecioAsc();
    }



}
