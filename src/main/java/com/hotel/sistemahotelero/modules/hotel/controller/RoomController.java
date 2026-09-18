package com.hotel.sistemahotelero.modules.hotel.controller;

import com.hotel.sistemahotelero.modules.hotel.dto.RoomRequest;
import com.hotel.sistemahotelero.modules.hotel.service.RoomService;
import com.hotel.sistemahotelero.shared.dto.ApiResponse;
import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import com.hotel.sistemahotelero.shared.persistence.Room;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RoomController {

    private final RoomService roomService;

    @GetMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<List<Room>>> getRoomsByFloor(@PathVariable Long floorId) {
        try {
            List<Room> rooms = roomService.getRoomsByFloor(floorId);
            return ResponseEntity.ok(ApiResponse.success(rooms));
        } catch (Exception e) {
            log.error("Error obteniendo habitaciones: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @PostMapping("/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<Room>> createRoom(@PathVariable Long floorId, @Valid @RequestBody RoomRequest request) {
        try {
            Room room = roomService.createRoom(floorId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Habitación creada exitosamente", room));
        } catch (Exception e) {
            log.error("Error creando habitación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Room>> updateRoom(@PathVariable Long roomId, @Valid @RequestBody RoomRequest request) {
        try {
            Room room = roomService.updateRoom(roomId, request);
            return ResponseEntity.ok(ApiResponse.success("Habitación actualizada exitosamente", room));
        } catch (Exception e) {
            log.error("Error actualizando habitación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @PatchMapping("/rooms/{roomId}/status")
    public ResponseEntity<ApiResponse<Room>> updateRoomStatus(@PathVariable Long roomId, @RequestParam RoomStatus status) {
        try {
            Room room = roomService.updateRoomStatus(roomId, status);
            return ResponseEntity.ok(ApiResponse.success("Estado actualizado exitosamente", room));
        } catch (Exception e) {
            log.error("Error actualizando estado: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/hotels/{hotelId}/rooms/status/{status}")
    public ResponseEntity<ApiResponse<List<Room>>> getRoomsByStatus(@PathVariable Long hotelId, @PathVariable RoomStatus status) {
        try {
            List<Room> rooms = roomService.getRoomsByStatus(hotelId, status);
            return ResponseEntity.ok(ApiResponse.success(rooms));
        } catch (Exception e) {
            log.error("Error obteniendo habitaciones por estado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long roomId) {
        try {
            roomService.deleteRoom(roomId);
            return ResponseEntity.ok(ApiResponse.success("Habitación eliminada exitosamente", null));
        } catch (Exception e) {
            log.error("Error eliminando habitación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }
}
