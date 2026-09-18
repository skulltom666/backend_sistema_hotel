package com.hotel.sistemahotelero.unitarias;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.huespedes.Huesped;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import com.hotel.sistemahotelero.operaciones.HabitacionService;
import com.hotel.sistemahotelero.operaciones.Operacion;
import com.hotel.sistemahotelero.operaciones.OperacionRepository;
import com.hotel.sistemahotelero.operaciones.OperacionesController;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstadoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperacionesControllerTest {

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
    private OperacionesController controller;

    private void inyectarDependencias() {
        ReflectionTestUtils.setField(controller, "habitacionService", habitacionService);
        ReflectionTestUtils.setField(controller, "huespedRepository", huespedRepository);
        ReflectionTestUtils.setField(controller, "habitacionRepository", habitacionRepository);
        ReflectionTestUtils.setField(controller, "operacionRepository", operacionRepository);
        ReflectionTestUtils.setField(controller, "historialRepository", historialRepository);
    }


    @Test
    @DisplayName("CP-U08: huesped que no existe devuelve 404")
    void buscarHuespedInexistentePorDni() {
        inyectarDependencias();
        when(huespedRepository.findByDni("99999999")).thenReturn(Optional.empty());

        ResponseEntity<?> respuesta = controller.buscarHuespedPorDni("99999999");

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    @DisplayName("CP-U09: Pre-checkout calcula horas, total y saldo pendiente")
    void preCheckoutCalculaTotales() {
        inyectarDependencias();
        Long idHabitacion = 1L;

        Habitacion habitacion = new Habitacion();
        habitacion.setId(idHabitacion);
        habitacion.setEstadoActual(EstadoHabitacion.OCUPADO);
        habitacion.setHorasMinimas(1);
        habitacion.setPrecioMinimo(50.0);
        habitacion.setPrecioHoraExtra(15.0);
        when(habitacionRepository.findById(idHabitacion)).thenReturn(Optional.of(habitacion));

        Operacion operacion = new Operacion();
        operacion.setHabitacion(habitacion);
        operacion.setAdelanto(10.0);
        operacion.setFechaIngreso(LocalDateTime.now().minusHours(2));
        operacion.setEstado("ACTIVO");
        when(operacionRepository.findAll()).thenReturn(List.of(operacion));

        @SuppressWarnings("unchecked")
        Map<String, Object> cuerpo = (Map<String, Object>) controller
                .obtenerDetalleCheckOut(idHabitacion)
                .getBody();

        assertNotNull(cuerpo);
        assertEquals(2L, cuerpo.get("horasTotales"));
        assertEquals(65.0, cuerpo.get("totalAPagar"));
        assertEquals(10.0, cuerpo.get("adelanto"));
        assertEquals(55.0, cuerpo.get("saldoPendiente"));
        assertEquals(idHabitacion, ((Habitacion) cuerpo.get("habitacion")).getId());
    }

}