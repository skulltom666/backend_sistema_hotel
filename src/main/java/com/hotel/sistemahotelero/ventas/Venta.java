package com.hotel.sistemahotelero.ventas;

import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.huespedes.Huesped;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "habitacion_id")
    private Habitacion habitacion;

    @ManyToOne
    @JoinColumn(name = "huesped_id")
    private Huesped huesped;

    private Double montoTotal;

    private String medioPago; // CONTADO, QR, POS

    private LocalDateTime fechaVenta;

    private String tiempoEstadia; // Ej: "2h con 15m"

    public Venta() {}

    public Venta(Habitacion habitacion, Huesped huesped, Double montoTotal, String medioPago, String tiempoEstadia) {
        this.habitacion = habitacion;
        this.huesped = huesped;
        this.montoTotal = montoTotal;
        this.medioPago = medioPago;
        this.tiempoEstadia = tiempoEstadia;
        this.fechaVenta = LocalDateTime.now();
    }

    // --- Getters y Setters ---
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Habitacion getHabitacion() { return habitacion; }
    public void setHabitacion(Habitacion habitacion) { this.habitacion = habitacion; }

    public Huesped getHuesped() { return huesped; }
    public void setHuesped(Huesped huesped) { this.huesped = huesped; }

    public Double getMontoTotal() { return montoTotal; }
    public void setMontoTotal(Double montoTotal) { this.montoTotal = montoTotal; }

    public String getMedioPago() { return medioPago; }
    public void setMedioPago(String medioPago) { this.medioPago = medioPago; }

    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime fechaVenta) { this.fechaVenta = fechaVenta; }

    public String getTiempoEstadia() { return tiempoEstadia; }
    public void setTiempoEstadia(String tiempoEstadia) { this.tiempoEstadia = tiempoEstadia; }
}
