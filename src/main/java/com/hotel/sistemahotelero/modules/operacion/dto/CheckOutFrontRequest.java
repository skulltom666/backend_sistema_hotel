package com.hotel.sistemahotelero.modules.operacion.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckOutFrontRequest {
    private BigDecimal total;
    private String metodoPago;
    private String tiempoUso;
}