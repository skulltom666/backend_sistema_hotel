package com.hotel.sistemahotelero.shared.persistence;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "floors")
@Data
public class Floor extends BaseEntity {
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    private String name;
    private String description;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Room> rooms = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.name == null) {
            this.name = "Piso " + this.floorNumber;
        }
    }
}
