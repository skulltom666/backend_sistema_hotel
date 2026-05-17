package com.hotel.sistemahotelero.operaciones;

/**
 * DTO para capturar la solicitud de Check-In desde Angular
 */
public class CheckInRequest {
    private Long idHabitacion;
    private HuespedDTO huesped;
    private Double adelanto;
    private String observacion;

    // Constructor vacío (Necesario para que Jackson convierta el JSON)
    public CheckInRequest() {}

    // Getters y Setters
    public Long getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(Long idHabitacion) { this.idHabitacion = idHabitacion; }

    public HuespedDTO getHuesped() { return huesped; }
    public void setHuesped(HuespedDTO huesped) { this.huesped = huesped; }

    public Double getAdelanto() { return adelanto; }
    public void setAdelanto(Double adelanto) { this.adelanto = adelanto; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}