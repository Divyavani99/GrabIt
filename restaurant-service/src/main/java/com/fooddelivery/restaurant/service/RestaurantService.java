package com.fooddelivery.restaurant.service;

import com.fooddelivery.restaurant.dto.MenuPage;
import com.fooddelivery.restaurant.dto.RestaurantSummary;
import com.fooddelivery.restaurant.exception.RestaurantNotFoundException;
import com.fooddelivery.restaurant.model.FoodMenu;
import com.fooddelivery.restaurant.model.Restaurant;
import com.fooddelivery.restaurant.repository.FoodMenuRepository;
import com.fooddelivery.restaurant.repository.RestaurantRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final FoodMenuRepository foodMenuRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, FoodMenuRepository foodMenuRepository) {
        this.restaurantRepository = restaurantRepository;
        this.foodMenuRepository = foodMenuRepository;
    }

    public List<RestaurantSummary> findNearby(double lat, double lng, double radiusKm) {
        double[] bbox = GeoUtils.boundingBox(lat, lng, radiusKm);
        List<Restaurant> candidates = restaurantRepository.findWithinBoundingBox(bbox[0], bbox[1], bbox[2], bbox[3]);

        return candidates.stream()
                .map(r -> RestaurantSummary.of(r, GeoUtils.haversineKm(lat, lng, r.getLat(), r.getLng())))
                .filter(s -> s.distanceKm() <= radiusKm)
                .sorted(Comparator.comparingDouble(RestaurantSummary::distanceKm))
                .collect(Collectors.toList());
    }

    public List<RestaurantSummary> search(String query, Double lat, Double lng, String cuisine,
                                           Double rating, String sortBy) {
        List<Restaurant> results = restaurantRepository.search(query, cuisine, rating);

        List<RestaurantSummary> summaries = results.stream()
                .map(r -> {
                    double dist = (lat != null && lng != null)
                            ? GeoUtils.haversineKm(lat, lng, r.getLat(), r.getLng())
                            : 0.0;
                    return RestaurantSummary.of(r, dist);
                })
                .collect(Collectors.toList());

        Comparator<RestaurantSummary> comparator = switch (sortBy == null ? "distance" : sortBy) {
            case "rating" -> Comparator.comparingDouble(RestaurantSummary::rating).reversed();
            case "distance" -> Comparator.comparingDouble(RestaurantSummary::distanceKm);
            default -> Comparator.comparingDouble(RestaurantSummary::distanceKm);
        };
        summaries.sort(comparator);
        return summaries;
    }

    @Cacheable(value = "restaurantDetails", key = "#id")
    public Restaurant getById(UUID id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }

    public MenuPage getMenu(UUID restaurantId, int page, int size) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new RestaurantNotFoundException(restaurantId);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<FoodMenu> menuPage = foodMenuRepository.findByRestaurantId(restaurantId, pageable);

        var byCategory = menuPage.getContent().stream()
                .collect(Collectors.groupingBy(FoodMenu::getCategory));

        return new MenuPage(byCategory, menuPage.getNumber(), menuPage.getSize(),
                menuPage.getTotalElements(), menuPage.getTotalPages());
    }
}
