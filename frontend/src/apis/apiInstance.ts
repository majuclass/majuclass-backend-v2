/** @format */

import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_BASE_API_URL,
  withCredentials: true,
});

export const fastapi = axios.create({
  baseURL: import.meta.env.VITE_BASE_AI_URL,
  withCredentials: true,
});

// TODO: authorization 주입
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("accessToken");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
