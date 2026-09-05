package com.fooddelivery.order.client;

import com.fooddelivery.order.dto.FoodMenuDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
public class RestaurantClient {

    private final WebClient webClient;

    public RestaurantClient(WebClient.Builder webClientBuilder,
                             @Value("${services.restaurant-service.url}") String restaurantServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(restaurantServiceUrl).build();
    }

    /** Fetches a single menu item's live price/availability from restaurant-service. */
    public FoodMenuDto getMenuItem(UUID restaurantId, UUID itemId) {
        // In a real implementation restaurant-service would expose GET /v1/restaurants/{id}/menu/items/{itemId}.
        // Here we fetch the paginated menu and locate the item, since only the listed endpoints exist.
        var menuPage = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/restaurants/{id}/menu")
                        .queryParam("page", 0)
                        .queryParam("size", 500)
                        .build(restaurantId))
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .block();

        if (menuPage == null) return null;

        var itemsByCategory = (java.util.Map<String, java.util.List<java.util.Map<String, Object>>>) menuPage.get("itemsByCategory");
        if (itemsByCategory == null) return null;

        for (var items : itemsByCategory.values()) {
            for (var item : items) {
                if (itemId.toString().equals(String.valueOf(item.get("id")))) {
                    return new FoodMenuDto(
                            itemId,
                            (String) item.get("name"),
                            new java.math.BigDecimal(String.valueOf(item.get("price"))),
                            Boolean.TRUE.equals(item.get("available"))
                    );
                }
            }
        }
        return null;
    }
}
