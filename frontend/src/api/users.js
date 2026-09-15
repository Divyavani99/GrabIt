import { request } from "./client";

export const registerUser = (body) =>
  request("/v1/users/register", {
    method: "POST",
    body,
  });

export const getProfile = (userId, token) =>
  request(`/v1/users/${userId}`, {
    userId,
    token,
  });

export const addAddress = (userId, token, body) =>
  request(`/v1/users/${userId}/addresses`, {
    method: "POST",
    userId,
    token,
    body,
  });
