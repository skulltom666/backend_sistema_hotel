package com.hotel.sistemahotelero.operaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstadoRepository;

@ExtendWith(MockitoExtension.class)
public class OperacionesControllerTest {

    @Mock
    private HabitacionService habitacionService;

    @Mock
    private HuespedRepository huespedRepository;

    @Mock
    private HabitacionRepository habitacionRepository;

    @Mock
    private OperacionRepository operacionRepository;

    @Mock
    private HistorialEstadoRepository historialRepository;

    @InjectMocks
    private OperacionesController operacionesController;

    @Test
    void testBuscarHuespedPorDni() {

    }

    @Test
    void testConfirmarCheckIn() {

    }

    @Test
    void testObtenerDetalleCheckOut() {
        long idhabitacion = 1L;

        Habitacion habitacion = new Habitacion();
        habitacion.setId(idhabitacion);

        Operacion operacionActiva = new Operacion();
        operacionActiva.setHabitacion(habitacion);
        operacionActiva.setFechaIngreso(LocalDateTime.now());
        operacionActiva.setAdelanto(0.0);
        operacionActiva.setEstado("ACTIVO");

        List<Operacion> operacions = new ArrayList<>();
        operacions.add(operacionActiva);
        for (long i = 2; i <= 5; i++) {
            Operacion operacion = new Operacion();
            Habitacion habitacionint = new Habitacion();
            habitacionint.setId(i);
            operacion.setHabitacion(habitacionint);
            operacions.add(operacion);
        }

        when(habitacionRepository.findById(idhabitacion)).thenReturn(Optional.of(habitacion));
        when(operacionRepository.findAll()).thenReturn(operacions);

        ResponseEntity<?> response = operacionesController.obtenerDetalleCheckOut(idhabitacion);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(habitacionRepository).findById(idhabitacion);
        verify(operacionRepository).findAll();

    }

    @Test
    void testRealizarCheckOut() {

    }
}
