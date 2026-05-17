package com.hotel.sistemahotelero.trazabilidad;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import org.springframework.context.ApplicationEvent;

public class EstadoHabitacionEvent extends ApplicationEvent {
    private final Long habitacionId; // Cambiar de Integer a Long
    private final EstadoHabitacion estadoAnterior;
    private final EstadoHabitacion estadoNuevo;

    public EstadoHabitacionEvent(Object source, Long habitacionId, EstadoHabitacion anterior, EstadoHabitacion nuevo) {
        super(source);
        this.habitacionId = habitacionId; // Ahora coinciden los tipos
        this.estadoAnterior = anterior;
        this.estadoNuevo = nuevo;
    }

    // Getters
    public Long getHabitacionId() { return habitacionId; }
    public EstadoHabitacion getEstadoAnterior() { return estadoAnterior; }
    public EstadoHabitacion getEstadoNuevo() { return estadoNuevo; }
}