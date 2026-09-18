package com.hotel.sistemahotelero.modules.operacion.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckInFrontRequest {
    @NotNull(message = "El idHabitacion es obligatorio")
    private Long idHabitacion;

    @NotNull(message = "Los datos del huésped son obligatorios")
    private HuespedRequest huesped;

    private BigDecimal adelanto = BigDecimal.ZERO;
    private String observacion;

    @Data
    public static class HuespedRequest {
        private String dni;
        private String nombres;
        private String apellidos;
        private String telefono;
        private String email;
    }
}