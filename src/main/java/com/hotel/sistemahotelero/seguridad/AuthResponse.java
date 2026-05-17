package com.hotel.sistemahotelero.seguridad;

import java.util.List;

public class AuthResponse {

    private String token;
    private String nombre;
    private String rol;
    // 🚨 CAMBIO: Ahora devolvemos la lista de sedes a las que el usuario tiene acceso
    private List<HotelDTO> hoteles;

    // Constructor vacío
    public AuthResponse() {}

    // Constructor completo
    public AuthResponse(String token, String nombre, String rol, List<HotelDTO> hoteles) {
        this.token = token;
        this.nombre = nombre;
        this.rol = rol;
        this.hoteles = hoteles;
    }

    // --- Getters y Setters ---
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public List<HotelDTO> getHoteles() { return hoteles; }
    public void setHoteles(List<HotelDTO> hoteles) { this.hoteles = hoteles; }
}