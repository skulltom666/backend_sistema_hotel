package com.hotel.sistemahotelero.shared.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "huespedes")
@Data
public class Huesped extends BaseEntity {
    @Column(name = "dni", nullable = false, length = 20)
    private String dni;

    private String nombre;
    private String apellido;
    private String telefono;
    private String email;

    @Column(name = "user_id")
    private Long userId;
}