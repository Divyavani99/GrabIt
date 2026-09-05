package com.fooddelivery.restaurant.controller;

import com.fooddelivery.restaurant.dto.MenuPage;
import com.fooddelivery.restaurant.dto.RestaurantSummary;
import com.fooddelivery.restaurant.model.Restaurant;
import com.fooddelivery.restaurant.service.RestaurantService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    // GET /v1/restaurants/nearby?lat=..&lng=..&radius=..
    @GetMapping("/nearby")
    public List<RestaurantSummary> nearby(@RequestParam double lat,
                                           @RequestParam double lng,
                                           @RequestParam(defaultValue = "5") double radius) {
        return restaurantService.findNearby(lat, lng, radius);
    }

    // GET /v1/restaurants/search?query=&lat=&lng=&cuisine=&rating=&sortBy=
    @GetMapping("/search")
    public List<RestaurantSummary> search(@RequestParam(required = false) String query,
                                           @RequestParam(required = false) Double lat,
                                           @RequestParam(required = false) Double lng,
                                           @RequestParam(required = false) String cuisine,
                                           @RequestParam(required = false) Double rating,
                                           @RequestParam(required = false) String sortBy) {
        return restaurantService.search(query, lat, lng, cuisine, rating, sortBy);
    }

    // GET /v1/restaurants/{id}
    @GetMapping("/{id}")
    public Restaurant getById(@PathVariable UUID id) {
        return restaurantService.getById(id);
    }

    // GET /v1/restaurants/{id}/menu?page=&size=
    @GetMapping("/{id}/menu")
    public MenuPage getMenu(@PathVariable UUID id,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "20") int size) {
        return restaurantService.getMenu(id, page, size);
    }
}
