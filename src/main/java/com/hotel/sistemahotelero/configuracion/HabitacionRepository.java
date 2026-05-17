package com.hotel.sistemahotelero.configuracion;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HabitacionRepository extends JpaRepository<Habitacion, Long> {

    // 🔍 Buscamos por el ID del hotel (Nombre estándar de Spring)
    List<Habitacion> findByHotelId(Long hotelId);

    // 📊 Contamos por el ID del hotel
    long countByHotelId(Long hotelId);
}