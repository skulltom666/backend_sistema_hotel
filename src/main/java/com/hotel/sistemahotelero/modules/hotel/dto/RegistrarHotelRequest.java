package com.hotel.sistemahotelero.modules.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrarHotelRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String direccion;
    private String telefono;
    private String plan;
    private Integer limiteHabitaciones;
    private String propietario;
}