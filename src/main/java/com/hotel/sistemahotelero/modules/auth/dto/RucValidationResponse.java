package com.hotel.sistemahotelero.modules.auth.dto;

import lombok.Data;

import java.util.List;

@Data
public class RucValidationResponse {
    private String ruc;
    private String razonSocial;
    private String nombreComercial;
    private List<String> telefonos;
    private String tipo;
    private String estado;
    private String condicion;
    private String direccion;
    private String departamento;
    private String provincia;
    private String distrito;
    private String fechaInscripcion;
    private String ubigeo;
    private String capital;
}