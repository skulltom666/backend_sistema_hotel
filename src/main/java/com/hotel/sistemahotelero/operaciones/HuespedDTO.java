package com.hotel.sistemahotelero.operaciones;

/**
 * DTO con los datos básicos del huésped que vienen del formulario
 */
public class HuespedDTO {
    private String dni;
    private String nombres;
    private String apellidos;
    private String telefono;
    private String email;

    public HuespedDTO() {}

    // Getters y Setters
    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}