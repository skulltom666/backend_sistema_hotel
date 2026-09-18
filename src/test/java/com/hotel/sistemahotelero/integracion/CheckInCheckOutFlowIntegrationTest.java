package com.hotel.sistemahotelero.integracion;

import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.booking.repository.BookingRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.FloorRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.RoomRepository;
import com.hotel.sistemahotelero.modules.operacion.repository.MovimientoRepository;
import com.hotel.sistemahotelero.shared.enums.BookingStatus;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.persistence.Booking;
import com.hotel.sistemahotelero.shared.persistence.Floor;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.MovimientoHistorico;
import com.hotel.sistemahotelero.shared.persistence.Room;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integracion del core del negocio (check-in / check-out) usando
 * Spring Boot Test + MockMvc sobre HTTP y repositorios reales (H2).
 * Cubre interaccion entre modulos, seguridad JWT y transiciones de estado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Integracion - Flujo check-in / check-out y estado de habitaciones")
class CheckInCheckOutFlowIntegrationTest {

    private static final String TENANT = "20123456789";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private FloorRepository floorRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private MovimientoRepository movimientoRepository;

    private Hotel hotel;
    private Room room;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setRuc(TENANT);
        user.setEmail("flow@test.com");
        user.setPassword("secret");
        user.setNombre("Admin Flujo");
        user.setRol("ADMINISTRADOR");
        user.setSubscriptionPlan(SubscriptionPlan.EMPRESARIAL);
        user.setMaxRooms(100);
        user = userRepository.save(user);

        hotel = new Hotel();
        hotel.setTenantId(TENANT);
        hotel.setNombre("Hotel Flujo");
        hotel.setDireccion("Av. Flujo 1");
        hotel.setUser(user);
        hotel = hotelRepository.save(hotel);

        Floor floor = new Floor();
        floor.setTenantId(TENANT);
        floor.setFloorNumber(1);
        floor.setHotel(hotel);
        floor = floorRepository.save(floor);

