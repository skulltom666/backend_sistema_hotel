package com.hotel.sistemahotelero.modules.hotel.repository;

import com.hotel.sistemahotelero.shared.persistence.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    @Query("SELECT f FROM Floor f WHERE f.hotel.tenantId = :tenantId")
    List<Floor> findAllByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT f FROM Floor f WHERE f.id = :id AND f.hotel.tenantId = :tenantId")
    Optional<Floor> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT f FROM Floor f WHERE f.hotel.id = :hotelId AND f.hotel.tenantId = :tenantId")
    List<Floor> findByHotelAndTenant(@Param("hotelId") Long hotelId, @Param("tenantId") String tenantId);

    @Query("SELECT COUNT(f) FROM Floor f WHERE f.hotel.tenantId = :tenantId")
    long countByTenant(@Param("tenantId") String tenantId);
}