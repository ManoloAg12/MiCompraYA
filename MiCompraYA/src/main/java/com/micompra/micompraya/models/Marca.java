package com.micompra.micompraya.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "marca", schema = "public")
public class Marca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marca", nullable = false)
    private Integer id;

    @Column(name = "nombre_marca", nullable = false, length = 100)
    private String nombreMarca;

    //geter y seter
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getNombreMarca() {
        return nombreMarca;
    }
    public void setNombreMarca(String nombreMarca) {
        this.nombreMarca = nombreMarca;
    }

    public Marca() {

    }

    public Marca(Integer id, String nombreMarca) {
        this.id = id;
        this.nombreMarca = nombreMarca;
    }

    @Override
    public String toString() {
        return "Marca{" +
                "id=" + id +
                ", nombreMarca='" + nombreMarca + '\'' +
                '}';
    }

}