package com.hotel.sistemahotelero.modules.booking.service;

import com.hotel.sistemahotelero.modules.booking.dto.CheckInRequest;
import com.hotel.sistemahotelero.modules.booking.repository.BookingRepository;
import com.hotel.sistemahotelero.modules.cleaning.repository.CleaningRepository;
import com.hotel.sistemahotelero.modules.hotel.repository.RoomRepository;
import com.hotel.sistemahotelero.security.tenant.TenantContext;
import com.hotel.sistemahotelero.shared.enums.BookingStatus;
import com.hotel.sistemahotelero.shared.enums.CleaningStatus;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Booking;
import com.hotel.sistemahotelero.shared.persistence.Cleaning;
import com.hotel.sistemahotelero.shared.persistence.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final CleaningRepository cleaningRepository;

    /**
     * Obtiene todas las reservas del tenant actual
     */
    public List<Booking> getBookingsByTenant() {
        String tenantId = TenantContext.getCurrentTenant();
        log.debug("Obteniendo reservas para tenant: {}", tenantId);
        return bookingRepository.findAllByTenant(tenantId);
    }

    /**
     * Obtiene una reserva por ID verificando que pertenezca al tenant actual
     */
    public Booking getBookingById(Long bookingId) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.findByIdAndTenant(bookingId, tenantId)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));
    }

    /**
     * Obtiene todas las reservas activas del tenant actual
     */
    public List<Booking> getActiveBookings() {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.findByStatusInAndTenant(
                List.of(BookingStatus.CONFIRMADA, BookingStatus.CHECK_IN),
                tenantId
        );
    }

    /**
     * Obtiene los check-ins actuales (huéspedes alojados)
     */
    public List<Booking> getCurrentCheckIns() {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.findCurrentCheckIns(tenantId);
    }

    /**
     * Obtiene las reservas de una habitación específica
     */
    public List<Booking> getBookingsByRoom(Long roomId) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.findByRoomAndTenant(roomId, tenantId);
    }

    /**
     * Realiza check-in de un huésped
     */
    @Transactional
    public Booking checkIn(CheckInRequest request) {
        String tenantId = TenantContext.getCurrentTenant();

        Room room = roomRepository.findByIdAndTenant(request.getRoomId(), tenantId)
                .orElseThrow(() -> new BusinessException("Habitación no encontrada"));

        if (room.getStatus() != RoomStatus.DISPONIBLE) {
            throw new BusinessException("La habitación no está disponible para check-in");
        }

        // Verificar si ya hay un booking activo
        boolean hasActiveBooking = bookingRepository.existsActiveBookingByRoomAndTenant(
                room.getId(), tenantId);
        if (hasActiveBooking) {
            throw new BusinessException("La habitación ya tiene una reserva activa");
        }

        Booking booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setRoom(room);
        booking.setGuestName(request.getGuestName());
        booking.setGuestDocument(request.getGuestDocument());
        booking.setGuestEmail(request.getGuestEmail());
        booking.setGuestPhone(request.getGuestPhone());
        booking.setCheckIn(LocalDateTime.now());
        booking.setCheckOut(request.getCheckOut());
        booking.setStatus(BookingStatus.CHECK_IN);
        booking.setCheckedIn(true);
        booking.setCheckedOut(false);
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setObservations(request.getObservations());
        booking.setSource(request.getSource());

        // Actualizar estado de la habitación
        room.setStatus(RoomStatus.OCUPADO);
        roomRepository.save(room);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Check-in realizado: room {}, guest {}, tenant {}",
                room.getRoomNumber(), request.getGuestName(), tenantId);

        return savedBooking;
    }

    /**
     * Realiza check-out de un huésped
     */
    @Transactional
    public Booking checkOut(Long bookingId) {
        String tenantId = TenantContext.getCurrentTenant();

        Booking booking = bookingRepository.findByIdAndTenant(bookingId, tenantId)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        if (booking.getCheckedOut()) {
            throw new BusinessException("El check-out ya fue realizado");
        }

        Room room = booking.getRoom();

        booking.setCheckedOut(true);
        booking.setStatus(BookingStatus.CHECK_OUT);
        booking.setCheckOut(LocalDateTime.now());

        // Actualizar habitación a limpieza
        room.setStatus(RoomStatus.LIMPIEZA);
        roomRepository.save(room);

        // Crear tarea de limpieza automática
        createCleaningTask(room, tenantId);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Check-out realizado: room {}, tenant {}",
                room.getRoomNumber(), tenantId);

        return savedBooking;
    }

    /**
     * Crea una tarea de limpieza automática después del check-out
     */
    private void createCleaningTask(Room room, String tenantId) {
        Cleaning cleaning = new Cleaning();
        cleaning.setTenantId(tenantId);
        cleaning.setRoom(room);
        cleaning.setScheduledDate(LocalDateTime.now());
        cleaning.setStatus(CleaningStatus.PENDIENTE);
        cleaning.setObservations("Limpieza requerida después de check-out");

        cleaningRepository.save(cleaning);
        log.info("Tarea de limpieza creada para habitación: {}", room.getRoomNumber());
    }

    /**
     * Cancela una reserva
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        String tenantId = TenantContext.getCurrentTenant();

        Booking booking = bookingRepository.findByIdAndTenant(bookingId, tenantId)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        if (booking.getCheckedIn() || booking.getCheckedOut()) {
            throw new BusinessException("No se puede cancelar una reserva con check-in o check-out realizado");
        }

        booking.setStatus(BookingStatus.CANCELADA);

        // Liberar la habitación si estaba ocupada
        Room room = booking.getRoom();
        if (room.getStatus() == RoomStatus.OCUPADO) {
            room.setStatus(RoomStatus.DISPONIBLE);
            roomRepository.save(room);
        }

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Reserva cancelada: {}, tenant {}", bookingId, tenantId);

        return savedBooking;
    }

    /**
     * Obtiene reservas por estado
     */
    public List<Booking> getBookingsByStatus(BookingStatus status) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.findByStatusAndTenant(status, tenantId);
    }

    /**
     * Obtiene reservas por rango de fechas
     */
    public List<Booking> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.findByDateRangeAndTenant(startDate, endDate, tenantId);
    }

    /**
     * Obtiene reservas pendientes de check-in
     */
    public List<Booking> getPendingCheckIns() {
        String tenantId = TenantContext.getCurrentTenant();
        LocalDateTime now = LocalDateTime.now();
        return bookingRepository.findPendingCheckIns(now, tenantId);
    }

    /**
     * Actualiza los datos de un huésped
     */
    @Transactional
    public Booking updateGuestInfo(Long bookingId, String guestName, String guestDocument,
                                   String guestEmail, String guestPhone) {
        String tenantId = TenantContext.getCurrentTenant();

        Booking booking = bookingRepository.findByIdAndTenant(bookingId, tenantId)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        if (booking.getCheckedOut()) {
            throw new BusinessException("No se puede modificar una reserva con check-out realizado");
        }

        if (guestName != null) booking.setGuestName(guestName);
        if (guestDocument != null) booking.setGuestDocument(guestDocument);
        if (guestEmail != null) booking.setGuestEmail(guestEmail);
        if (guestPhone != null) booking.setGuestPhone(guestPhone);

        return bookingRepository.save(booking);
    }

    /**
     * Elimina una reserva (soft delete)
     */
    @Transactional
    public void deleteBooking(Long bookingId) {
        String tenantId = TenantContext.getCurrentTenant();

        Booking booking = bookingRepository.findByIdAndTenant(bookingId, tenantId)
                .orElseThrow(() -> new BusinessException("Reserva no encontrada"));

        if (booking.getCheckedIn() && !booking.getCheckedOut()) {
            throw new BusinessException("No se puede eliminar una reserva con check-in activo");
        }

        bookingRepository.softDeleteByIdAndTenant(bookingId, tenantId);
        log.info("Reserva eliminada: {}, tenant {}", bookingId, tenantId);
    }

    /**
     * Conteo de reservas activas
     */
    public long countActiveBookings() {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.countByStatusInAndTenant(
                List.of(BookingStatus.CONFIRMADA, BookingStatus.CHECK_IN),
                tenantId
        );
    }

    /**
     * Busca reservas por nombre del huésped
     */
    public List<Booking> searchByGuestName(String guestName) {
        String tenantId = TenantContext.getCurrentTenant();
        return bookingRepository.findByGuestNameContainingAndTenant(guestName, tenantId);
    }
}