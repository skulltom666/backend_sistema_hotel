package com.hotel.sistemahotelero.modules.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HotelRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String direccion;
    private String telefono;
    private String email;
    private String website;

    // Campos adicionales
    private String ubicacionLat;
    private String ubicacionLng;
    private String horarioApertura;
    private String horarioCierre;
    private String descripcion;
}