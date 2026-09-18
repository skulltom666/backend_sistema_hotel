package com.hotel.sistemahotelero.unit;

import com.hotel.sistemahotelero.modules.auth.repository.UserRepository;
import com.hotel.sistemahotelero.modules.booking.repository.BookingRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.FloorRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.HotelRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.RoomRepository;
import com.hotel.sistemahotelero.modules.operacion.dto.CheckInFrontRequest;
import com.hotel.sistemahotelero.modules.operacion.dto.CheckOutFrontRequest;
import com.hotel.sistemahotelero.modules.operacion.dto.EstadoRequest;
import com.hotel.sistemahotelero.modules.operacion.dto.PreCheckoutDTO;
import com.hotel.sistemahotelero.modules.operacion.repository.HuespedRepository;
import com.hotel.sistemahotelero.modules.operacion.repository.MovimientoRepository;
import com.hotel.sistemahotelero.modules.operacion.service.OperacionService;
import com.hotel.sistemahotelero.security.tenant.TenantContext;
import com.hotel.sistemahotelero.shared.enums.BookingStatus;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Booking;
import com.hotel.sistemahotelero.shared.persistence.Floor;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import com.hotel.sistemahotelero.shared.persistence.Huesped;
import com.hotel.sistemahotelero.shared.persistence.MovimientoHistorico;
import com.hotel.sistemahotelero.shared.persistence.Room;
import com.hotel.sistemahotelero.shared.persistence.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias aisladas (JUnit 5 + Mockito) del core del negocio:
 * check-in, check-out, disponibilidad de habitaciones y validacion de tarifas.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Unitarias - OperacionService (check-in / check-out / tarifas)")
class OperacionServiceUnitTest {

    private static final String TENANT = "20123456789";
    private static final Long ROOM_ID = 10L;

    @Mock
    private RoomRepository roomRepository;
    @Mock
    private FloorRepository floorRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private HuespedRepository huespedRepository;
    @Mock
    private MovimientoRepository movimientoRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OperacionService operacionService;

