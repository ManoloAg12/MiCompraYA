package com.micompra.micompraya.models;

import jakarta.persistence.*;

@Entity
@Table(name = "estados", schema = "public")
public class Estado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado", nullable = false)
    private Integer id;

    @Column(name = "estado", nullable = false, length = 50)
    private String estado;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Estado() {
    }

    public Estado(Integer id, String estado) {
        this.id = id;
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Estado{" +
                "id=" + id +
                ", estado='" + estado + '\'' +
                '}';
    }
}