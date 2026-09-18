package com.hotel.sistemahotelero.shared.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "movimientos_historicos")
@Data
public class MovimientoHistorico extends BaseEntity {
    private Long hotelId;
    private Long habitacionId;
    private String habitacionNumero;
    private String estadoAnterior;
    private String estadoNuevo;
    private String usuarioEncargado;
    private LocalDateTime fechaHora;
    private String observacion;
}