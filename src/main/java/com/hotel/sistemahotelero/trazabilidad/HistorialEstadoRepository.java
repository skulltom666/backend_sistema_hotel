package com.hotel.sistemahotelero.trazabilidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {

    // Método para una sola habitación (usando tu habitacionId)
    List<HistorialEstado> findByHabitacionIdOrderByFechaHoraDesc(Long habitacionId);

    // 🚨 LA SOLUCIÓN PARA EL DASHBOARD:
    // Hacemos un "puente" manual hacia la tabla de Habitaciones para filtrar por Hotel
    @Query("SELECT h FROM HistorialEstado h WHERE h.habitacionId IN " +
            "(SELECT hab.id FROM Habitacion hab WHERE hab.hotel.id = :hotelId) " +
            "ORDER BY h.fechaHora DESC")
    List<HistorialEstado> findByHotelId(@Param("hotelId") Long hotelId);
}