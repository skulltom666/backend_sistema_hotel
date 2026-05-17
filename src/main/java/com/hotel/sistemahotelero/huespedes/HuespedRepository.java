package com.hotel.sistemahotelero.huespedes;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// El "Long" aquí es porque la llave primaria (@Id) en Huesped.java es Long
public interface HuespedRepository extends JpaRepository<Huesped, Long> {

    // Aquí está el truco: Buscamos por la columna "dni" que es un String
    Optional<Huesped> findByDni(String dni);
}