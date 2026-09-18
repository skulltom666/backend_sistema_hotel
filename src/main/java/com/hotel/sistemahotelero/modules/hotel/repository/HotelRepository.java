package com.hotel.sistemahotelero.modules.hotel.repository;

import com.hotel.sistemahotelero.shared.persistence.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

    @Query("SELECT h FROM Hotel h WHERE h.tenantId = :tenantId AND h.enabled = true")
    List<Hotel> findAllByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT h FROM Hotel h WHERE h.id = :id AND h.tenantId = :tenantId AND h.enabled = true")
    Optional<Hotel> findByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT h FROM Hotel h WHERE h.tenantId = :tenantId AND h.user.id = :userId AND h.enabled = true")
    List<Hotel> findByTenantAndUser(@Param("tenantId") String tenantId, @Param("userId") Long userId);

    @Query("SELECT COUNT(h) FROM Hotel h WHERE h.tenantId = :tenantId AND h.enabled = true")
    long countByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT CASE WHEN COUNT(h) > 0 THEN true ELSE false END FROM Hotel h " +
            "WHERE h.id = :id AND h.tenantId = :tenantId AND h.enabled = true")
    boolean existsByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Modifying
    @Query("UPDATE Hotel h SET h.enabled = false WHERE h.id = :id AND h.tenantId = :tenantId")
    void softDeleteByIdAndTenant(@Param("id") Long id, @Param("tenantId") String tenantId);

    @Query("SELECT h FROM Hotel h LEFT JOIN FETCH h.floors f LEFT JOIN FETCH f.rooms r " +
            "WHERE h.tenantId = :tenantId AND h.enabled = true")
    List<Hotel> findAllWithDetailsByTenant(@Param("tenantId") String tenantId);

    @Query("SELECT h FROM Hotel h WHERE h.tenantId = :tenantId AND h.nombre LIKE %:nombre% AND h.enabled = true")
    List<Hotel> findByNombreContainingAndTenant(@Param("nombre") String nombre, @Param("tenantId") String tenantId);
}