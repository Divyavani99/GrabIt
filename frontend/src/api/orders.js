import { request } from "./client";

export const placeOrder = (userId, { cartId, addressId, paymentMethod, promoCode }) =>
  request("/v1/orders", {
    method: "POST",
    userId,
    body: { cartId, addressId, paymentMethod, promoCode },
  });

export const getOrder = (orderId) => request(`/v1/orders/${orderId}`);