        room = new Room();
        room.setTenantId(TENANT);
        room.setRoomNumber("101");
        room.setCategory("Estandar");
        room.setCapacity(2);
        room.setStatus(RoomStatus.DISPONIBLE);
        room.setFloor(floor);
        room.setHorasMinimas(6);
        room.setPrecioMinimo(new BigDecimal("50"));
        room.setPrecio12Horas(new BigDecimal("150"));
        room.setPrecio24Horas(new BigDecimal("250"));
        room.setPrecioHoraExtra(new BigDecimal("20"));
        room = roomRepository.save(room);
    }

    @Test
    @WithMockUser(username = "flow@test.com", roles = "ADMIN")
    @DisplayName("CP-I01: flujo completo check-in -> pre-checkout -> check-out con transiciones de estado")
    void flujoCompleto_checkInCheckOut_transicionaEstadoHabitacion() throws Exception {
        String checkIn = "{\"idHabitacion\":" + room.getId() + ","
                + "\"huesped\":{\"dni\":\"12345678\",\"nombres\":\"Carlos\",\"apellidos\":\"Quispe\","
                + "\"telefono\":\"987654321\",\"email\":\"c@test.com\"},"
                + "\"adelanto\":50,\"observacion\":\"prueba\"}";

        mockMvc.perform(post("/api/operaciones/checkin")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkIn))
                .andExpect(status().isCreated());

        Room ocupada = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(RoomStatus.OCUPADO, ocupada.getStatus(), "Tras el check-in la habitacion queda OCUPADA");

        List<Booking> activos = bookingRepository.findActiveByRoomAndTenant(room.getId(), TENANT);
        assertEquals(1, activos.size(), "Debe existir un booking activo");
        assertEquals(BookingStatus.CHECK_IN, activos.get(0).getStatus());
        assertTrue(activos.get(0).getCheckedIn());

        mockMvc.perform(get("/api/operaciones/" + room.getId() + "/pre-checkout")
                        .header("X-Tenant-ID", TENANT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAPagar").exists())
                .andExpect(jsonPath("$.saldoPendiente").exists())
                .andExpect(jsonPath("$.habitacion.numero").value("101"));

        mockMvc.perform(post("/api/operaciones/" + room.getId() + "/check-out")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"total\":90,\"metodoPago\":\"EFECTIVO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        Room sucia = roomRepository.findById(room.getId()).orElseThrow();
        assertEquals(RoomStatus.SUCIO, sucia.getStatus(), "Tras el check-out la habitacion queda SUCIO");

        Booking cerrado = bookingRepository.findByRoomAndTenant(room.getId(), TENANT).get(0);
        assertEquals(BookingStatus.CHECK_OUT, cerrado.getStatus());
        assertTrue(cerrado.getCheckedOut());
        assertEquals(90.0, cerrado.getTotalAmount());

        List<MovimientoHistorico> movimientos =
                movimientoRepository.findByHabitacionAndTenant(room.getId(), TENANT);
        assertTrue(movimientos.stream().anyMatch(m -> "OCUPADO".equals(m.getEstadoNuevo())),
                "Debe registrar el movimiento hacia OCUPADO");
        assertTrue(movimientos.stream().anyMatch(m -> "SUCIO".equals(m.getEstadoNuevo())),
                "Debe registrar el movimiento hacia SUCIO");
    }

    @Test
    @WithMockUser(username = "flow@test.com", roles = "ADMIN")
    @DisplayName("CP-I02: check-in sobre habitacion OCUPADA retorna 400")
    void checkIn_habitacionOcupada_retorna400() throws Exception {
        room.setStatus(RoomStatus.OCUPADO);
        roomRepository.save(room);

        String checkIn = "{\"idHabitacion\":" + room.getId() + ","
                + "\"huesped\":{\"dni\":\"12345678\",\"nombres\":\"Carlos\",\"apellidos\":\"Quispe\"}}";

        mockMvc.perform(post("/api/operaciones/checkin")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkIn))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "flow@test.com", roles = "ADMIN")
    @DisplayName("CP-I03: check-in de habitacion inexistente retorna 400")
    void checkIn_habitacionInexistente_retorna400() throws Exception {
        String checkIn = "{\"idHabitacion\":999999,"
                + "\"huesped\":{\"dni\":\"12345678\",\"nombres\":\"Carlos\",\"apellidos\":\"Quispe\"}}";

        mockMvc.perform(post("/api/operaciones/checkin")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkIn))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "flow@test.com", roles = "ADMIN")
    @DisplayName("CP-I04: interaccion entre modulos hotel/operacion al crear y listar habitaciones")
    void crearYListarHabitaciones_interaccionEntreModulos() throws Exception {
        String nueva = "{\"hotelId\":" + hotel.getId() + ",\"piso\":2,\"numero\":\"201\","
                + "\"limitePersonas\":2,\"horasMinimas\":5,\"precioMinimo\":40,"
                + "\"precio12Horas\":120,\"precio24Horas\":200,\"precioHoraExtra\":15}";

        mockMvc.perform(post("/api/habitaciones")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(nueva))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/habitaciones/hotel/" + hotel.getId())
                        .header("X-Tenant-ID", TENANT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].numero").exists());

        assertTrue(roomRepository.findByHotelAndTenant(hotel.getId(), TENANT).stream()
                        .anyMatch(r -> "201".equals(r.getRoomNumber())),
                "La habitacion nueva debe persistir y listarse por hotel");
    }

    @Test
    @WithMockUser(username = "flow@test.com", roles = "ADMIN")
    @DisplayName("CP-I05: check-in valida campos obligatorios (400 si falta idHabitacion)")
    void checkIn_sinIdHabitacion_retorna400() throws Exception {
        String checkIn = "{\"huesped\":{\"nombres\":\"Carlos\",\"apellidos\":\"Quispe\"}}";

        mockMvc.perform(post("/api/operaciones/checkin")
                        .with(csrf())
                        .header("X-Tenant-ID", TENANT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkIn))
                .andExpect(status().isBadRequest());
    }
}
