package com.micompra.micompraya.models;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_pago", schema = "public")
public class TipoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_pago", nullable = false)
    private Integer id;

    @Column(name = "tipo_pago", nullable = false, length = 50)
    private String tipoPago;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipoPago() {
        return tipoPago;
    }

    public void setTipoPago(String tipoPago) {
        this.tipoPago = tipoPago;
    }

    public TipoPago() {
    }

    public TipoPago(Integer id, String tipoPago) {
        this.id = id;
        this.tipoPago = tipoPago;
    }

    @Override
    public String toString() {
        return "TipoPago{" +
                "id=" + id +
                ", tipoPago='" + tipoPago + '\'' +
                '}';
    }
}