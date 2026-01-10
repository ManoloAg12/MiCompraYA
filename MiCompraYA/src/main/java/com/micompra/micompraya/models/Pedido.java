package com.micompra.micompraya.models;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido", schema = "public")
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "fecha_pedido")
    private LocalDateTime fechaPedido;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "estado_pedido", nullable = false)
    private EstadoPedido estadoPedido;

    @Column(name = "codigo", length = 50)
    private String codigo;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "id_tipo_pago")
    private TipoPago tipoPago;

    // --- NUEVO CAMPO ---
    @Column(name = "advertencia_enviada", nullable = false)
    @ColumnDefault("false") // Valor por defecto en la BD (para PostgreSQL)
    private Boolean advertenciaEnviada = false;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public EstadoPedido getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(EstadoPedido estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public TipoPago getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(TipoPago tipoPago) {
        this.tipoPago = tipoPago;
    }

    // --- NUEVOS GETTER Y SETTER ---
    public Boolean getAdvertenciaEnviada() {
        return advertenciaEnviada;
    }

    public void setAdvertenciaEnviada(Boolean advertenciaEnviada) {
        // Aseguramos que no se guarde null si viene de la BD
        this.advertenciaEnviada = (advertenciaEnviada != null) ? advertenciaEnviada : false;
    }

    public Pedido() {
    }

    public Pedido(Integer id, Cliente cliente, LocalDateTime fechaPedido, EstadoPedido estadoPedido, String codigo, BigDecimal total, TipoPago tipoPago) {
        this.id = id;
        this.cliente = cliente;
        this.fechaPedido = fechaPedido;
        this.estadoPedido = estadoPedido;
        this.codigo = codigo;
        this.total = total;
        this.tipoPago = tipoPago;
        this.advertenciaEnviada = (advertenciaEnviada != null) ? advertenciaEnviada : false;
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", cliente=" + cliente +
                ", fechaPedido=" + fechaPedido +
                ", estadoPedido=" + estadoPedido +
                ", codigo='" + codigo + '\'' +
                ", total=" + total +
                ", tipoPago=" + tipoPago +
                ", advertenciaEnviada=" + advertenciaEnviada +
                '}';
    }
}