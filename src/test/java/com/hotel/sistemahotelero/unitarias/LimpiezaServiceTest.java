package com.hotel.sistemahotelero.unitarias;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.limpieza.LimpiezaService;
import com.hotel.sistemahotelero.trazabilidad.EstadoHabitacionEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LimpiezaServiceTest {

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LimpiezaService limpiezaService;

    @Test
    @DisplayName("CP-U04: Inicio de limpieza")
    void testIniciarLimpieza() {
        long idHabitacion = 1l;

        Habitacion habitacion = new Habitacion();
        habitacion.setId(idHabitacion);
        habitacion.setEstadoActual(EstadoHabitacion.SUCIO);

        when(habitacionRepository.findById(idHabitacion)).thenReturn(Optional.of(habitacion));
        when(habitacionRepository.save(habitacion)).thenReturn(habitacion);

        Habitacion resultado = limpiezaService.iniciarLimpieza(idHabitacion);

        assertEquals(EstadoHabitacion.EN_LIMPIEZA, resultado.getEstadoActual());

        verify(habitacionRepository).findById(idHabitacion);
        verify(habitacionRepository).save(habitacion);

        verify(eventPublisher).publishEvent(any(EstadoHabitacionEvent.class));
    }

    @Test
    @DisplayName("CP-U05: Limpieza invalida")
    void testLimpiezaInvalida() {
        long idHabitacion = 1l;

        Habitacion habitacion = new Habitacion();
        habitacion.setId(idHabitacion);
        habitacion.setEstadoActual(EstadoHabitacion.DISPONIBLE);

        when(habitacionRepository.findById(idHabitacion)).thenReturn(Optional.of(habitacion));

        assertThrows(IllegalStateException.class, () -> {
            limpiezaService.finalizarLimpieza(idHabitacion);
        });

        verify(habitacionRepository).findById(idHabitacion);

        verify(habitacionRepository, never()).save(any());

    }

    @Test
    @DisplayName("CP-U06: Fin de limpieza")
    void testFinalizarLimpieza() {
        long idHabitacion = 1l;

        Habitacion habitacion = new Habitacion();
        habitacion.setId(idHabitacion);
        habitacion.setEstadoActual(EstadoHabitacion.EN_LIMPIEZA);

        when(habitacionRepository.findById(idHabitacion)).thenReturn(Optional.of(habitacion));
        when(habitacionRepository.save(habitacion)).thenReturn(habitacion);

        Habitacion resultado = limpiezaService.finalizarLimpieza(idHabitacion);

        assertEquals(EstadoHabitacion.DISPONIBLE, resultado.getEstadoActual());

        verify(habitacionRepository).findById(idHabitacion);
        verify(habitacionRepository).save(habitacion);

        verify(eventPublisher).publishEvent(any(EstadoHabitacionEvent.class));
    }
}
