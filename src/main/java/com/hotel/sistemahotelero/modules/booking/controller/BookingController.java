package com.hotel.sistemahotelero.modules.booking.controller;

import com.hotel.sistemahotelero.modules.booking.dto.CheckInRequest;
import com.hotel.sistemahotelero.modules.booking.service.BookingService;
import com.hotel.sistemahotelero.shared.dto.ApiResponse;
import com.hotel.sistemahotelero.shared.enums.BookingStatus;
import com.hotel.sistemahotelero.shared.persistence.Booking;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    /**
     * Obtiene todas las reservas del tenant actual
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookings() {
        try {
            List<Booking> bookings = bookingService.getBookingsByTenant();
            return ResponseEntity.ok(ApiResponse.success("Reservas obtenidas exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error obteniendo reservas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene una reserva por ID
     */
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Booking>> getBookingById(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.getBookingById(bookingId);
            return ResponseEntity.ok(ApiResponse.success("Reserva obtenida exitosamente", booking));
        } catch (Exception e) {
            log.error("Error obteniendo reserva {}: {}", bookingId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Realiza check-in
     */
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Booking>> checkIn(@Valid @RequestBody CheckInRequest request) {
        try {
            Booking booking = bookingService.checkIn(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Check-in realizado exitosamente", booking));
        } catch (Exception e) {
            log.error("Error en check-in: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Realiza check-out
     */
    @PostMapping("/{bookingId}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Booking>> checkOut(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.checkOut(bookingId);
            return ResponseEntity.ok(ApiResponse.success("Check-out realizado exitosamente", booking));
        } catch (Exception e) {
            log.error("Error en check-out {}: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Cancela una reserva
     */
    @PostMapping("/{bookingId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(@PathVariable Long bookingId) {
        try {
            Booking booking = bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok(ApiResponse.success("Reserva cancelada exitosamente", booking));
        } catch (Exception e) {
            log.error("Error cancelando reserva {}: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Obtiene reservas activas
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> getActiveBookings() {
        try {
            List<Booking> bookings = bookingService.getActiveBookings();
            return ResponseEntity.ok(ApiResponse.success("Reservas activas obtenidas exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error obteniendo reservas activas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene check-ins actuales
     */
    @GetMapping("/current-checkins")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> getCurrentCheckIns() {
        try {
            List<Booking> bookings = bookingService.getCurrentCheckIns();
            return ResponseEntity.ok(ApiResponse.success("Check-ins actuales obtenidos exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error obteniendo check-ins actuales: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene reservas por habitación
     */
    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByRoom(@PathVariable Long roomId) {
        try {
            List<Booking> bookings = bookingService.getBookingsByRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success("Reservas de la habitación obtenidas exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error obteniendo reservas por habitación {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene reservas por estado
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByStatus(@PathVariable BookingStatus status) {
        try {
            List<Booking> bookings = bookingService.getBookingsByStatus(status);
            return ResponseEntity.ok(ApiResponse.success("Reservas por estado obtenidas exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error obteniendo reservas por estado {}: {}", status, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene reservas por rango de fechas
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Booking> bookings = bookingService.getBookingsByDateRange(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Reservas por rango de fechas obtenidas exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error obteniendo reservas por rango de fechas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene reservas pendientes de check-in
     */
    @GetMapping("/pending-checkins")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> getPendingCheckIns() {
        try {
            List<Booking> bookings = bookingService.getPendingCheckIns();
            return ResponseEntity.ok(ApiResponse.success("Reservas pendientes de check-in obtenidas exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error obteniendo reservas pendientes de check-in: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Actualiza información del huésped
     */
    @PatchMapping("/{bookingId}/guest")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Booking>> updateGuestInfo(
            @PathVariable Long bookingId,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) String guestDocument,
            @RequestParam(required = false) String guestEmail,
            @RequestParam(required = false) String guestPhone) {
        try {
            Booking booking = bookingService.updateGuestInfo(bookingId, guestName, guestDocument, guestEmail, guestPhone);
            return ResponseEntity.ok(ApiResponse.success("Información del huésped actualizada exitosamente", booking));
        } catch (Exception e) {
            log.error("Error actualizando información del huésped {}: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Conteo de reservas activas
     */
    @GetMapping("/count/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Long>> countActiveBookings() {
        try {
            long count = bookingService.countActiveBookings();
            return ResponseEntity.ok(ApiResponse.success("Conteo de reservas activas obtenido exitosamente", count));
        } catch (Exception e) {
            log.error("Error obteniendo conteo de reservas activas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Busca reservas por nombre del huésped
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Booking>>> searchByGuestName(@RequestParam String guestName) {
        try {
            List<Booking> bookings = bookingService.searchByGuestName(guestName);
            return ResponseEntity.ok(ApiResponse.success("Reservas encontradas exitosamente", bookings));
        } catch (Exception e) {
            log.error("Error buscando reservas por nombre {}: {}", guestName, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Elimina una reserva (soft delete)
     */
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(@PathVariable Long bookingId) {
        try {
            bookingService.deleteBooking(bookingId);
            return ResponseEntity.ok(ApiResponse.success("Reserva eliminada exitosamente", null));
        } catch (Exception e) {
            log.error("Error eliminando reserva {}: {}", bookingId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}