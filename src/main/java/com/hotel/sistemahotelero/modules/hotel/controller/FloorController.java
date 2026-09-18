package com.hotel.sistemahotelero.modules.hotel.controller;

import com.hotel.sistemahotelero.modules.hotel.dto.FloorRequest;
import com.hotel.sistemahotelero.modules.hotel.service.FloorService;
import com.hotel.sistemahotelero.shared.dto.ApiResponse;
import com.hotel.sistemahotelero.shared.persistence.Floor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hotels/{hotelId}/floors")
@RequiredArgsConstructor
@Slf4j
public class FloorController {

    private final FloorService floorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Floor>>> getFloors(@PathVariable Long hotelId) {
        try {
            List<Floor> floors = floorService.getFloorsByHotel(hotelId);
            return ResponseEntity.ok(ApiResponse.success(floors));
        } catch (Exception e) {
            log.error("Error obteniendo pisos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Floor>> createFloor(@PathVariable Long hotelId, @Valid @RequestBody FloorRequest request) {
        try {
            Floor floor = floorService.createFloor(hotelId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Piso creado exitosamente", floor));
        } catch (Exception e) {
            log.error("Error creando piso: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/{floorId}")
    public ResponseEntity<ApiResponse<Floor>> updateFloor(@PathVariable Long hotelId,
                                                          @PathVariable Long floorId,
                                                          @Valid @RequestBody FloorRequest request) {
        try {
            Floor floor = floorService.updateFloor(hotelId, floorId, request);
            return ResponseEntity.ok(ApiResponse.success("Piso actualizado exitosamente", floor));
        } catch (Exception e) {
            log.error("Error actualizando piso: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @DeleteMapping("/{floorId}")
    public ResponseEntity<ApiResponse<Void>> deleteFloor(@PathVariable Long hotelId, @PathVariable Long floorId) {
        try {
            floorService.deleteFloor(hotelId, floorId);
            return ResponseEntity.ok(ApiResponse.success("Piso eliminado exitosamente", null));
        } catch (Exception e) {
            log.error("Error eliminando piso: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}
