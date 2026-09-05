package com.fooddelivery.restaurant.service;

public final class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private GeoUtils() {}

    /** Great-circle distance between two lat/lng points, in kilometers. */
    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    /** Rough bounding box (in degrees) for a given radius in km, to pre-filter with SQL before precise distance calc. */
    public static double[] boundingBox(double lat, double lng, double radiusKm) {
        double latDelta = radiusKm / 111.0; // ~111km per degree latitude
        double lngDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(lat)) + 1e-6);
        return new double[] { lat - latDelta, lat + latDelta, lng - lngDelta, lng + lngDelta };
    }
}
