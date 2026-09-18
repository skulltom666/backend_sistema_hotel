package com.hotel.sistemahotelero.modules.operacion.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class HabitacionDTO {
    private Long id;
    private String numero;
    private Integer piso;
    private String estadoActual;
    private Integer limitePersonas;
    private Integer camasSimples;
    private Integer camasDobles;
    private Integer horasMinimas;
    private BigDecimal precioMinimo;
    private BigDecimal precio12Horas;
    private BigDecimal precio24Horas;
    private BigDecimal precioHoraExtra;
    private String descripcion;
    private HuespedActualView huespedActual;

    @Data
    @Builder
    public static class HuespedActualView {
        private String nombre;
        private String apellidos;
        private String dni;
    }
}