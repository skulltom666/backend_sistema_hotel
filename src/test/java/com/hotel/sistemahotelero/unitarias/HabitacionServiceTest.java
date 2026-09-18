package com.hotel.sistemahotelero.unitarias;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.huespedes.Huesped;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import com.hotel.sistemahotelero.operaciones.CheckInRequest;
import com.hotel.sistemahotelero.operaciones.HabitacionService;
import com.hotel.sistemahotelero.operaciones.HuespedDTO;
import com.hotel.sistemahotelero.seguridad.UsuarioRepository;
import com.hotel.sistemahotelero.trazabilidad.EstadoHabitacionEvent;
import com.hotel.sistemahotelero.ventas.Venta;
import com.hotel.sistemahotelero.ventas.VentaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HabitacionServiceTest {

        @Mock
        private HuespedRepository huespedRepository;

        @Mock
        private HabitacionRepository habitacionRepository;

        @Mock
        private ApplicationEventPublisher eventPublisher;

        @Mock
        private VentaRepository ventaRepository;

        @Mock
        private UsuarioRepository usuarioRepository;

        @InjectMocks
        private HabitacionService habitacionService;


        @Test
        @DisplayName("CP-U02: Check-out valido")
        void checkOutValido() {
                long idHabitacion = 1L;
                long idHuesped = 1L;
                Huesped huesped = new Huesped();
                huesped.setId(idHuesped);
                Habitacion habitacionInicio = new Habitacion();
                habitacionInicio.setId(idHabitacion);
                habitacionInicio.setHuespedActual(huesped);
                habitacionInicio.setEstadoActual(EstadoHabitacion.OCUPADO);
                habitacionInicio.setFechaCheckIn(LocalDateTime.now());
                when(habitacionRepository.findById(idHabitacion)).thenReturn(Optional.of(habitacionInicio));
                when(habitacionRepository.save(habitacionInicio)).thenReturn(habitacionInicio);
                // prueba
                Habitacion resultado = habitacionService.realizarCheckOut(idHabitacion, 50.0, "YAPE", "2h");
                assertEquals(EstadoHabitacion.SUCIO, resultado.getEstadoActual());
                assertNull(resultado.getHuespedActual());
                assertNull(resultado.getFechaCheckIn());
                verify(habitacionRepository).findById(idHabitacion);
                verify(ventaRepository).save(any(Venta.class));
                verify(habitacionRepository).save(habitacionInicio);
                verify(eventPublisher).publishEvent(any(EstadoHabitacionEvent.class));
        }

        @Test
        @DisplayName("CP-U03: Check-out invalido")
        void checkOutInvalido() {
                long idHabitacion = 1L;
                long idHuesped = 1L;

                Huesped huesped = new Huesped();
                huesped.setId(idHuesped);

                Habitacion habitacion = new Habitacion();
                habitacion.setId(idHabitacion);
                habitacion.setHuespedActual(huesped);
                habitacion.setEstadoActual(EstadoHabitacion.DISPONIBLE);

                when(habitacionRepository.findById(idHabitacion))
                                .thenReturn(Optional.of(habitacion));

                assertThrows(
                                IllegalStateException.class,
                                () -> habitacionService.realizarCheckOut(
                                                idHabitacion,
                                                250.0,
                                                "EFECTIVO",
                                                "2h"));

                verify(ventaRepository, never())
                                .save(any());

                verify(habitacionRepository, never())
                                .save(any());

                verify(eventPublisher, never())
                                .publishEvent(any());

        }
}
