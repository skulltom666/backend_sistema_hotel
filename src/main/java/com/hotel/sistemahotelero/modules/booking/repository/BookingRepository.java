package com.hotel.sistemahotelero.modules.booking.repository;

import com.hotel.sistemahotelero.shared.enums.BookingStatus;
import com.hotel.sistemahotelero.shared.persistence.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.enabled = true")
    List<Booking> findAllByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.tenantId = :tenantId AND b.enabled = true")
    Optional<Booking> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.tenantId = :tenantId AND b.enabled = true")
    List<Booking> findByRoomAndTenant(@Param("roomId") Long roomId, @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.tenantId = :tenantId AND b.checkedOut = false AND b.enabled = true")
    List<Booking> findActiveByRoomAndTenant(@Param("roomId") Long roomId, @Param("tenantId") String tenantId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.room.id = :roomId AND b.tenantId = :tenantId AND b.checkedOut = false AND b.enabled = true")
    boolean existsActiveBookingByRoomAndTenant(@Param("roomId") Long roomId, @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.status = :status AND b.enabled = true")
    List<Booking> findByStatusAndTenant(@Param("status") BookingStatus status, @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.status IN :statuses AND b.enabled = true")
    List<Booking> findByStatusInAndTenant(@Param("statuses") List<BookingStatus> statuses,
                                          @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.checkIn BETWEEN :startDate AND :endDate AND b.enabled = true")
    List<Booking> findByDateRangeAndTenant(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.checkedIn = true AND b.checkedOut = false AND b.enabled = true")
    List<Booking> findCurrentCheckIns(@Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.checkedIn = false AND b.checkedOut = false AND b.checkIn <= :now AND b.enabled = true")
    List<Booking> findPendingCheckIns(@Param("now") LocalDateTime now, @Param("tenantId") String tenantId);

    @Modifying
    @Query("UPDATE Booking b SET b.enabled = false WHERE b.id = :id AND b.tenantId = :tenantId")
    void softDeleteByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.tenantId = :tenantId AND b.status IN :statuses AND b.enabled = true")
    long countByStatusInAndTenant(@Param("statuses") List<BookingStatus> statuses,
                                  @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.guestName LIKE %:guestName% AND b.enabled = true")
    List<Booking> findByGuestNameContainingAndTenant(@Param("guestName") String guestName,
                                                     @Param("tenantId") String tenantId);

    @Query("SELECT b FROM Booking b WHERE b.tenantId = :tenantId AND b.guestDocument = :document AND b.enabled = true")
    List<Booking> findByGuestDocumentAndTenant(@Param("document") String document,
                                               @Param("tenantId") String tenantId);
}