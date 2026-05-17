package com.hotel.sistemahotelero.seguridad;

public class LoginRequest {

    private String email;
    private String password;

    // Constructor vacío exigido por Spring Boot
    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // --- Getters y Setters ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
