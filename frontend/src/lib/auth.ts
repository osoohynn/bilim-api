const ACCESS_KEY = "bilim_access";
const REFRESH_KEY = "bilim_refresh";

export const getAccessToken = () =>
  typeof window !== "undefined" ? localStorage.getItem(ACCESS_KEY) : null;

export const getRefreshToken = () =>
  typeof window !== "undefined" ? localStorage.getItem(REFRESH_KEY) : null;

export const setTokens = (access: string, refresh: string) => {
  localStorage.setItem(ACCESS_KEY, access);
  localStorage.setItem(REFRESH_KEY, refresh);
};

export const clearTokens = () => {
  localStorage.removeItem(ACCESS_KEY);
  localStorage.removeItem(REFRESH_KEY);
};

export const getUserId = (): number | null => {
  const token = getAccessToken();
  if (!token) return null;
  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.id ?? null;
  } catch {
    return null;
  }
};
