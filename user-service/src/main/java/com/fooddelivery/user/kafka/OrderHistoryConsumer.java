package com.fooddelivery.user.kafka;

import com.fooddelivery.user.event.OrderPlacedEvent;
import com.fooddelivery.user.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderHistoryConsumer {

    private final UserRepository userRepository;

    public OrderHistoryConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Keeps User.orderHistory eventually-consistent with order-service, without a synchronous call.
    @KafkaListener(topics = "order-events", groupId = "user-service-group")
    @Transactional
    public void onOrderPlaced(OrderPlacedEvent event) {
        userRepository.findById(event.userId()).ifPresent(user -> {
            if (!user.getOrderHistory().contains(event.orderId())) {
                user.getOrderHistory().add(event.orderId());
                userRepository.save(user);
            }
        });
    }
}
