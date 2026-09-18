package com.hotel.sistemahotelero.modules.auth.dto;

import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    // Opcional: si no se envía RUC, se autogenera uno local (modo frontend RoomioHub)
    private String ruc;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    private String nombre;
    private String apellido;
    private String telefono;
    private String rol;
    private SubscriptionPlan subscriptionPlan;
}