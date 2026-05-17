package com.hotel.sistemahotelero.ventas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {
    // Aquí luego podremos agregar métodos para reportes, como buscar ventas por fecha
}
