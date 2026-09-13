package com.hotel.sistemahotelero.IT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.util.Optional;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.huespedes.Huesped;
import com.hotel.sistemahotelero.huespedes.HuespedRepository;
import com.hotel.sistemahotelero.operaciones.Operacion;
import com.hotel.sistemahotelero.operaciones.OperacionRepository;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstado;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstadoRepository;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class SistemaHoteleroApplicationTests {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private HabitacionRepository habitacionRepository;

        @Autowired
        private HuespedRepository huespedRepository;

        @Autowired
        private OperacionRepository operacionRepository;

        @Autowired
        private HistorialEstadoRepository historialRepository;

        @Test
        void testDeberiaRegistrarCheckInCorrectamente() throws Exception {

                // cargar habitación
                Habitacion habitacion = new Habitacion();
                habitacion.setNumero("101");
                habitacion.setEstadoActual(EstadoHabitacion.DISPONIBLE);

                habitacion = habitacionRepository.save(habitacion);

                // json de entrada
                String json = """
                                {
                                    "idHabitacion": %d,
                                    "adelanto": 10.0,
                                    "huesped": {
                                        "dni": "87654321",
                                        "nombres": "Juan",
                                        "apellidos": "Perez",
                                        "telefono": "987654321"
                                    }
                                }
                                """.formatted(habitacion.getId());

                // prueba el endpoint
                mockMvc.perform(
                                post("/api/operaciones/checkin")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message")
                                                .value("Check-In registrado exitosamente."));

                // verifica los datos creados
                Optional<Huesped> huesped = huespedRepository.findByDni("87654321");

                assertTrue(huesped.isPresent());

                List<Operacion> operaciones = operacionRepository.findAll();

                assertEquals(1, operaciones.size());

                Operacion operacion = operaciones.get(0);

                assertEquals(
                                habitacion.getId(),
                                operacion.getHabitacion().getId());

                assertEquals(
                                "87654321",
                                operacion.getHuesped().getDni());

                assertEquals(10.0, operacion.getAdelanto());

                assertEquals("ACTIVO", operacion.getEstado());

                assertNotNull(operacion.getFechaIngreso());

                List<HistorialEstado> historiales = historialRepository.findAll();

                assertEquals(1, historiales.size());

                HistorialEstado historial = historiales.get(0);

                assertEquals(
                                habitacion.getId(),
                                historial.getHabitacionId());

                assertEquals(
                                "DISPONIBLE",
                                historial.getEstadoAnterior());

                assertEquals(
                                "OCUPADO",
                                historial.getEstadoNuevo());

                assertNotNull(historial.getFechaHora());

                Habitacion habitacionActualizada = habitacionRepository
                                .findById(habitacion.getId())
                                .orElseThrow();

                assertEquals(
                                EstadoHabitacion.OCUPADO,
                                habitacionActualizada.getEstadoActual());

                assertEquals(
                                huesped.get().getId(),
                                habitacionActualizada
                                                .getHuespedActual()
                                                .getId());

        }

}
