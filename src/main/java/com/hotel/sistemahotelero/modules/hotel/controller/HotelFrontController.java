package com.hotel.sistemahotelero.modules.hotel.controller;

import com.hotel.sistemahotelero.modules.auth.dto.HotelDTO;
import com.hotel.sistemahotelero.modules.hotel.dto.RegistrarHotelRequest;
import com.hotel.sistemahotelero.modules.hotel.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hoteles")
@RequiredArgsConstructor
@Slf4j
public class HotelFrontController {

    private final HotelService hotelService;

    /**
     * Obtiene los hoteles del usuario actual (contrato frontend)
     */
    @GetMapping("/mio")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<List<HotelDTO>> hotelesMio() {
        return ResponseEntity.ok(hotelService.hotelesDelUsuario());
    }

    /**
     * Registra un hotel desde el formulario frontend (contrato)
     */
    @PostMapping("/registrar")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPCION')")
    public ResponseEntity<HotelDTO> registrarHotel(@Valid @RequestBody RegistrarHotelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(hotelService.crearHotelFront(request));
    }
}