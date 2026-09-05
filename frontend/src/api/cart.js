import { request } from "./client";

export const getCart = (userId) => request("/v1/cart", { userId });

export const addCartItem = (userId, restaurantId, { itemId, quantity, customizations }) =>
  request(`/v1/cart/items?restaurantId=${restaurantId}`, {
    method: "POST",
    userId,
    body: { itemId, quantity, customizations },
  });

export const removeCartItem = (userId, cartItemId) =>
  request(`/v1/cart/items/${cartItemId}`, { method: "DELETE", userId });

export const applyPromo = (userId, promoCode) =>
  request("/v1/cart/promo", { method: "POST", userId, body: { promoCode } });
