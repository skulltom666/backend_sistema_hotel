package com.hotel.sistemahotelero.shared.persistence;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "hotels")
@Data
public class Hotel extends BaseEntity {
    @Column(nullable = false)
    private String nombre;

    private String direccion;
    private String telefono;
    private String email;
    private String website;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Floor> floors = new ArrayList<>();

    @Column(name = "total_rooms")
    private Integer totalRooms = 0;

    @Column(name = "total_floors")
    private Integer totalFloors = 0;

    private String ubicacionLat;
    private String ubicacionLng;
    private String horarioApertura;
    private String horarioCierre;
    private String descripcion;

    @PrePersist
    protected void onCreate() {
        if (this.getTenantId() == null && this.user != null) {
            this.setTenantId(this.user.getRuc()); // 👈 Usar setter
        }
    }
}