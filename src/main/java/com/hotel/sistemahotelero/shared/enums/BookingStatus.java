package com.hotel.sistemahotelero.shared.enums;


public enum BookingStatus {
    CONFIRMADA("Confirmada"),
    CHECK_IN("Check-in Realizado"),
    CHECK_OUT("Check-out Realizado"),
    CANCELADA("Cancelada");

    private final String descripcion;

    BookingStatus(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
