package com.fooddelivery.user.service;

import com.fooddelivery.user.config.JwtUtil;
import com.fooddelivery.user.dto.AddressRequest;
import com.fooddelivery.user.dto.RegisterRequest;
import com.fooddelivery.user.dto.RegisterResponse;
import com.fooddelivery.user.event.UserRegisteredEvent;
import com.fooddelivery.user.exception.ApiExceptions;
import com.fooddelivery.user.model.Address;
import com.fooddelivery.user.model.User;
import com.fooddelivery.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserEventProducer eventProducer;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, UserEventProducer eventProducer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiExceptions.EmailAlreadyExistsException("Email already registered: " + req.email());
        }
        User user = new User();
        user.setName(req.name());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user = userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());

        eventProducer.publishUserRegistered(
                new UserRegisteredEvent(user.getId(), user.getName(), user.getEmail(), Instant.now())
        );

        return new RegisterResponse(user.getId(), token);
    }

    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiExceptions.UserNotFoundException("User not found: " + userId));
    }

    @Transactional
    public Address addAddress(UUID userId, AddressRequest req) {
        User user = getUser(userId);
        Address address = new Address();
        address.setUser(user);
        address.setStreet(req.street());
        address.setCity(req.city());
        address.setLat(req.lat());
        address.setLng(req.lng());
        address.setLabel(req.label());
        user.getAddresses().add(address);
        userRepository.save(user);
        return address;
    }
}
