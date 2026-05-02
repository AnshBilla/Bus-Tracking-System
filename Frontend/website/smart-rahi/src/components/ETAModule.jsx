// ETAModule.jsx — Full width + FavouriteTrips styling + OSRM (Single route request)
import React, { useState, useEffect, useCallback } from "react";
import { MapContainer, TileLayer, Marker, Popup, Polyline } from "react-leaflet";
import { useLocation, useNavigate } from "react-router-dom";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import { fetchWithAuth } from "../config/api";

// const BASE = "https://smart-rahi-gun5.onrender.com/api";

// Helpers
const toLocalDateFromTimeStr = (t) => {
  if (!t) return null;
  const [hh, mm, ss = "0"] = t.split(":");
  const now = new Date();
  return new Date(now.getFullYear(), now.getMonth(), now.getDate(), hh, mm, ss);
};
const formatClock = (date) =>
  date?.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) ?? "—";
const calculateRemaining = (date) => {
  if (!date) return "Unknown";
  const diff = Math.round((date.getTime() - Date.now()) / 60000);
  if (diff <= 0) return "Arrived";
  if (diff < 60) return `${diff}m`;
  return `${Math.floor(diff / 60)}h ${diff % 60}m`;
};

// Marker icons (FavouriteTrips style)
const pickupIcon = L.divIcon({
  className: "custom-map-icon",
  html: '<div class="p-2 bg-white rounded-full shadow-xl border-4 border-orange-500"></div>',
  iconSize: [40, 40],
  iconAnchor: [20, 40],
});
const destinationIcon = L.divIcon({
  className: "custom-map-icon",
  html: '<div class="w-4 h-4 rounded-full bg-green-600 border-2 border-white shadow"></div>',
  iconSize: [16, 16],
  iconAnchor: [8, 8],
});
const courierIcon = L.divIcon({
  className: "custom-map-icon",
  html: '<div class="p-1 bg-white rounded-full shadow border-2 border-blue-600"></div>',
  iconSize: [30, 30],
  iconAnchor: [15, 15],
});

const CloseIcon = () => (
  <span className="text-2xl font-bold cursor-pointer text-white opacity-90 hover:opacity-100">×</span>
);

