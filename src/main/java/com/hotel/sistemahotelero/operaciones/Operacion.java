package com.hotel.sistemahotelero.operaciones;

import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.huespedes.Huesped;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "operaciones")
public class Operacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;

    @ManyToOne
    @JoinColumn(name = "huesped_id")
    private Huesped huesped;

    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private Double adelanto;
    private String observacion;
    private String estado; // "ACTIVO", "FINALIZADO"

    // --- Getters y Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }

    public Huesped getHuesped() { return huesped; }
    public void setHuesped(Huesped huesped) { this.huesped = huesped; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public LocalDateTime getFechaSalida() { return fechaSalida; }
    public void setFechaSalida(LocalDateTime fechaSalida) { this.fechaSalida = fechaSalida; }

    public Double getAdelanto() { return adelanto; }
    public void setAdelanto(Double adelanto) { this.adelanto = adelanto; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}