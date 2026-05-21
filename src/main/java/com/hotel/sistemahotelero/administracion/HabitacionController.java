package com.hotel.sistemahotelero.administracion;

import com.hotel.sistemahotelero.configuracion.EstadoHabitacion;
import com.hotel.sistemahotelero.configuracion.Habitacion;
import com.hotel.sistemahotelero.configuracion.HabitacionRepository;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstado;
import com.hotel.sistemahotelero.trazabilidad.HistorialEstadoRepository; // 👈 IMPORTANTE: Añade este import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/habitaciones")
public class HabitacionController {

    @Autowired
    private HabitacionRepository habitacionRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired // 🚨 ESTE ES EL QUE FALTABA: Inyectamos el repositorio de trazabilidad
    private HistorialEstadoRepository historialRepository;

    // 1. LISTAR HABITACIONES FILTRADAS POR HOTEL
    @GetMapping("/hotel/{hotelId}")
    public List<Habitacion> listarPorHotel(@PathVariable Long hotelId) {
        return habitacionRepository.findByHotelId(hotelId);
    }

    // 2. CREAR NUEVA HABITACIÓN
    @PostMapping
    public ResponseEntity<?> crearHabitacion(@RequestBody Habitacion nuevaHabitacion) {
        Long idBuscado = nuevaHabitacion.getHotelIdRecibido();

        if (idBuscado == null || idBuscado <= 0) {
            return ResponseEntity.badRequest().body("ID de hotel no proporcionado o inválido");
        }

        Hotel hotel = hotelRepository.findById(idBuscado)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel no existe"));

        System.out.println("🏨 Creando habitación para hotelId=" + idBuscado);

        long cantidadActual = habitacionRepository.countByHotelId(hotel.getId());

        if (cantidadActual >= hotel.getLimiteHabitaciones()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Límite alcanzado para el plan " + hotel.getPlan());
        }

        nuevaHabitacion.setHotel(hotel);
        nuevaHabitacion.setEstadoActual(EstadoHabitacion.DISPONIBLE);

        Habitacion guardada = habitacionRepository.save(nuevaHabitacion);
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }

    // 3. ELIMINAR UNA HABITACIÓN
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarHabitacion(@PathVariable Long id) {
        try {
            habitacionRepository.deleteById(id);
            return ResponseEntity.ok("Habitación eliminada correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la habitación: " + e.getMessage());
        }
    }

    // 4. ACTUALIZAR ESTADO (Limpieza, Mantenimiento, etc.)
    @PostMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstadoHabitacion(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        try {
            String nuevoEstadoStr = payload.get("estado");
            EstadoHabitacion nuevoEstado = EstadoHabitacion.valueOf(nuevoEstadoStr);

            Habitacion habitacion = habitacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

            // Guardamos el estado anterior antes de cambiarlo
            String estadoAnterior = habitacion.getEstadoActual().toString();

            habitacion.setEstadoActual(nuevoEstado);
            habitacionRepository.save(habitacion);

            // 🚨 GUARDAMOS EN EL HISTORIAL PARA EL DASHBOARD
            HistorialEstado historial = new HistorialEstado();
            historial.setHabitacionId(habitacion.getId());
            historial.setEstadoAnterior(estadoAnterior);
            historial.setEstadoNuevo(nuevoEstadoStr);
            historial.setFechaHora(LocalDateTime.now());
            historial.setUsuarioEncargado("Personal Operativo");

            historialRepository.save(historial); // ✅ Ahora sí funcionará porque declaramos el repositorio arriba

            return ResponseEntity.ok("Estado actualizado y grabado en historial.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}