package com.hotel.sistemahotelero.modules.operacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CrearHabitacionRequest {
    @NotNull(message = "El hotelId es obligatorio")
    private Long hotelId;

    @NotNull(message = "El piso es obligatorio")
    private Integer piso;

    @NotBlank(message = "El número es obligatorio")
    private String numero;

    private String descripcion;
    private Integer limitePersonas = 2;
    private Integer camasSimples = 1;
    private Integer camasDobles = 1;
    private Integer horasMinimas = 6;
    private BigDecimal precioMinimo = BigDecimal.ZERO;
    private BigDecimal precio12Horas = BigDecimal.ZERO;
    private BigDecimal precio24Horas = BigDecimal.ZERO;
    private BigDecimal precioHoraExtra = BigDecimal.ZERO;
}