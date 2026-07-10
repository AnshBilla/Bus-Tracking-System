// src/config/api.js
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";
export default BASE_URL;
// INSIDE config/api.js (Replace your fetchWithAuth function)

export const fetchWithAuth = async (endpoint, options = {}) => {
  let token = localStorage.getItem("accessToken");
  
  if (!token) {
    const guestRes = await fetch(`${BASE_URL}/auth/guest`, { method: 'POST' });
    const guestData = await guestRes.json();
    token = guestData.accessToken;
    localStorage.setItem("accessToken", token);
    // Note: Assuming your backend also returns a refreshToken for guests
    if (guestData.refreshToken) localStorage.setItem("refreshToken", guestData.refreshToken);
  }

  const headers = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
    ...options.headers,
  };

  let response = await fetch(`${BASE_URL}${endpoint}`, { ...options, headers });

  // Handle Token Expiration (401)
  if (response.status === 401 || response.status === 403) {
      console.warn("Token expired, attempting refresh...");
      const refreshToken = localStorage.getItem("refreshToken");
      
      if (refreshToken) {
          try {
              const refreshRes = await fetch(`${BASE_URL}/auth/refresh-token`, {
                  method: 'POST',
                  headers: { "Content-Type": "application/json" },
                  body: JSON.stringify({ refreshToken })
              });

              if (refreshRes.ok) {
                  const refreshData = await refreshRes.json();
                  localStorage.setItem("accessToken", refreshData.accessToken);
                  localStorage.setItem("refreshToken", refreshData.refreshToken);
                  
                  // Retry the original request with the new token
                  headers["Authorization"] = `Bearer ${refreshData.accessToken}`;
                  response = await fetch(`${BASE_URL}${endpoint}`, { ...options, headers });
              } else {
                  // Refresh failed, clear tokens so next request generates a new guest session
                  localStorage.removeItem("accessToken");
                  localStorage.removeItem("refreshToken");
              }
          } catch (e) {
              console.error("Refresh token network error", e);
          }
      }
  }

  return response;
};