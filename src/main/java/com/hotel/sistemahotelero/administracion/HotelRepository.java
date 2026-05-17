package com.hotel.sistemahotelero.administracion;

import com.hotel.sistemahotelero.seguridad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    // Consulta SQL directa (HQL): "Búscame los hoteles donde el dueño sea este usuario"
    @Query("SELECT h FROM Hotel h WHERE h.administrador = :admin")
    List<Hotel> buscarMisHotelesDefinitivo(@Param("admin") Usuario admin);

}
