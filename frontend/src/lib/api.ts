import axios from "axios";
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from "./auth";

const api = axios.create({
  baseURL: "",
});

api.interceptors.request.use((config) => {
  const token = getAccessToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let refreshing = false;

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config;
    if (error.response?.status === 401 && !refreshing && !original._retry) {
      original._retry = true;
      refreshing = true;
      try {
        const refreshToken = getRefreshToken();
        const { data } = await axios.post("/api/auth/refresh", { refreshToken });
        setTokens(data.accessToken, refreshToken!);
        original.headers.Authorization = `Bearer ${data.accessToken}`;
        return api(original);
      } catch {
        clearTokens();
        window.location.href = "/login";
      } finally {
        refreshing = false;
      }
    }
    return Promise.reject(error);
  }
);

export default api;
