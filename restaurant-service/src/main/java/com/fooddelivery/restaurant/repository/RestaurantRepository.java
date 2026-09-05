package com.fooddelivery.restaurant.repository;

import com.fooddelivery.restaurant.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    // Coarse bounding-box pre-filter; precise distance + sort done in the service layer.
    @Query("""
           SELECT r FROM Restaurant r
           WHERE r.lat BETWEEN :minLat AND :maxLat
             AND r.lng BETWEEN :minLng AND :maxLng
           """)
    List<Restaurant> findWithinBoundingBox(@Param("minLat") double minLat, @Param("maxLat") double maxLat,
                                            @Param("minLng") double minLng, @Param("maxLng") double maxLng);

    @Query("""
           SELECT r FROM Restaurant r
           WHERE (:query IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')))
             AND (:cuisine IS NULL OR LOWER(r.cuisine) = LOWER(:cuisine))
             AND (:minRating IS NULL OR r.rating >= :minRating)
           """)
    List<Restaurant> search(@Param("query") String query,
                             @Param("cuisine") String cuisine,
                             @Param("minRating") Double minRating);
}
