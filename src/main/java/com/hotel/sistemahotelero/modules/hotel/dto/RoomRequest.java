package com.hotel.sistemahotelero.modules.hotel.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomRequest {
    @NotBlank(message = "El número de habitación es obligatorio")
    private String roomNumber;

    @NotBlank(message = "La categoría es obligatoria")
    private String category;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacity;

    @NotNull(message = "El precio por noche es obligatorio")
    @Min(value = 0, message = "El precio debe ser mayor o igual a 0")
    private BigDecimal pricePerNight;

    private String description;
}