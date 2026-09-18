package com.hotel.sistemahotelero.modules.operacion.repository;

import com.hotel.sistemahotelero.shared.persistence.MovimientoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<MovimientoHistorico, Long> {

    @Query("SELECT m FROM MovimientoHistorico m " +
            "WHERE m.hotelId = :hotelId AND m.tenantId = :tenantId AND m.enabled = true " +
            "ORDER BY m.fechaHora DESC")
    List<MovimientoHistorico> findByHotelAndTenant(@Param("hotelId") Long hotelId,
                                                   @Param("tenantId") String tenantId);

    @Query("SELECT m FROM MovimientoHistorico m " +
            "WHERE m.habitacionId = :habitacionId AND m.tenantId = :tenantId AND m.enabled = true " +
            "ORDER BY m.fechaHora DESC")
    List<MovimientoHistorico> findByHabitacionAndTenant(@Param("habitacionId") Long habitacionId,
                                                        @Param("tenantId") String tenantId);
}