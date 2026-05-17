package com.hotel.sistemahotelero.operaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperacionRepository extends JpaRepository<Operacion, Long> {
    // Aquí podrías agregar métodos de búsqueda personalizados después
}