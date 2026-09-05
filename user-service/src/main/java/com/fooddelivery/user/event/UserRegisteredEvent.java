package com.fooddelivery.user.event;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String name,
        String email,
        Instant occurredAt
) {}
