package com.hotel.sistemahotelero.modules.operacion.dto;

import com.hotel.sistemahotelero.shared.persistence.Huesped;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HuespedDTO {
    private Long id;
    private String dni;
    private String nombre;
    private String apellido;
    private String telefono;
    private String email;

    public static HuespedDTO of(Huesped h) {
        return HuespedDTO.builder()
                .id(h.getId())
                .dni(h.getDni())
                .nombre(h.getNombre())
                .apellido(h.getApellido())
                .telefono(h.getTelefono())
                .email(h.getEmail())
                .build();
    }
}