package com.hotel.sistemahotelero.trazabilidad;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/historial")
public class HistorialController {

    @Autowired
    private HistorialEstadoRepository historialRepository;

    // Dashboard: Trae todo el historial de la sede (Hotel)
    @GetMapping("/hotel/{hotelId}")
    public List<HistorialEstado> obtenerPorHotel(@PathVariable Long hotelId) {
        return historialRepository.findByHotelId(hotelId);
    }

    // Opcional: Trae el historial de una habitación específica
    @GetMapping("/habitacion/{habitacionId}")
    public List<HistorialEstado> obtenerPorHabitacion(@PathVariable Long habitacionId) {
        return historialRepository.findByHabitacionIdOrderByFechaHoraDesc(habitacionId);
    }
}
