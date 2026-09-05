package com.fooddelivery.delivery.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Entity 6: DeliveryPartner — driver_id, name, phone, vehicle, current_location,
// availability_status, assigned_orders[]
@Entity
@Table(name = "delivery_partners")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPartner {

    @Id
    @GeneratedValue
    private UUID id; // driver_id

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    // e.g. "Bike", "Scooter", "Car"
    @Column(nullable = false)
    private String vehicle;

    @Column(nullable = false)
    private Double currentLat;

    @Column(nullable = false)
    private Double currentLng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.OFFLINE;

    @ElementCollection
    @CollectionTable(name = "delivery_partner_assigned_orders", joinColumns = @JoinColumn(name = "driver_id"))
    @Column(name = "order_id")
    private List<UUID> assignedOrders = new ArrayList<>();
}
