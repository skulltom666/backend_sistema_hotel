package com.hotel.sistemahotelero.modules.auth.dto;

import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import com.hotel.sistemahotelero.shared.persistence.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelDTO {
    private Long id;
    private String nombre;
    private String plan;
    private String direccion;
    private String telefono;

    public static HotelDTO of(Hotel hotel, SubscriptionPlan plan) {
        return HotelDTO.builder()
                .id(hotel.getId())
                .nombre(hotel.getNombre())
                .plan(mapPlanToFront(plan))
                .direccion(hotel.getDireccion())
                .telefono(hotel.getTelefono())
                .build();
    }

    /**
     * El frontend usa BASIC / PREMIUM / PRO.
     */
    public static String mapPlanToFront(SubscriptionPlan plan) {
        if (plan == null) {
            return "BASIC";
        }
        return switch (plan) {
            case PROFESIONAL -> "PREMIUM";
            case EMPRESARIAL -> "PRO";
            default -> "BASIC";
        };
    }

    /**
     * Traduce el plan del frontend al enum del backend.
     */
    public static SubscriptionPlan mapPlanFromFront(String plan) {
        if (plan == null) {
            return SubscriptionPlan.BASICO;
        }
        return switch (plan.toUpperCase()) {
            case "PREMIUM" -> SubscriptionPlan.PROFESIONAL;
            case "PRO", "EMPRESARIAL" -> SubscriptionPlan.EMPRESARIAL;
            default -> SubscriptionPlan.BASICO;
        };
    }
}