package com.hotel.sistemahotelero.modules.operacion.dto;

import lombok.Data;

@Data
public class VincularRequest {
    private String dni;
    private String nombre;
    private String apellido;
    private String telefono;
}