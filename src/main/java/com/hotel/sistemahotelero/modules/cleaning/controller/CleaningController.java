package com.hotel.sistemahotelero.modules.cleaning.controller;


import com.hotel.sistemahotelero.modules.cleaning.dto.CleaningRequest;
import com.hotel.sistemahotelero.modules.cleaning.service.CleaningService;
import com.hotel.sistemahotelero.shared.dto.ApiResponse;
import com.hotel.sistemahotelero.shared.enums.CleaningStatus;
import com.hotel.sistemahotelero.shared.persistence.Cleaning;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para la gestión de tareas de limpieza.
 * Todos los endpoints requieren autenticación y el tenant ID se obtiene del contexto.
 */
@RestController
@RequestMapping("/api/cleanings")
@RequiredArgsConstructor
@Slf4j
public class CleaningController {

    private final CleaningService cleaningService;

    /**
     * Obtiene todas las tareas de limpieza del tenant actual
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<List<Cleaning>>> getAllCleanings() {
        try {
            List<Cleaning> cleanings = cleaningService.getCleaningsByTenant();
            return ResponseEntity.ok(ApiResponse.success("Tareas de limpieza obtenidas exitosamente", cleanings));
        } catch (Exception e) {
            log.error("Error obteniendo tareas de limpieza: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene una tarea de limpieza por su ID
     */
    @GetMapping("/{cleaningId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<Cleaning>> getCleaningById(@PathVariable Long cleaningId) {
        try {
            Cleaning cleaning = cleaningService.getCleaningById(cleaningId);
            return ResponseEntity.ok(ApiResponse.success("Tarea de limpieza obtenida exitosamente", cleaning));
        } catch (Exception e) {
            log.error("Error obteniendo tarea de limpieza {}: {}", cleaningId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Crea una nueva tarea de limpieza
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Cleaning>> createCleaning(@Valid @RequestBody CleaningRequest request) {
        try {
            Cleaning cleaning = cleaningService.createCleaning(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tarea de limpieza creada exitosamente", cleaning));
        } catch (Exception e) {
            log.error("Error creando tarea de limpieza: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Crea una tarea de limpieza automática para una habitación específica
     */
    @PostMapping("/auto/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Void>> autoCreateCleaning(@PathVariable Long roomId) {
        try {
            cleaningService.autoCreateCleaningForRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success("Tarea de limpieza automática creada exitosamente", null));
        } catch (Exception e) {
            log.error("Error creando tarea de limpieza automática: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Completa una tarea de limpieza
     */
    @PostMapping("/{cleaningId}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<Cleaning>> completeCleaning(@PathVariable Long cleaningId) {
        try {
            Cleaning cleaning = cleaningService.completeCleaning(cleaningId);
            return ResponseEntity.ok(ApiResponse.success("Limpieza completada exitosamente", cleaning));
        } catch (Exception e) {
            log.error("Error completando limpieza {}: {}", cleaningId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Cancela una tarea de limpieza
     */
    @PostMapping("/{cleaningId}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Cleaning>> cancelCleaning(@PathVariable Long cleaningId) {
        try {
            Cleaning cleaning = cleaningService.cancelCleaning(cleaningId);
            return ResponseEntity.ok(ApiResponse.success("Limpieza cancelada exitosamente", cleaning));
        } catch (Exception e) {
            log.error("Error cancelando limpieza {}: {}", cleaningId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Asigna una tarea de limpieza a una persona
     */
    @PatchMapping("/{cleaningId}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Cleaning>> assignCleaning(
            @PathVariable Long cleaningId,
            @RequestParam String assignedTo) {
        try {
            Cleaning cleaning = cleaningService.assignCleaning(cleaningId, assignedTo);
            return ResponseEntity.ok(ApiResponse.success("Limpieza asignada exitosamente", cleaning));
        } catch (Exception e) {
            log.error("Error asignando limpieza {}: {}", cleaningId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Actualiza el estado de una tarea de limpieza
     */
    @PatchMapping("/{cleaningId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<Cleaning>> updateCleaningStatus(
            @PathVariable Long cleaningId,
            @RequestParam CleaningStatus status) {
        try {
            Cleaning cleaning = cleaningService.updateCleaningStatus(cleaningId, status);
            return ResponseEntity.ok(ApiResponse.success("Estado de limpieza actualizado exitosamente", cleaning));
        } catch (Exception e) {
            log.error("Error actualizando estado de limpieza {}: {}", cleaningId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Obtiene todas las tareas de limpieza pendientes
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<List<Cleaning>>> getPendingCleanings() {
        try {
            List<Cleaning> cleanings = cleaningService.getPendingCleanings();
            return ResponseEntity.ok(ApiResponse.success("Limpiezas pendientes obtenidas exitosamente", cleanings));
        } catch (Exception e) {
            log.error("Error obteniendo limpiezas pendientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene todas las tareas de limpieza de una habitación específica
     */
    @GetMapping("/room/{roomId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<List<Cleaning>>> getCleaningsByRoom(@PathVariable Long roomId) {
        try {
            List<Cleaning> cleanings = cleaningService.getCleaningsByRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success("Limpiezas de la habitación obtenidas exitosamente", cleanings));
        } catch (Exception e) {
            log.error("Error obteniendo limpiezas por habitación {}: {}", roomId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene todas las tareas de limpieza atrasadas
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Cleaning>>> getOverdueCleanings() {
        try {
            List<Cleaning> cleanings = cleaningService.getOverdueCleanings();
            return ResponseEntity.ok(ApiResponse.success("Limpiezas atrasadas obtenidas exitosamente", cleanings));
        } catch (Exception e) {
            log.error("Error obteniendo limpiezas atrasadas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene el conteo de tareas de limpieza pendientes
     */
    @GetMapping("/count/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<Long>> countPendingCleanings() {
        try {
            long count = cleaningService.countPendingCleanings();
            return ResponseEntity.ok(ApiResponse.success("Conteo de limpiezas pendientes obtenido exitosamente", count));
        } catch (Exception e) {
            log.error("Error obteniendo conteo de limpiezas pendientes: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene las tareas de limpieza asignadas a una persona específica
     */
    @GetMapping("/assigned/{assignedTo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION', 'LIMPIEZA')")
    public ResponseEntity<ApiResponse<List<Cleaning>>> getCleaningsAssignedTo(
            @PathVariable String assignedTo,
            @RequestParam(required = false) CleaningStatus status) {
        try {
            List<Cleaning> cleanings = cleaningService.getCleaningsAssignedTo(assignedTo, status);
            return ResponseEntity.ok(ApiResponse.success("Limpiezas asignadas obtenidas exitosamente", cleanings));
        } catch (Exception e) {
            log.error("Error obteniendo limpiezas asignadas a {}: {}", assignedTo, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene las tareas de limpieza por rango de fechas
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Cleaning>>> getCleaningsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Cleaning> cleanings = cleaningService.getCleaningsByDateRange(startDate, endDate);
            return ResponseEntity.ok(ApiResponse.success("Limpiezas por rango de fechas obtenidas exitosamente", cleanings));
        } catch (Exception e) {
            log.error("Error obteniendo limpiezas por rango de fechas: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Elimina una tarea de limpieza (soft delete)
     */
    @DeleteMapping("/{cleaningId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCleaning(@PathVariable Long cleaningId) {
        try {
            cleaningService.deleteCleaning(cleaningId);
            return ResponseEntity.ok(ApiResponse.success("Tarea de limpieza eliminada exitosamente", null));
        } catch (Exception e) {
            log.error("Error eliminando tarea de limpieza {}: {}", cleaningId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Actualiza una tarea de limpieza existente
     */
    @PutMapping("/{cleaningId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Cleaning>> updateCleaning(
            @PathVariable Long cleaningId,
            @Valid @RequestBody CleaningRequest request) {
        try {
            Cleaning cleaning = cleaningService.updateCleaning(cleaningId, request);
            return ResponseEntity.ok(ApiResponse.success("Tarea de limpieza actualizada exitosamente", cleaning));
        } catch (Exception e) {
            log.error("Error actualizando tarea de limpieza {}: {}", cleaningId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}