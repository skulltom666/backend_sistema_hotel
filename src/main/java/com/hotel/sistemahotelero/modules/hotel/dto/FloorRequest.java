package com.hotel.sistemahotelero.modules.hotel.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FloorRequest {
    @NotNull(message = "El número de piso es obligatorio")
    @Min(value = 1, message = "El número de piso debe ser mayor a 0")
    private Integer floorNumber;

    private String name;
    private String description;
}