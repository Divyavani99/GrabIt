package com.fooddelivery.delivery.repository;

import com.fooddelivery.delivery.model.AvailabilityStatus;
import com.fooddelivery.delivery.model.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, UUID> {
    List<DeliveryPartner> findByAvailabilityStatus(AvailabilityStatus status);
}
