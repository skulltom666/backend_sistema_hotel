package com.hotel.sistemahotelero.modules.hotel.repository;

import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.persistence.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;


import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query("SELECT r FROM Room r WHERE r.floor.hotel.tenantId = :tenantId AND r.enabled = true")
    List<Room> findAllByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT r FROM Room r WHERE r.id = :id AND r.floor.hotel.tenantId = :tenantId AND r.enabled = true")
    Optional<Room> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT r FROM Room r WHERE r.floor.id = :floorId AND r.floor.hotel.tenantId = :tenantId AND r.enabled = true")
    List<Room> findByFloorAndTenant(@Param("floorId") Long floorId, @Param("tenantId") String tenantId);

    @Query("SELECT r FROM Room r WHERE r.floor.hotel.id = :hotelId AND r.floor.hotel.tenantId = :tenantId AND r.enabled = true")
    List<Room> findByHotelAndTenant(@Param("hotelId") Long hotelId, @Param("tenantId") String tenantId);

    @Query("SELECT r FROM Room r WHERE r.floor.hotel.tenantId = :tenantId AND r.status = :status AND r.enabled = true")
    List<Room> findByStatusAndTenant(@Param("status") RoomStatus status, @Param("tenantId") String tenantId);

    @Query("SELECT r FROM Room r WHERE r.floor.hotel.id = :hotelId AND r.floor.hotel.tenantId = :tenantId AND r.status = :status AND r.enabled = true")
    List<Room> findByStatusAndHotelAndTenant(@Param("status") RoomStatus status,
                                             @Param("hotelId") Long hotelId,
                                             @Param("tenantId") String tenantId);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Room r " +
            "WHERE r.floor.id = :floorId AND r.roomNumber = :roomNumber AND r.floor.hotel.tenantId = :tenantId AND r.enabled = true")
    boolean existsByFloorAndRoomNumberAndTenant(@Param("floorId") Long floorId,
                                                @Param("roomNumber") String roomNumber,
                                                @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(r) FROM Room r WHERE r.floor.id = :floorId AND r.floor.hotel.tenantId = :tenantId AND r.enabled = true")
    long countByFloorAndTenant(@Param("floorId") Long floorId, @Param("tenantId") String tenantId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Room r " +
            "JOIN Booking b ON b.room.id = r.id " +
            "WHERE r.id = :roomId AND b.checkedOut = false AND b.enabled = true")
    boolean hasActiveBookings(@Param("roomId") Long roomId);

    @Modifying
    @Query("UPDATE Room r SET r.enabled = false WHERE r.id = :id AND r.floor.hotel.tenantId = :tenantId")
    void softDeleteByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT r FROM Room r WHERE r.floor.hotel.tenantId = :tenantId AND r.category = :category AND r.enabled = true")
    List<Room> findByCategoryAndTenant(@Param("category") String category, @Param("tenantId") String tenantId);

    @Query("SELECT DISTINCT r.category FROM Room r WHERE r.floor.hotel.tenantId = :tenantId AND r.enabled = true")
    List<String> findDistinctCategoriesByTenant(@Param("tenantId") String tenantId);
}
