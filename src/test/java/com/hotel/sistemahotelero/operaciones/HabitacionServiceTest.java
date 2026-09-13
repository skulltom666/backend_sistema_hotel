package com.hotel.sistemahotelero.operaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.huespedes.Huesped;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import com.hotel.sistemahotelero.seguridad.UsuarioRepository;
import com.hotel.sistemahotelero.trazabilidad.EstadoHabitacionEvent;
import com.hotel.sistemahotelero.ventas.Venta;
import com.hotel.sistemahotelero.ventas.VentaRepository;

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
        void chackInValido() {

                // Datos precargados
                Long idHabitacion = 1L;
                String dni = "87654321";

                Habitacion habitacion = new Habitacion();
                habitacion.setId(idHabitacion);
                habitacion.setEstadoActual(EstadoHabitacion.DISPONIBLE);

                Huesped huesped = new Huesped();

                CheckInRequest checkInRequest = new CheckInRequest();
                checkInRequest.setIdHabitacion(idHabitacion);

                HuespedDTO huespedDTO = new HuespedDTO();
                huespedDTO.setDni(dni);
                huespedDTO.setNombres("Juan");
                huespedDTO.setApellidos("Perez");
                huespedDTO.setTelefono("999999999");

                checkInRequest.setHuesped(huespedDTO);

                // proceso de pruebas
                when(habitacionRepository.findById(idHabitacion)).thenReturn(Optional.of(habitacion));

                when(huespedRepository.findByDni(dni)).thenReturn(Optional.of(huesped));

                when(usuarioRepository.findByDni(dni)).thenReturn(Optional.empty());

                when(huespedRepository.save(huesped)).thenReturn(huesped);

                when(habitacionRepository.save(habitacion)).thenReturn(habitacion);

                // pruebas del servicio
                Habitacion resultado = habitacionService.procesarCheckInCompleto(checkInRequest);

                assertEquals(EstadoHabitacion.OCUPADO, resultado.getEstadoActual());

                assertEquals(huesped, resultado.getHuespedActual());

                assertNotNull(resultado.getFechaCheckIn());

                verify(habitacionRepository).findById(idHabitacion);
                verify(huespedRepository).findByDni(dni);
                verify(huespedRepository).save(huesped);
                verify(habitacionRepository).save(habitacion);

                verify(eventPublisher).publishEvent(any(EstadoHabitacionEvent.class));

        }

        @Test
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
