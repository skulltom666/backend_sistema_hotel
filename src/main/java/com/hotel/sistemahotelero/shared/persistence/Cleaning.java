package com.hotel.sistemahotelero.shared.persistence;

import com.hotel.sistemahotelero.shared.enums.CleaningStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cleanings")
@Data
public class Cleaning extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDateTime scheduledDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Enumerated(EnumType.STRING)
    private CleaningStatus status = CleaningStatus.PENDIENTE;

    @Column(name = "assigned_to")
    private String assignedTo;

    private String observations;

    @PrePersist
    protected void onCreate() {
        if (this.scheduledDate == null) {
            this.scheduledDate = LocalDateTime.now();
        }
    }
}
