package com.hotel.sistemahotelero.modules.operacion.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PerfilDTO {
    private String email;
    private String nombreUsuario;
    private String dni;
    private String nombres;
    private String apellidos;
    private String telefono;
}