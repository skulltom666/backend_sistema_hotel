package com.hotel.sistemahotelero.shared.persistence;

import com.hotel.sistemahotelero.shared.enums.RoomStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rooms")
@Data
public class Room extends BaseEntity {
    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "price_per_night")
    private BigDecimal pricePerNight;

    private String description;

    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.DISPONIBLE;

    @ManyToOne
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    private boolean hasBathroom = true;
    private boolean hasTV = true;
    private boolean hasWifi = true;
    private boolean hasAirConditioning = true;
    private String view;
    private Integer squareMeters;
    private String amenities; // JSON o texto separado por comas

    // ===== Campos alineados al frontend (RoomioHub) =====
    private Integer camasSimples = 1;
    private Integer camasDobles = 1;
    private Integer horasMinimas = 6;
    private BigDecimal precioMinimo = BigDecimal.ZERO;
    private BigDecimal precio12Horas = BigDecimal.ZERO;
    private BigDecimal precio24Horas = BigDecimal.ZERO;
    private BigDecimal precioHoraExtra = BigDecimal.ZERO;
}
