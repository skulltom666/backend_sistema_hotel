package com.hotel.sistemahotelero.modules.operacion.controller;

import com.hotel.sistemahotelero.modules.operacion.dto.*;
import com.hotel.sistemahotelero.modules.operacion.service.OperacionService;
import com.hotel.sistemahotelero.shared.exceptions.BusinessException;
import com.hotel.sistemahotelero.shared.persistence.Huesped;
import com.hotel.sistemahotelero.shared.persistence.MovimientoHistorico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class OperacionController {

    private final OperacionService operacionService;

    // ==================== HUÉSPEDES ====================

    @GetMapping({"/operaciones/huesped/{documento}", "/huespedes/buscar/{documento}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<HuespedDTO> buscarHuesped(@PathVariable String documento) {
        return operacionService.buscarHuesped(documento)
                .map(huesped -> ResponseEntity.ok(HuespedDTO.of(huesped)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/huespedes")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<HuespedDTO> guardarHuesped(@RequestBody HuespedDTO request) {
        Huesped huesped = operacionService.guardarHuesped(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(HuespedDTO.of(huesped));
    }

    // ==================== CHECK-IN / CHECK-OUT ====================

    @PostMapping("/operaciones/checkin")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<String> checkIn(@Valid @RequestBody CheckInFrontRequest request) {
        operacionService.checkIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Check-In exitoso");
    }

    @GetMapping("/operaciones/{idHabitacion}/pre-checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<PreCheckoutDTO> preCheckout(@PathVariable Long idHabitacion) {
        return ResponseEntity.ok(operacionService.preCheckout(idHabitacion));
    }

    @PostMapping("/operaciones/{idHabitacion}/check-out")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<Map<String, Object>> checkOut(@PathVariable Long idHabitacion,
                                                        @RequestBody CheckOutFrontRequest request) {
        operacionService.checkOut(idHabitacion, request);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("success", true);
        respuesta.put("message", "Pago registrado y check-out exitoso. Habitación en limpieza.");
        return ResponseEntity.ok(respuesta);
    }

    // ==================== HISTORIAL ====================

    @GetMapping("/historial/hotel/{idHotel}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<MovimientoHistorico>> historialHotel(@PathVariable Long idHotel) {
        List<MovimientoHistorico> historial = operacionService.historialHotel(idHotel);
        return ResponseEntity.ok(historial);
    }

    @GetMapping("/habitaciones/{id}/historial")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<MovimientoHistorico>> historialHabitacion(@PathVariable Long id) {
        List<MovimientoHistorico> historial = operacionService.historialHabitacion(id);
        return ResponseEntity.ok(historial);
    }

    // ==================== PERFIL ====================

    @GetMapping("/perfil/actual")
    public ResponseEntity<PerfilDTO> perfilActual() {
        return ResponseEntity.ok(operacionService.perfilActual());
    }

    @PostMapping("/perfil/vincular")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<String> vincularPerfil(@RequestBody VincularRequest request) {
        return ResponseEntity.ok(operacionService.vincularPerfil(request));
    }
}