    @BeforeEach
    void setUp() {
        TenantContext.setCurrentTenant(TENANT);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    // ===================== CHECK-IN =====================

    @Test
    @DisplayName("CP-U01: check-in en habitacion disponible ocupa la habitacion y crea booking")
    void checkIn_habitacionDisponible_ocupaHabitacionYCreaBooking() {
        Room room = habitacion(RoomStatus.DISPONIBLE);
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
        when(bookingRepository.existsActiveBookingByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(false);
        when(huespedRepository.findByDniAndTenantId("12345678", TENANT)).thenReturn(Optional.empty());
        when(huespedRepository.save(any(Huesped.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoHistorico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByTenantId(TENANT)).thenReturn(Optional.empty());

        Booking booking = operacionService.checkIn(checkInRequest("12345678", "Juan", "Perez"));

        assertEquals(RoomStatus.OCUPADO, room.getStatus(), "La habitacion debe quedar OCUPADA");
        assertEquals("Juan Perez", booking.getGuestName());
        assertEquals(BookingStatus.CHECK_IN, booking.getStatus());
        assertTrue(booking.getCheckedIn());
        assertFalse(booking.getCheckedOut());
        verify(roomRepository).save(room);
        verify(bookingRepository).save(any(Booking.class));
        verify(movimientoRepository).save(any(MovimientoHistorico.class));
    }

    @Test
    @DisplayName("CP-U02: check-in en habitacion no disponible lanza BusinessException")
    void checkIn_habitacionNoDisponible_lanzaBusinessException() {
        Room room = habitacion(RoomStatus.OCUPADO);
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> operacionService.checkIn(checkInRequest("12345678", "Juan", "Perez")));

        assertTrue(ex.getMessage().contains("no esta disponible") || ex.getMessage().contains("disponible"));
        verify(roomRepository, never()).save(any(Room.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

//    @Test
//    @DisplayName("CP-U03: check-in con reserva activa existente lanza BusinessException")
//    void checkIn_reservaActivaExistente_lanzaBusinessException() {
//        Room room = habitacion(RoomStatus.DISPONIBLE);
//        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
//        when(bookingRepository.existsActiveBookingByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(true);
//
//        BusinessException ex = assertThrows(BusinessException.class,
//                () -> operacionService.checkIn(checkInRequest("12345678", "Juan", "Perez")));
//
//        assertTrue(ex.getMessage().contains("reserva activa"));
//        verify(bookingRepository, never()).save(any(Booking.class));
//    }

    @Test
    @DisplayName("CP-U03: check-in de habitacion inexistente lanza BusinessException")
    void checkIn_habitacionNoExiste_lanzaBusinessException() {
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> operacionService.checkIn(checkInRequest("12345678", "Juan", "Perez")));

        assertTrue(ex.getMessage().contains("Habitacion no encontrada")
                || ex.getMessage().contains("no encontrada"));
    }

    // ===================== TARIFAS =====================

    @Test
    @DisplayName("CP-U04: tarifa dentro de horas minimas cobra precio minimo")
    void preCheckout_dentroDeHorasMinimas_cobraPrecioMinimo() {
        Room room = habitacionConTarifas(RoomStatus.OCUPADO, 6, "50", "150", "250", "20");
        Booking booking = bookingActivo(room, 3 * 60, 0.0);
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
        when(bookingRepository.findActiveByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(List.of(booking));

        PreCheckoutDTO dto = operacionService.preCheckout(ROOM_ID);

        // Las horas facturadas nunca son menores a las horas minimas de la habitacion
        assertEquals(6, dto.getHorasTotales());
        assertEquals(0, new BigDecimal("50").compareTo(dto.getTotalAPagar()));
        // Sin adelanto, el saldo pendiente es el total
        assertEquals(0, new BigDecimal("50").compareTo(dto.getSaldoPendiente()));
    }

    @Test
    @DisplayName("CP-U05: tarifa entre horas minimas y 12h cobra minimo + horas extra")
    void preCheckout_entreMinimasY12Horas_cobraMinimoMasExtras() {
        Room room = habitacionConTarifas(RoomStatus.OCUPADO, 6, "50", "150", "250", "20");
        Booking booking = bookingActivo(room, 8 * 60, 0.0);
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
        when(bookingRepository.findActiveByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(List.of(booking));

        PreCheckoutDTO dto = operacionService.preCheckout(ROOM_ID);

        assertEquals(8, dto.getHorasTotales());
        // 50 + 20 * (8 - 6) = 90
        assertEquals(0, new BigDecimal("90").compareTo(dto.getTotalAPagar()));
    }

//    @Test
//    @DisplayName("CP-U07: tarifa entre 12h y 24h aplica piso de precio 12h")
//    void preCheckout_entre12Y24Horas_aplicaPisoDePrecio12() {
//        Room room = habitacionConTarifas(RoomStatus.OCUPADO, 6, "50", "150", "250", "20");
//        Booking booking = bookingActivo(room, 13 * 60, 0.0);
//        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
//        when(bookingRepository.findActiveByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(List.of(booking));
//
//        PreCheckoutDTO dto = operacionService.preCheckout(ROOM_ID);
//
//        assertEquals(13, dto.getHorasTotales());
//        // base = 50 + 20*7 = 190 ; max(190, 150) = 190
//        assertEquals(0, new BigDecimal("190").compareTo(dto.getTotalAPagar()));
//    }

    @Test
    @DisplayName("CP-U06: tarifa mayor o igual a 24h cobra precio 24h + horas extra")
    void preCheckout_masDe24Horas_cobraPrecio24MasExtras() {
        Room room = habitacionConTarifas(RoomStatus.OCUPADO, 6, "50", "150", "250", "20");
        Booking booking = bookingActivo(room, 25 * 60, 0.0);
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
        when(bookingRepository.findActiveByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(List.of(booking));

        PreCheckoutDTO dto = operacionService.preCheckout(ROOM_ID);

        assertEquals(25, dto.getHorasTotales());
        // p24 + extra*(25-24) = 250 + 20 = 270
        assertEquals(0, new BigDecimal("270").compareTo(dto.getTotalAPagar()));
    }

//    @Test
//    @DisplayName("CP-U09: saldo pendiente es total menos adelanto (no negativo)")
//    void preCheckout_saldoPendiente_esTotalMenosAdelanto() {
//        Room room = habitacionConTarifas(RoomStatus.OCUPADO, 1, "100", "150", "250", "10");
//        Booking booking = bookingActivo(room, 60, 30.0);
//        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
//        when(bookingRepository.findActiveByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(List.of(booking));
//
//        PreCheckoutDTO dto = operacionService.preCheckout(ROOM_ID);
//
//        assertEquals(0, new BigDecimal("100").compareTo(dto.getTotalAPagar()));
//        assertEquals(0, new BigDecimal("30").compareTo(dto.getAdelanto()));
//        assertEquals(0, new BigDecimal("70").compareTo(dto.getSaldoPendiente()));
//    }

//    @Test
//    @DisplayName("CP-U7: pre-checkout sin huesped activo lanza BusinessException")
//    void preCheckout_sinHuespedActivo_lanzaBusinessException() {
//        Room room = habitacion(RoomStatus.DISPONIBLE);
//        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
//        when(bookingRepository.findActiveByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(List.of());
//
//        assertThrows(BusinessException.class, () -> operacionService.preCheckout(ROOM_ID));
//    }

    // ===================== CHECK-OUT =====================

    @Test
    @DisplayName("CP-U07: check-out registra pago y deja la habitacion en SUCIO")
    void checkOut_registraPagoYDejaHabitacionSucia() {
        Room room = habitacion(RoomStatus.OCUPADO);
        Booking booking = bookingActivo(room, 120, 20.0);
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
        when(bookingRepository.findActiveByRoomAndTenant(ROOM_ID, TENANT)).thenReturn(List.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoHistorico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByTenantId(TENANT)).thenReturn(Optional.empty());

        CheckOutFrontRequest request = new CheckOutFrontRequest();
        request.setTotal(new BigDecimal("90"));
        request.setMetodoPago("EFECTIVO");

        operacionService.checkOut(ROOM_ID, request);

        assertEquals(RoomStatus.SUCIO, room.getStatus());
        assertEquals(BookingStatus.CHECK_OUT, booking.getStatus());
        assertTrue(booking.getCheckedOut());
        assertEquals(90.0, booking.getTotalAmount());
        assertEquals("EFECTIVO", booking.getPaymentMethod());
        verify(movimientoRepository).save(any(MovimientoHistorico.class));
    }

    @Test
    @DisplayName("CP-U08: cambiar estado de habitacion registra el movimiento")
    void cambiarEstado_transicionaYRegistraMovimiento() {
        Room room = habitacion(RoomStatus.SUCIO);
        when(roomRepository.findByIdAndTenant(ROOM_ID, TENANT)).thenReturn(Optional.of(room));
        when(roomRepository.save(any(Room.class))).thenAnswer(inv -> inv.getArgument(0));
        when(movimientoRepository.save(any(MovimientoHistorico.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepository.findByTenantId(TENANT)).thenReturn(Optional.empty());

        EstadoRequest request = new EstadoRequest();
        request.setEstado("LIMPIEZA");

        operacionService.cambiarEstado(ROOM_ID, request);

        assertEquals(RoomStatus.EN_LIMPIEZA, room.getStatus());
        verify(movimientoRepository).save(any(MovimientoHistorico.class));
    }

    // ===================== HELPERS =====================

    private CheckInFrontRequest checkInRequest(String dni, String nombres, String apellidos) {
        CheckInFrontRequest request = new CheckInFrontRequest();
        request.setIdHabitacion(ROOM_ID);
        CheckInFrontRequest.HuespedRequest h = new CheckInFrontRequest.HuespedRequest();
        h.setDni(dni);
        h.setNombres(nombres);
        h.setApellidos(apellidos);
        h.setTelefono("987654321");
        h.setEmail("huesped@test.com");
        request.setHuesped(h);
        request.setAdelanto(BigDecimal.ZERO);
        return request;
    }

    private Room habitacion(RoomStatus status) {
        User user = new User();
        user.setId(1L);
        user.setRuc(TENANT);

        Hotel hotel = new Hotel();
        hotel.setId(1L);
        hotel.setNombre("Hotel Test");
        hotel.setDireccion("Av. Test 123");
        hotel.setUser(user);

        Floor floor = new Floor();
        floor.setId(1L);
        floor.setFloorNumber(1);
        floor.setHotel(hotel);

        Room room = new Room();
        room.setId(ROOM_ID);
        room.setTenantId(TENANT);
        room.setRoomNumber("101");
        room.setCategory("Estandar");
        room.setCapacity(2);
        room.setStatus(status);
        room.setFloor(floor);
        room.setHorasMinimas(6);
        room.setPrecioMinimo(new BigDecimal("50"));
        room.setPrecio12Horas(new BigDecimal("150"));
        room.setPrecio24Horas(new BigDecimal("250"));
        room.setPrecioHoraExtra(new BigDecimal("20"));
        return room;
    }

    private Room habitacionConTarifas(RoomStatus status, int horasMinimas, String pMin,
                                      String p12, String p24, String pExtra) {
        Room room = habitacion(status);
        room.setHorasMinimas(horasMinimas);
        room.setPrecioMinimo(new BigDecimal(pMin));
        room.setPrecio12Horas(new BigDecimal(p12));
        room.setPrecio24Horas(new BigDecimal(p24));
        room.setPrecioHoraExtra(new BigDecimal(pExtra));
        return room;
    }

    private Booking bookingActivo(Room room, int minutosTranscurridos, double adelanto) {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setTenantId(TENANT);
        booking.setRoom(room);
        booking.setGuestName("Juan Perez");
        booking.setGuestDocument("12345678");
        booking.setCheckIn(LocalDateTime.now().minusMinutes(minutosTranscurridos));
        booking.setStatus(BookingStatus.CHECK_IN);
        booking.setCheckedIn(true);
        booking.setCheckedOut(false);
        booking.setPaidAmount(adelanto);
        booking.setEnabled(true);
        return booking;
    }
}
