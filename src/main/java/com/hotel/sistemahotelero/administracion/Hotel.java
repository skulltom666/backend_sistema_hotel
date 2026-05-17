package com.hotel.sistemahotelero.administracion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hotel.sistemahotelero.seguridad.Usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "hoteles")
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String direccion;
    private boolean suscripcionActiva = false; // Solo se activa cuando "paga"
    private String plan; // "Básico", "Estándar" o "Premium"
    private Integer limiteHabitaciones;

    // En Hotel.java
    @ManyToOne
    @JoinColumn(name = "administrador_id") // <-- ESTO OBLIGA A CREAR LA COLUMNA
    @JsonIgnore
    private Usuario administrador;

    // --- Constructor Vacío ---
    public Hotel() {}

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public boolean isSuscripcionActiva() { return suscripcionActiva; }
    public void setSuscripcionActiva(boolean suscripcionActiva) { this.suscripcionActiva = suscripcionActiva; }
    public Usuario getAdministrador() { return administrador; }
    public void setAdministrador(Usuario administrador) { this.administrador = administrador; }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public Integer getLimiteHabitaciones() {
        return limiteHabitaciones;
    }

    public void setLimiteHabitaciones(Integer limiteHabitaciones) {
        this.limiteHabitaciones = limiteHabitaciones;
    }
}
