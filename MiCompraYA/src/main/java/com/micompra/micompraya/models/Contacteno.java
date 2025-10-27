package com.micompra.micompraya.models;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "contactenos", schema = "public")
public class Contacteno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "telefono", length = 25)
    private String telefono;

    @Column(name = "correo", nullable = false, length = 255)
    private String correo;

    @Column(name = "asunto", nullable = false, length = 255)
    private String asunto;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;



    // --- Getters y Setters Manuales ---

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }



    // --- Constructores ---

    public Contacteno() {
        // Constructor vacío requerido por JPA
    }

    public Contacteno(Integer id, String nombre, String telefono, String correo, String asunto, String mensaje, OffsetDateTime fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.correo = correo;
        this.asunto = asunto;
        this.mensaje = mensaje;
    }

    // --- Método toString ---

    @Override
    public String toString() {
        return "Contacteno{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", telefono='" + telefono + '\'' +
                ", correo='" + correo + '\'' +
                ", asunto='" + asunto + '\'' +
                ", mensaje='" + mensaje + '\'' +
                '}';
    }
}