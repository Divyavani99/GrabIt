import { request } from "./client";

export const registerUser = ({ name, email, phone, password }) =>
  request("/v1/users/register", { method: "POST", body: { name, email, phone, password } });

export const getProfile = (userId) => request(`/v1/users/${userId}`);

export const addAddress = (userId, { street, city, lat, lng, label }) =>
  request(`/v1/users/${userId}/addresses`, {
    method: "POST",
    body: { street, city, lat, lng, label },
  });
