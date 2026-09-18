package com.hotel.sistemahotelero.modules.operacion.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PreCheckoutDTO {
    private HabitacionView habitacion;
    private HuespedView huesped;
    private LocalDateTime fechaIngreso;
    private LocalDateTime fechaSalida;
    private Integer horasTotales;
    private BigDecimal totalAPagar;
    private BigDecimal adelanto;
    private BigDecimal saldoPendiente;

    @Data
    @Builder
    public static class HabitacionView {
        private Long id;
        private String numero;
        private Integer piso;
        private HotelView hotel;
    }

    @Data
    @Builder
    public static class HotelView {
        private String nombre;
        private String ruc;
        private String direccion;
    }

    @Data
    @Builder
    public static class HuespedView {
        private String nombre;
        private String apellido;
        private String dni;
    }
}