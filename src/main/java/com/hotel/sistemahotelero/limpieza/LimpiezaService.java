package com.hotel.sistemahotelero.limpieza;

import com.hotel.sistemahotelero.configuracion.*;
import com.hotel.sistemahotelero.trazabilidad.EstadoHabitacionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class LimpiezaService {

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher; // Publicador de eventos de Spring

    public Habitacion iniciarLimpieza(Long habitacionId) { // Cambiado a Long
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new RuntimeException("Error: Habitación no encontrada"));

        if (habitacion.getEstadoActual() != EstadoHabitacion.SUCIO) {
            throw new IllegalStateException("Logística bloqueada: Solo se puede limpiar una habitación SUCIA.");
        }

        EstadoHabitacion estadoAnterior = habitacion.getEstadoActual();
        habitacion.setEstadoActual(EstadoHabitacion.EN_LIMPIEZA);
        Habitacion guardada = habitacionRepository.save(habitacion);

        // Notificamos el cambio al sistema mediante un evento
        eventPublisher.publishEvent(new EstadoHabitacionEvent(this, habitacionId, estadoAnterior, EstadoHabitacion.EN_LIMPIEZA));

        return guardada;
    }

    public Habitacion finalizarLimpieza(Long habitacionId) { // Cambiado a Long
        Habitacion habitacion = habitacionRepository.findById(habitacionId)
                .orElseThrow(() -> new RuntimeException("Error: Habitación no encontrada"));

        if (habitacion.getEstadoActual() != EstadoHabitacion.EN_LIMPIEZA) {
            throw new IllegalStateException("Logística bloqueada: La habitación no está en proceso de limpieza.");
        }

        EstadoHabitacion estadoAnterior = habitacion.getEstadoActual();
        habitacion.setEstadoActual(EstadoHabitacion.DISPONIBLE);
        Habitacion guardada = habitacionRepository.save(habitacion);

        // Notificamos el cambio al sistema mediante un evento
        eventPublisher.publishEvent(new EstadoHabitacionEvent(this, habitacionId, estadoAnterior, EstadoHabitacion.DISPONIBLE));

        return guardada;
    }
}
