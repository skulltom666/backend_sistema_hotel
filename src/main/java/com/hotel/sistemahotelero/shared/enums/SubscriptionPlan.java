package com.hotel.sistemahotelero.shared.enums;

import lombok.Getter;

@Getter
public enum SubscriptionPlan {
    BASICO(1, 3, 10, 9.99),
    PROFESIONAL(3, 10, 50, 29.99),
    EMPRESARIAL(10, 30, 200, 79.99);

    private final int maxHotels;
    private final int maxFloors;
    private final int maxRooms;
    private final double price;

    SubscriptionPlan(int maxHotels, int maxFloors, int maxRooms, double price) {
        this.maxHotels = maxHotels;
        this.maxFloors = maxFloors;
        this.maxRooms = maxRooms;
        this.price = price;
    }
}
