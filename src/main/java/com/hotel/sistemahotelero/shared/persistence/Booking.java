package com.hotel.sistemahotelero.shared.persistence;

import com.hotel.sistemahotelero.shared.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "bookings")
@Data
public class Booking extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "guest_document")
    private String guestDocument;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "check_in", nullable = false)
    private LocalDateTime checkIn;

    @Column(name = "check_out")
    private LocalDateTime checkOut;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.CONFIRMADA;

    @Column(name = "number_of_guests")
    private Integer numberOfGuests = 1;

    @Column(name = "checked_in")
    private Boolean checkedIn = false;

    @Column(name = "checked_out")
    private Boolean checkedOut = false;

    private String observations;
    private String source; // "WEB", "APP", "PRESENCIAL"
    private Double totalAmount;
    private Double paidAmount;
    private String paymentMethod;
}