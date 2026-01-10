package com.micompra.micompraya.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_movimiento", schema = "public")
public class TipoMovimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_movimiento", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "operacion", nullable = false, length = 10)
    private String operacion;

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    // Constructores
    public TipoMovimiento() {
    }

    public TipoMovimiento(Integer id, String nombre, String operacion) {
        this.id = id;
        this.nombre = nombre;
        this.operacion = operacion;
    }

    @Override
    public String toString() {
        return "TipoMovimiento{id=" + id + ", nombre='" + nombre + "', operacion='" + operacion + "'}";
    }
}