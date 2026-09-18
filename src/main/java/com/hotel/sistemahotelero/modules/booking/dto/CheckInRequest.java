package com.hotel.sistemahotelero.modules.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckInRequest {
    @NotNull(message = "El ID de la habitación es obligatorio")
    private Long roomId;

    @NotBlank(message = "El nombre del huésped es obligatorio")
    private String guestName;

    @NotBlank(message = "El documento del huésped es obligatorio")
    private String guestDocument;

    @Email(message = "Email inválido")
    private String guestEmail;

    private String guestPhone;

    @NotNull(message = "La fecha de check-out es obligatoria")
    private LocalDateTime checkOut;

    @Min(value = 1, message = "Debe haber al menos 1 huésped")
    private Integer numberOfGuests = 1;

    private String observations;

    private String source; // WEB, APP, PRESENCIAL
}