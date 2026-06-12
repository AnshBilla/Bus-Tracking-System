import React, { useState, useEffect, useMemo, useCallback } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import AsyncSelect from "react-select/async";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  CircleMarker,
  Tooltip,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import { fetchWithAuth } from "../config/api";

const Hero = () => {
  const [pickup, setPickup] = useState(null);
  const [destination, setDestination] = useState(null);
  const [tripId, setTripId] = useState("");
  const [message, setMessage] = useState("");
  const [nearbyStops, setNearbyStops] = useState([]);
  const [mapNearbyStops, setMapNearbyStops] = useState([]);

  const navigate = useNavigate();
  const defaultCenter = useMemo(() => [12.9716, 77.5946], []);

  // 1. Fetch nearby stops (Cleaned UI, No Duplicates)
  const fetchNearbyStops = useCallback(async (lat, lon) => {
    try {
      const res = await fetchWithAuth(`/passenger/stops/near?lat=${lat}&lon=${lon}&radius=1000&page=0&size=20`);
      if (!res.ok) throw new Error("Failed to fetch nearby stops");
      const data = await res.json();

      const uniqueStops = [];
      const seenNames = new Set();
      
      (data || []).forEach((stop) => {
        if (!seenNames.has(stop.stopName)) {
           seenNames.add(stop.stopName);
           uniqueStops.push({
             value: stop.stopName, // Backend uses this name for routing
             label: stop.stopName, // Clean UI, no ID
             id: stop.stopId,      
             lat: stop.stopLat,
             lon: stop.stopLon,
           });
        }
      });

      setNearbyStops(uniqueStops);
      setMapNearbyStops(uniqueStops.slice(0, 20));
    } catch (err) {
      console.error("Nearby stops error:", err);
    }
  }, []);

  // Run when pickup changes
  useEffect(() => {
    const lat = pickup ? pickup.lat : defaultCenter[0];
    const lon = pickup ? pickup.lon : defaultCenter[1];

    fetchNearbyStops(lat, lon);
  }, [pickup, defaultCenter, fetchNearbyStops]);

  // 2. Destination Search
  const loadDestinationOptions = useCallback(async (inputValue) => {
    // FIX 1: Return empty array here. `defaultOptions` prop handles nearby stops automatically!
    if (!inputValue || inputValue.trim().length < 2) return [];

    try {
      const encodedQuery = encodeURIComponent(inputValue.trim());
      const res = await fetchWithAuth(`/passenger/stops/search?query=${encodedQuery}`);
      if (!res.ok) throw new Error("Search failed on backend");
      const data = await res.json();

      const uniqueStops = [];
      const seenNames = new Set();
      
      data.forEach((stop) => {
        if (!seenNames.has(stop.stopName)) {
           seenNames.add(stop.stopName);
           uniqueStops.push({
             value: stop.stopName,
             label: stop.stopName,
             id: stop.stopId,
             lat: stop.stopLat,
             lon: stop.stopLon,
           });
        }
      });
      return uniqueStops;
    } catch (error) {
      console.error("Destination search failed:", error);
      return [];
    }
  }, []); // removed nearbyStops dependency

  // Handle Search
  const handleRequest = async () => {
    if (tripId.trim()) {
      setMessage(`🔍 Searching trip ID: ${tripId}...`);
      try {
        const res = await fetchWithAuth(`/trips/${tripId.trim()}`);
        if (!res.ok) {
          setMessage("❌ Trip not found!");
          return;
        }
        const tripData = await res.json();
        navigate("/eta", {
          state: {
            tripId: tripId.trim(),
            tripData,
          },
        });
      } catch {
        setMessage("❌ Something went wrong. Try again!");
      }
    } else if (pickup && destination) {
      setMessage("🚍 Fetching your buses...");
      navigate("/buses", {
        state: {
          pickup: pickup.value,
          pickupId: pickup.id,
          destination: destination.value,
          destinationId: destination.id,
        },
      });
    } else {
      setMessage("⚠ Please enter a Trip ID OR select both stops.");
    }
  };

  return (
    <div className="relative min-h-screen bg-white flex flex-col items-center pt-24 px-6 lg:px-16">
      <motion.div
        initial={{ y: -25, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        className="text-center mb-14"
      >
        <h1 className="text-5xl font-extrabold text-gray-900">
          Track ⚡{" "}
          <span className="bg-gradient-to-r from-blue-600 to-blue-800 bg-clip-text text-transparent">
            Every Bus
          </span>
        </h1>
        <p className="text-lg text-gray-600 mt-3">
          Live tracking made simple 🚍📍
        </p>
      </motion.div>

      <div className="flex flex-col lg:flex-row w-full gap-10">
        <div className="w-full lg:w-1/2 space-y-8">
          <div className="bg-white p-6 rounded-xl shadow border">
            <label className="text-sm font-semibold">From (Nearby)</label>
            
            {/* FIX 2: Removed the `key` prop so it doesn't destroy itself! */}
            <AsyncSelect
              defaultOptions={nearbyStops} 
              loadOptions={loadDestinationOptions} 
              value={pickup}
              onChange={(val) => {
                setPickup(val);
                setTripId("");
              }}
              placeholder="Search starting stop..."
              isClearable
            />

            <label className="text-sm font-semibold mt-4 block">To</label>
            
            {/* FIX 2: Removed the `key` prop here too! */}
            <AsyncSelect
              defaultOptions={nearbyStops} 
              loadOptions={loadDestinationOptions}
              value={destination}
              onChange={(val) => {
                setDestination(val);
                setTripId("");
              }}
              placeholder="Search destination stop..."
              isClearable
            />

            <button
              onClick={handleRequest}
              className="w-full mt-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Find Buses
            </button>
          </div>

          <div className="bg-white p-6 rounded-xl shadow border">
            <label className="text-sm font-semibold">Track by Trip ID</label>
            <input
              value={tripId}
              onChange={(e) => {
                setTripId(e.target.value);
                setPickup(null);
                setDestination(null);
              }}
              className="w-full p-3 border rounded mt-2"
            />
            <button
              onClick={handleRequest}
              className="w-full mt-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Track Trip
            </button>
          </div>

          {message && (
            <p className="text-blue-600 text-center">{message}</p>
          )}
        </div>

        <div className="w-full lg:w-1/2 h-[500px]">
          <MapContainer
            center={pickup ? [pickup.lat, pickup.lon] : defaultCenter}
            zoom={15}
            className="h-full w-full"
          >
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            {pickup && (
              <CircleMarker center={[pickup.lat, pickup.lon]} radius={10}>
                <Popup>{pickup.value}</Popup>
              </CircleMarker>
            )}
            {mapNearbyStops.map((stop) => (
              <Marker key={stop.id} position={[stop.lat, stop.lon]}>
                <Popup>{stop.label}</Popup>
                <Tooltip>{stop.label}</Tooltip>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </div>
    </div>
  );
};

export default Hero;