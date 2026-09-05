package com.fooddelivery.user.dto;

import com.fooddelivery.user.model.Address;
import com.fooddelivery.user.model.PaymentMethod;
import com.fooddelivery.user.model.User;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserProfileResponse(
        UUID id,
        String name,
        String email,
        String phone,
        List<Address> addresses,
        List<PaymentMethod> paymentMethods
) {
    public static UserProfileResponse from(User u) {
        return new UserProfileResponse(
                u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getAddresses(), u.getPaymentMethods()
        );
    }
}
