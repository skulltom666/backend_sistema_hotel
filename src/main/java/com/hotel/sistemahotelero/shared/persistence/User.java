package com.hotel.sistemahotelero.shared.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hotel.sistemahotelero.shared.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
public class User extends BaseEntity {
    @Column(unique = true, nullable = false, length = 11)
    private String ruc;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    private String nombre;
    private String apellido;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String telefono;

    private String rol = "ADMINISTRADOR";

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscriptionPlan;

    private Integer maxHotels;
    private Integer maxFloors;
    private Integer maxRooms;

    private boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Hotel> hotels = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        // ✅ Asignar tenantId
        if (this.getTenantId() == null) {
            this.setTenantId(this.ruc);
        }

        // ✅ Asignar fechas
        if (this.getCreatedAt() == null) {
            this.setCreatedAt(LocalDateTime.now());
        }
        if (this.getUpdatedAt() == null) {
            this.setUpdatedAt(LocalDateTime.now());
        }

        // ✅ Asignar plan por defecto
        if (this.subscriptionPlan == null) {
            this.subscriptionPlan = SubscriptionPlan.BASICO;
        }

        // ✅ Asignar límites según plan
        if (this.maxHotels == null) {
            this.maxHotels = this.subscriptionPlan.getMaxHotels();
        }
        if (this.maxFloors == null) {
            this.maxFloors = this.subscriptionPlan.getMaxFloors();
        }
        if (this.maxRooms == null) {
            this.maxRooms = this.subscriptionPlan.getMaxRooms();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.setUpdatedAt(LocalDateTime.now());
    }
}