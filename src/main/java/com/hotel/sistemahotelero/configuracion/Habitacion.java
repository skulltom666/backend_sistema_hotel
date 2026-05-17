package com.hotel.sistemahotelero.configuracion;

import com.hotel.sistemahotelero.huespedes.Huesped;
import com.hotel.sistemahotelero.administracion.Hotel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "habitacion")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_actual", nullable = false)
    private EstadoHabitacion estadoActual;

    @Column(name = "categoria_id")
    private Integer categoriaId;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "limite_personas")
    private Integer limitePersonas;

    @Column(name = "camas_simples")
    private Integer camasSimples;

    @Column(name = "camas_dobles")
    private Integer camasDobles;

    @Column(name = "piso")
    private Integer piso;

    // --- RELACIÓN REAL CON LA BASE DE DATOS ---
    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = true)
    private Hotel hotel;

    // --- CAMPO PUENTE (SOLO PARA RECIBIR DATOS DE ANGULAR) ---
    @Transient // Indica a JPA que ignore este campo al guardar en la BD
    @JsonProperty("hotelId") // Mapea la propiedad 'hotelId' del JSON que envía Angular
    private Long hotelIdRecibido;

    // --- SISTEMA DE TARIFAS ---
    @Column(name = "horas_minimas")
    private Integer horasMinimas;

    @Column(name = "precio_minimo")
    private Double precioMinimo;

    @Column(name = "precio_12_horas")
    private Double precio12Horas;

    @Column(name = "precio_24_horas")
    private Double precio24Horas;

    @Column(name = "precio_hora_extra")
    private Double precioHoraExtra;

    @Column(name = "fecha_check_in")
    private LocalDateTime fechaCheckIn;

    // --- RELACIÓN CON EL HUÉSPED ---
    @ManyToOne
    @JoinColumn(name = "huesped_id", nullable = true)
    private Huesped huespedActual;

    // Constructor vacío obligatorio para JPA
    public Habitacion() {}

    // --- GETTERS Y SETTERS DEL CAMPO PUENTE (RESUELVE EL ERROR EN CONTROLLER) ---
    public Long getHotelIdRecibido() {
        return hotelIdRecibido;
    }

    public void setHotelIdRecibido(Long hotelIdRecibido) {
        this.hotelIdRecibido = hotelIdRecibido;
    }

    // --- GETTERS Y SETTERS ESTÁNDAR ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public EstadoHabitacion getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoHabitacion estadoActual) { this.estadoActual = estadoActual; }

    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Integer getLimitePersonas() { return limitePersonas; }
    public void setLimitePersonas(Integer limitePersonas) { this.limitePersonas = limitePersonas; }

    public Integer getCamasSimples() { return camasSimples; }
    public void setCamasSimples(Integer camasSimples) { this.camasSimples = camasSimples; }

    public Integer getCamasDobles() { return camasDobles; }
    public void setCamasDobles(Integer camasDobles) { this.camasDobles = camasDobles; }

    public Integer getPiso() { return piso; }
    public void setPiso(Integer piso) { this.piso = piso; }

    public Hotel getHotel() { return hotel; }
    public void setHotel(Hotel hotel) { this.hotel = hotel; }

    public Integer getHorasMinimas() { return horasMinimas; }
    public void setHorasMinimas(Integer horasMinimas) { this.horasMinimas = horasMinimas; }

    public Double getPrecioMinimo() { return precioMinimo; }
    public void setPrecioMinimo(Double precioMinimo) { this.precioMinimo = precioMinimo; }

    public Double getPrecio12Horas() { return precio12Horas; }
    public void setPrecio12Horas(Double precio12Horas) { this.precio12Horas = precio12Horas; }

    public Double getPrecio24Horas() { return precio24Horas; }
    public void setPrecio24Horas(Double precio24Horas) { this.precio24Horas = precio24Horas; }

    public Double getPrecioHoraExtra() { return precioHoraExtra; }
    public void setPrecioHoraExtra(Double precioHoraExtra) { this.precioHoraExtra = precioHoraExtra; }

    public LocalDateTime getFechaCheckIn() { return fechaCheckIn; }
    public void setFechaCheckIn(LocalDateTime fechaCheckIn) { this.fechaCheckIn = fechaCheckIn; }

    public Huesped getHuespedActual() { return huespedActual; }
    public void setHuespedActual(Huesped huespedActual) { this.huespedActual = huespedActual; }
}