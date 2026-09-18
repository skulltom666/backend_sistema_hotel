package com.hotel.sistemahotelero.modules.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String nombre;
    private String rol;
    private List<HotelDTO> hoteles = new ArrayList<>();
    private String message;
    private boolean success;
}