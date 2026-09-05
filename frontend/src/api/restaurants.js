import { request } from "./client";

export const getNearbyRestaurants = ({ lat, lng, radius = 5 }) =>
  request(`/v1/restaurants/nearby?lat=${lat}&lng=${lng}&radius=${radius}`);

export const searchRestaurants = ({ query, lat, lng, cuisine, rating, sortBy }) => {
  const params = new URLSearchParams();
  if (query) params.set("query", query);
  if (lat != null) params.set("lat", lat);
  if (lng != null) params.set("lng", lng);
  if (cuisine) params.set("cuisine", cuisine);
  if (rating != null) params.set("rating", rating);
  if (sortBy) params.set("sortBy", sortBy);
  return request(`/v1/restaurants/search?${params.toString()}`);
};

export const getRestaurant = (id) => request(`/v1/restaurants/${id}`);

export const getMenu = (id, { page = 0, size = 100 } = {}) =>
  request(`/v1/restaurants/${id}/menu?page=${page}&size=${size}`);
