package com.fooddelivery.delivery.service;

import com.fooddelivery.delivery.dto.LocationUpdateRequest;
import com.fooddelivery.delivery.dto.RegisterDriverRequest;
import com.fooddelivery.delivery.event.DeliveryAssignedEvent;
import com.fooddelivery.delivery.event.DeliveryTrackingUpdate;
import com.fooddelivery.delivery.exception.DeliveryExceptions;
import com.fooddelivery.delivery.model.AvailabilityStatus;
import com.fooddelivery.delivery.model.DeliveryPartner;
import com.fooddelivery.delivery.repository.DeliveryPartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeliveryPartnerService {

    private final DeliveryPartnerRepository repository;
    private final DeliveryEventProducer eventProducer;

    public DeliveryPartnerService(DeliveryPartnerRepository repository, DeliveryEventProducer eventProducer) {
        this.repository = repository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public DeliveryPartner register(RegisterDriverRequest req) {
        DeliveryPartner partner = new DeliveryPartner();
        partner.setName(req.name());
        partner.setPhone(req.phone());
        partner.setVehicle(req.vehicle());
        partner.setCurrentLat(req.lat());
        partner.setCurrentLng(req.lng());
        partner.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        return repository.save(partner);
    }

    public DeliveryPartner getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new DeliveryExceptions.DriverNotFoundException("Driver not found: " + id));
    }

    @Transactional
    public DeliveryPartner updateAvailability(UUID id, AvailabilityStatus status) {
        DeliveryPartner partner = getById(id);
        partner.setAvailabilityStatus(status);
        return repository.save(partner);
    }

    /** Called when a driver's app pushes a new GPS location / delivery status for an in-progress order. */
    @Transactional
    public DeliveryPartner updateLocation(UUID id, UUID orderId, LocationUpdateRequest req) {
        DeliveryPartner partner = getById(id);
        partner.setCurrentLat(req.lat());
        partner.setCurrentLng(req.lng());
        partner = repository.save(partner);

        eventProducer.publishTrackingUpdate(new DeliveryTrackingUpdate(
                orderId, partner.getId().toString(), partner.getName(),
                req.lat(), req.lng(), req.etaMinutes(),
                req.status() != null ? req.status() : "EN_ROUTE",
                Instant.now()
        ));

        return partner;
    }

    /**
     * Assigns the first available driver to a newly placed order.
     * A production system would rank candidates by proximity to the restaurant and current load;
     * this picks the first AVAILABLE partner for simplicity.
     */
    @Transactional
    public DeliveryPartner assignDriverToOrder(UUID orderId) {
        List<DeliveryPartner> available = repository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        if (available.isEmpty()) {
            throw new DeliveryExceptions.NoAvailableDriverException("No available delivery partners right now");
        }
        DeliveryPartner partner = available.get(0);
        partner.setAvailabilityStatus(AvailabilityStatus.BUSY);
        partner.getAssignedOrders().add(orderId);
        partner = repository.save(partner);

        eventProducer.publishDeliveryAssigned(new DeliveryAssignedEvent(
                orderId, partner.getId().toString(), partner.getName(), Instant.now()
        ));

        eventProducer.publishTrackingUpdate(new DeliveryTrackingUpdate(
                orderId, partner.getId().toString(), partner.getName(),
                partner.getCurrentLat(), partner.getCurrentLng(), 30, "ASSIGNED", Instant.now()
        ));

        return partner;
    }
}
