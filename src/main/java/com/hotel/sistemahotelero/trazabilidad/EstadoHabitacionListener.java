package com.hotel.sistemahotelero.trazabilidad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class EstadoHabitacionListener {

    @Autowired
    private HistorialEstadoRepository historialRepository;

    @EventListener
    public void alCambiarEstado(EstadoHabitacionEvent evento) {
        HistorialEstado registro = new HistorialEstado();

        // IMPORTANTE: habitacionId ahora viene como Long del evento
        registro.setHabitacionId(evento.getHabitacionId());
        registro.setEstadoAnterior(evento.getEstadoAnterior().toString());
        registro.setEstadoNuevo(evento.getEstadoNuevo().toString());
        registro.setFechaHora(LocalDateTime.now());

        // Si tienes un campo para el usuario encargado, lo seteamos aquí
        registro.setUsuarioEncargado("Recepcionista");

        historialRepository.save(registro);
    }
}