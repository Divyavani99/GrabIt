import { request, wsUrl } from "./client";

export const getTracking = (orderId) => request(`/v1/delivery/${orderId}/tracking`);

export const trackingSocketUrl = (orderId) => wsUrl(`/v1/orders/${orderId}/track`);
