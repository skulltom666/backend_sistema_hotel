package com.hotel.sistemahotelero.modules.operacion.controller;

import com.hotel.sistemahotelero.modules.operacion.dto.CrearHabitacionRequest;
import com.hotel.sistemahotelero.modules.operacion.dto.EstadoRequest;
import com.hotel.sistemahotelero.modules.operacion.dto.HabitacionDTO;
import com.hotel.sistemahotelero.modules.operacion.service.OperacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habitaciones")
@RequiredArgsConstructor
@Slf4j
public class HabitacionController {

    private final OperacionService operacionService;

    @GetMapping("/hotel/{idHotel}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<HabitacionDTO>> habitacionesDeHotel(@PathVariable Long idHotel) {
        return ResponseEntity.ok(operacionService.habitacionesDeHotel(idHotel));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<HabitacionDTO> habitacionDetalle(@PathVariable Long id) {
        return ResponseEntity.ok(operacionService.habitacionDetalle(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<String> crearHabitacion(@Valid @RequestBody CrearHabitacionRequest request) {
        operacionService.crearHabitacion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Habitación guardada exitosamente");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<String> eliminarHabitacion(@PathVariable Long id) {
        operacionService.eliminarHabitacion(id);
        return ResponseEntity.ok("Habitación eliminada");
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<String> cambiarEstado(@PathVariable Long id, @RequestBody EstadoRequest request) {
        operacionService.cambiarEstado(id, request);
        return ResponseEntity.ok("Estado actualizado");
    }
}