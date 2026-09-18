package com.hotel.sistemahotelero.modules.operacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EstadoRequest {
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}