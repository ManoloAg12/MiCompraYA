package com.micompra.micompraya.services;

import com.micompra.micompraya.models.Categoria;
import com.micompra.micompraya.models.EstadoProducto;
import com.micompra.micompraya.models.Marca;
import com.micompra.micompraya.models.Producto;
import com.micompra.micompraya.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository productoRepository;
    private final MarcaService marcaService;
    private final CategoriaService categoriaService;
    private final EstadoProductoService estadoProductoService;

    public Producto agregarProductoConImagen(Producto producto, MultipartFile imagen) {
        Assert.notNull(producto, "El producto es requerido");

        // Validaciones básicas
        Assert.hasText(producto.getNombre(), "El nombre del producto es requerido");
        Assert.hasText(producto.getDescripcion(), "La descripción es requerida");
        Assert.notNull(producto.getCategoria() != null && producto.getCategoria().getId() != null, "La categoría es requerida");
        Assert.notNull(producto.getMarca() != null && producto.getMarca().getId() != null, "La marca es requerida");
        Assert.notNull(producto.getEstadoProducto() != null && producto.getEstadoProducto().getId() != null, "El estado es requerido");

        // --- LÓGICA MOVIDA DESDE EL CONTROLADOR ---
        // 1. Buscar las entidades completas usando los IDs que vienen del formulario
        Categoria categoria = categoriaService.findById(producto.getCategoria().getId());
        Marca marca = marcaService.findById(producto.getMarca().getId());
        EstadoProducto estado = estadoProductoService.findById(producto.getEstadoProducto().getId());

        // Es una buena práctica validar que las entidades existen
        Assert.notNull(categoria, "La categoría especificada no existe.");
        Assert.notNull(marca, "La marca especificada no existe.");
        Assert.notNull(estado, "El estado especificado no existe.");

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

                String rutaCarpeta = new File("src/main/resources/static/images/productos").getAbsolutePath();
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

        // 5. Guardar el producto completamente ensamblado en la base de datos
        return productoRepository.save(producto);
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

    //agregar marca






}
