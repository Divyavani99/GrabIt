package com.fooddelivery.user.controller;

import com.fooddelivery.user.dto.AddressRequest;
import com.fooddelivery.user.dto.RegisterRequest;
import com.fooddelivery.user.dto.RegisterResponse;
import com.fooddelivery.user.dto.UserProfileResponse;
import com.fooddelivery.user.model.Address;
import com.fooddelivery.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST /v1/users/register
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        RegisterResponse response = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /v1/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(UserProfileResponse.from(userService.getUser(userId)));
    }

    // POST /v1/users/{userId}/addresses
    @PostMapping("/{userId}/addresses")
    public ResponseEntity<Address> addAddress(@PathVariable UUID userId,
                                               @Valid @RequestBody AddressRequest req) {
        Address address = userService.addAddress(userId, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(address);
    }
}
