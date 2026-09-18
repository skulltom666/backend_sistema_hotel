package com.hotel.sistemahotelero.modules.cleaning.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CleaningRequest {
    @NotNull(message = "El ID de la habitación es obligatorio")
    private Long roomId;

    private LocalDateTime scheduledDate;

    private String assignedTo;

    private String observations;

    // Constructor para creación simple
    public CleaningRequest(Long roomId) {
        this.roomId = roomId;
        this.scheduledDate = LocalDateTime.now();
    }
}
