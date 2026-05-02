// src/config/api.js
const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

export const fetchWithAuth = async (endpoint, options = {}) => {
  let token = localStorage.getItem("accessToken");

  // If no token exists, silently create a Guest session
  if (!token) {
    const guestRes = await fetch(`${BASE_URL}/auth/guest`, { method: 'POST' });
    const guestData = await guestRes.json();
    token = guestData.accessToken;
    localStorage.setItem("accessToken", token);
  }

  // Attach token to headers
  const headers = {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
    ...options.headers,
  };

  const response = await fetch(`${BASE_URL}${endpoint}`, { ...options, headers });
  
  if (response.status === 401 || response.status === 403) {
      // Handle token expiration logic here if needed
      console.error("Token expired or unauthorized");
  }

  return response;
};

export default BASE_URL;