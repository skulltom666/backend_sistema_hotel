package com.hotel.sistemahotelero.unitarias;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.trazabilidad.EstadoHabitacionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EstadoHabitacionEventTest {

    @Test
    @DisplayName("CP-U15: Se guarda los datos del cambio de estado")
    void elEventoGuardaLosDatosDelCambioDeEstado() {
        EstadoHabitacionEvent evento = new EstadoHabitacionEvent(
                this, 5L, EstadoHabitacion.DISPONIBLE, EstadoHabitacion.OCUPADO);

        assertEquals(5L, evento.getHabitacionId());
        assertEquals(EstadoHabitacion.DISPONIBLE, evento.getEstadoAnterior());
        assertEquals(EstadoHabitacion.OCUPADO, evento.getEstadoNuevo());
        assertNotNull(evento.getSource());
        assertEquals(this, evento.getSource());
    }
}