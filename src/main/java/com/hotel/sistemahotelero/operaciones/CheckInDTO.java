package com.hotel.sistemahotelero.operaciones;

import com.hotel.sistemahotelero.huespedes.Huesped;

public class CheckInDTO {
    private Long idHabitacion;
    private Huesped huesped;
    private Double adelanto;
    private String observacion;

    // Getters y Setters
    public Long getIdHabitacion() { return idHabitacion; }
    public void setIdHabitacion(Long idHabitacion) { this.idHabitacion = idHabitacion; }

    public Huesped getHuesped() { return huesped; }
    public void setHuesped(Huesped huesped) { this.huesped = huesped; }

    public Double getAdelanto() { return adelanto; }
    public void setAdelanto(Double adelanto) { this.adelanto = adelanto; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}