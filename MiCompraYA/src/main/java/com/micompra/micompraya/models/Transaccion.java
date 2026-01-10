package com.micompra.micompraya.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaccion", schema = "public")
public class Transaccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_transaccion", nullable = false)
    private Integer id;

    // Relación con tu modelo Producto existente
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    // Relación con el nuevo modelo TipoMovimiento
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tipo_movimiento", nullable = false)
    private TipoMovimiento tipoMovimiento;

    // Relación con tu modelo Usuario existente (tabla 'usuarios')
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    private Integer stockAnterior;

    @Column(name = "stock_nuevo", nullable = false)
    private Integer stockNuevo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_transaccion", nullable = false)
    private LocalDateTime fechaTransaccion;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public TipoMovimiento getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public Integer getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(Integer stockAnterior) { this.stockAnterior = stockAnterior; }

    public Integer getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(Integer stockNuevo) { this.stockNuevo = stockNuevo; }

    public LocalDateTime getFechaTransaccion() { return fechaTransaccion; }
    public void setFechaTransaccion(LocalDateTime fechaTransaccion) { this.fechaTransaccion = fechaTransaccion; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Constructor Vacío
    public Transaccion() {
    }

    // Constructor con campos principales (útil para el servicio)
    public Transaccion(Producto producto, TipoMovimiento tipoMovimiento, Usuario usuario, Integer cantidad, Integer stockAnterior, Integer stockNuevo, String descripcion) {
        this.producto = producto;
        this.tipoMovimiento = tipoMovimiento;
        this.usuario = usuario;
        this.cantidad = cantidad;
        this.stockAnterior = stockAnterior;
        this.stockNuevo = stockNuevo;
        this.descripcion = descripcion;
        this.fechaTransaccion = LocalDateTime.now(); // Asigna fecha actual automáticamente
    }

    @Override
    public String toString() {
        return "Transaccion{" +
                "id=" + id +
                ", producto=" + (producto != null ? producto.getNombre() : "null") +
                ", tipo=" + (tipoMovimiento != null ? tipoMovimiento.getNombre() : "null") +
                ", usuario=" + (usuario != null ? usuario.getNombreUsuario() : "null") +
                ", cantidad=" + cantidad +
                ", stockAnterior=" + stockAnterior +
                ", stockNuevo=" + stockNuevo +
                ", fecha=" + fechaTransaccion +
                ", descripcion='" + descripcion + '\'' +
                '}';
    }
}