package com.hotel.sistemahotelero.modules.hotel.controller;

import com.hotel.sistemahotelero.modules.hotel.dto.HotelRequest;
import com.hotel.sistemahotelero.modules.hotel.service.HotelService;
import com.hotel.sistemahotelero.shared.dto.ApiResponse;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Slf4j
public class HotelController {

    private final HotelService hotelService;

    /**
     * Obtiene todos los hoteles del tenant actual
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Hotel>>> getHotels() {
        try {
            List<Hotel> hotels = hotelService.getHotelsByTenant();
            return ResponseEntity.ok(ApiResponse.success("Hoteles obtenidos exitosamente", hotels));
        } catch (Exception e) {
            log.error("Error obteniendo hoteles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene un hotel por su ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Hotel>> getHotelById(@PathVariable Long id) {
        try {
            Hotel hotel = hotelService.getHotelById(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel obtenido exitosamente", hotel));
        } catch (Exception e) {
            log.error("Error obteniendo hotel {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    /**
     * Crea un nuevo hotel
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Hotel>> createHotel(@Valid @RequestBody HotelRequest request) {
        try {
            Hotel hotel = hotelService.createHotel(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Hotel creado exitosamente", hotel));
        } catch (Exception e) {
            log.error("Error creando hotel: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Actualiza un hotel existente
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Hotel>> updateHotel(
            @PathVariable Long id,
            @Valid @RequestBody HotelRequest request) {
        try {
            Hotel hotel = hotelService.updateHotel(id, request);
            return ResponseEntity.ok(ApiResponse.success("Hotel actualizado exitosamente", hotel));
        } catch (Exception e) {
            log.error("Error actualizando hotel {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Elimina un hotel (soft delete)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteHotel(@PathVariable Long id) {
        try {
            hotelService.deleteHotel(id);
            return ResponseEntity.ok(ApiResponse.success("Hotel eliminado exitosamente", null));
        } catch (Exception e) {
            log.error("Error eliminando hotel {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    /**
     * Obtiene el conteo de hoteles del tenant actual
     */
    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<Long>> countHotels() {
        try {
            long count = hotelService.countHotelsByTenant();
            return ResponseEntity.ok(ApiResponse.success("Conteo de hoteles obtenido exitosamente", count));
        } catch (Exception e) {
            log.error("Error obteniendo conteo de hoteles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Obtiene todos los hoteles con detalles (pisos y habitaciones)
     */
    @GetMapping("/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Hotel>>> getHotelsWithDetails() {
        try {
            List<Hotel> hotels = hotelService.getHotelsWithDetails();
            return ResponseEntity.ok(ApiResponse.success("Hoteles con detalles obtenidos exitosamente", hotels));
        } catch (Exception e) {
            log.error("Error obteniendo hoteles con detalles: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Busca hoteles por nombre
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<ApiResponse<List<Hotel>>> searchHotels(@RequestParam String nombre) {
        try {
            List<Hotel> hotels = hotelService.searchHotelsByName(nombre);
            return ResponseEntity.ok(ApiResponse.success("Hoteles encontrados exitosamente", hotels));
        } catch (Exception e) {
            log.error("Error buscando hoteles por nombre {}: {}", nombre, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    /**
     * Actualiza los conteos de un hotel (pisos y habitaciones)
     */
    @PostMapping("/{id}/update-counts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateHotelCounts(@PathVariable Long id) {
        try {
            hotelService.updateHotelCounts(id);
            return ResponseEntity.ok(ApiResponse.success("Conteos actualizados exitosamente", null));
        } catch (Exception e) {
            log.error("Error actualizando conteos del hotel {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}