package com.micompra.micompraya.models;

import jakarta.persistence.*;

@Entity
@Table(name = "estado_pedido", schema = "public")
public class EstadoPedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_pedido", nullable = false)
    private Integer id;

    @Column(name = "estado_pedido", nullable = false, length = 50)
    private String estadoPedido;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(String estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public EstadoPedido() {
    }

    public EstadoPedido(Integer id, String estadoPedido) {
        this.id = id;
        this.estadoPedido = estadoPedido;
    }

    @Override
    public String toString() {
        return "EstadoPedido{" +
                "id=" + id +
                ", estadoPedido='" + estadoPedido + '\'' +
                '}';
    }


}