const ETAModule = () => {
  const { state } = useLocation();
  const navigate = useNavigate();

  const tripData = state?.tripData ?? null;
  const tripId = state?.tripId || tripData?.gtfsTripId || tripData?.tripId;

  const [trip, setTrip] = useState(tripData);
  const [stops, setStops] = useState([]);
  const [map, setMap] = useState(null);
  const [routeCoords, setRouteCoords] = useState([]); // OSRM route lat-lng pairs
  const [liveBusCoords, setLiveBusCoords] = useState(null);

  const mapTripStops = (t) =>
    t?.tripStops?.map((ts) => ({
      id: ts.id,
      name: ts.stop?.stopName,
      coords: [ts.stop?.stopLat, ts.stop?.stopLon],
      eta: toLocalDateFromTimeStr(ts.expectedArrivalTime),
      original: ts,
    })) ?? [];

  // Fetch updated live ETA every minute
  const fetchLatest = useCallback(async () => {
    if (!tripId) return;
    try {
      const routeId = tripData?.routeId || tripData?.route?.routeId;
     const liveRes = await fetchWithAuth(`/passenger/buses/live${routeId ? `?routeId=${routeId}` : ''}`);
     if (liveRes.ok) {
         const liveBuses = await liveRes.json();
         // Find your specific bus from the active buses
         const myBus = liveBuses.find(b => b.tripId === tripId);
         
         if (myBus && myBus.currentLat && myBus.currentLon) {
             // Update the live bus marker
             setLiveBusCoords([myBus.currentLat, myBus.currentLon]);
         }
      }
    } catch (err) {
      console.warn("fetchLatest error:", err);
    }
  }, [tripId, tripData]);

  useEffect(() => {
    if (tripData) setStops(mapTripStops(tripData));
    fetchLatest();
    const id = setInterval(fetchLatest, 60000);
    return () => clearInterval(id);
  }, [fetchLatest, tripData]);

  // When stops change, request OSRM route (single request) and set routeCoords only when OSRM returns
  useEffect(() => {
    let cancelled = false;
    const loadOsrmRoute = async () => {
      setRouteCoords([]); // clear previous route until OSRM returns (Option A)
      if (!stops || stops.length < 2) return;

      try {
        const coordsParam = stops.map((s) => `${s.coords[1]},${s.coords[0]}`).join(";");
        const url = `https://router.project-osrm.org/route/v1/driving/${coordsParam}?overview=full&geometries=geojson`;
        const res = await fetch(url);
        const json = await res.json();
        const coords = json?.routes?.[0]?.geometry?.coordinates ?? null;
        if (!coords || !Array.isArray(coords) || coords.length === 0) {
          console.warn("OSRM returned no geometry, not rendering route.");
          return;
        }
        // convert [lon, lat] -> [lat, lon]
        const latLngs = coords.map((c) => [c[1], c[0]]);
        if (!cancelled) {
          setRouteCoords(latLngs);
          // fit map bounds to route if map present
          if (map && latLngs.length > 0) {
            try {
              const bounds = L.latLngBounds(latLngs);
              map.fitBounds(bounds, { padding: [60, 60] });
            } catch (e) {
              console.warn("fitBounds failed:", e);
            }
          }
        }
      } catch (err) {
        console.warn("OSRM fetch failed:", err);
      }
    };

    loadOsrmRoute();
    return () => {
      cancelled = true;
    };
  }, [stops, map]);

  const title = `${trip?.route?.routeName ?? ""} — ${trip?.headsign ?? "Trip"}`;
  const finalStop = stops.at(-1);
  const finalETA = finalStop?.eta ? calculateRemaining(finalStop.eta) : "N/A";

  // getMarker helper (FavouriteTrips style)
  const getMarker = (i, total) => {
    if (i === 0)
      return L.divIcon({
        className: "custom-map-marker",
        html: `<div class="p-2 bg-white rounded-full shadow-xl border-4 border-orange-500 w-6 h-6"></div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 15],
      });
    if (i === total - 1)
      return L.divIcon({
        className: "custom-map-marker",
        html: `<div class="p-2 bg-white rounded-full shadow-xl border-4 border-green-600 w-6 h-6"></div>`,
        iconSize: [30, 30],
        iconAnchor: [15, 15],
      });
    return L.divIcon({
      className: "custom-map-marker",
      html: `<div class="p-2 bg-white rounded-full shadow border-2 border-blue-600 w-3 h-3"></div>`,
      iconSize: [20, 20],
      iconAnchor: [10, 10],
    });
  };

return (
  <div>
    {/* New Clean Title Section */}
    <div className=" pb-2 flex flex-col items-center justify-center text-center pt-18">
  <h1 className="text-3xl font-extrabold text-blue-600 drop-shadow-sm">
    {title}
  </h1>

  <p className="text-gray-600 text-sm mt-2">
    Final ETA: <span className="font-semibold text-blue-700">{finalETA}</span>
  </p>

  {/* Blue underline like image */}
  <div className="mt-3 w-28 h-1.5 bg-blue-500 rounded-full"></div>
</div>

{/* Close button top-right */}
<button
  className="absolute top-4 right-4 bg-blue-600 text-white px-3 py-1 rounded-full shadow-lg hover:bg-blue-700"
  onClick={() => navigate(-1)}
>
  <CloseIcon />
</button>




      {/* Layout */}
      <div className="flex w-full h-[calc(100vh-72px)]">
        {/* Timeline */}
        <div className="w-full lg:w-1/2 overflow-y-auto px-6 py-4 space-y-6">
          <p className="text-xs text-gray-500">Live ETA refresh every 60s ✨</p>

          <ol className="border-l border-gray-300 pl-4 space-y-8">
            {stops.map((s, i) => (
              <li key={s.id ?? `${i}`} className="relative">
                <span
                  className={`absolute -left-3 top-2 w-3 h-3 rounded-full ${
                    i === stops.length - 1 ? "bg-green-600" : "bg-blue-600"
                  } border-2 border-white shadow`}
                />
                <div>
                  <h4 className="font-semibold text-gray-900">{s.name}</h4>
                  <p className="text-xs text-blue-700 font-medium mt-1">
                    ETA: {calculateRemaining(s.eta)} ({formatClock(s.eta)})
                  </p>
                </div>
              </li>
            ))}
          </ol>
        </div>

        {/* Map */}
        <div className="hidden lg:block w-1/2 h-full shadow-inner z-0">
          <MapContainer
            center={[12.97, 77.59]}
            zoom={14}
            scrollWheelZoom
            whenCreated={setMap}
            className="w-full h-full"
          >
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

            {/* Render OSRM route polyline only when returned */}
            {routeCoords && routeCoords.length > 0 && (
              <Polyline
                positions={routeCoords}
                pathOptions={{
                  weight: 4,
                  dashArray: "6 8",
                  color: "#2563eb",
                  opacity: 0.95,
                }}
              />
            )}

            {/* Markers always shown */}
            {stops.map((s, i) => (
              Array.isArray(s.coords) &&
              s.coords.length === 2 &&
              !isNaN(s.coords[0]) &&
              !isNaN(s.coords[1]) ? (
                <Marker key={s.id ?? i} position={s.coords} icon={getMarker(i, stops.length)}>
                  <Popup>
                    <div className="min-w-[160px]">
                      <div className="font-semibold">{s.name}</div>
                      <div className="text-xs text-gray-500 mt-1">
                        ETA: {s.eta ? `${calculateRemaining(s.eta)} (${formatClock(s.eta)})` : "Unknown"}
                      </div>
                      <div className="text-xs text-gray-400 mt-1">
                        Lat: {s.coords[0]}, Lon: {s.coords[1]}
                      </div>
                    </div>
                  </Popup>
                </Marker>
              ) : null
            ))}
          </MapContainer>
        </div>
      </div>
    </div>
  );
};

export default ETAModule;
