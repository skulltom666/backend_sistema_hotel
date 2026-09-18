package com.hotel.sistemahotelero.shared.enums;

public enum RoomStatus {
    DISPONIBLE("Disponible"),
    OCUPADO("Ocupado"),
    SUCIO("Sucio"),
    EN_LIMPIEZA("En Limpieza"),
    MANTENIMIENTO("En Mantenimiento"),
    LIMPIEZA("En Limpieza"),
    RESERVADO("Reservado");

    private final String descripcion;

    RoomStatus(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}