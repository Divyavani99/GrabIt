import { request } from "./client";

export const getPaymentsForOrder = (orderId) => request(`/v1/payments/order/${orderId}`);
