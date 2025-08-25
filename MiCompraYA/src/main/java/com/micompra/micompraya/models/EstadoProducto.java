package com.micompra.micompraya.models;

import jakarta.persistence.*;

@Entity
@Table(name = "estado_producto", schema = "public")
public class EstadoProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_producto", nullable = false)
    private Integer id;

    @Column(name = "estado_producto", nullable = false, length = 50)
    private String estadoProducto;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEstadoProducto() {
        return estadoProducto;
    }

    public void setEstadoProducto(String estadoProducto) {
        this.estadoProducto = estadoProducto;
    }

    public EstadoProducto() {
    }

    public EstadoProducto(Integer id, String estadoProducto) {
        this.id = id;
        this.estadoProducto = estadoProducto;
    }

    @Override
    public String toString() {
        return "EstadoProducto{" +
                "id=" + id +
                ", estadoProducto='" + estadoProducto + '\'' +
                '}';
    }
}