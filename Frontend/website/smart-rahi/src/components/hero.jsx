import React, { useState, useEffect, useMemo, useCallback } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import AsyncSelect from "react-select/async"; // ✅ Updated
import Select from "react-select"; // ✅ Keep for pickup nearby
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  CircleMarker,
  Tooltip,
} from "react-leaflet";
import "leaflet/dist/leaflet.css";
import BASE_URL from "../config/api";
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

  // ✅ Fetch nearby stops only
  useEffect(() => {
    const fetchNearbyStops = async () => {
      const lat = pickup ? pickup.lat : defaultCenter[0];
      const lon = pickup ? pickup.lon : defaultCenter[1];
      try {
        const res = await fetch(
          `/passenger/stops/near?lat=${lat}&lon=${lon}`
          
        );
        if (!res.ok) throw new Error("Failed to fetch nearby stops");
        const data = await res.json();
        const mappedStops = (data || []).map((s) => ({
          value: s.stopName,
          label: s.stopName,
          id: s.stopId,
          lat: s.stopLat,
          lon: s.stopLon,
        }));
        setNearbyStops(mappedStops);
        setMapNearbyStops(mappedStops.slice(0, 20));
      } catch (err) {
        console.error("Nearby stops error:", err);
      }
    };
    fetchNearbyStops();
  }, [defaultCenter, pickup]);

  // ✅ Destination Search API Loader
  const loadDestinationOptions = useCallback(async (inputValue) => {
    if (!inputValue || inputValue.length < 2) return [];

    try {
      const res = await fetchWithAuth(
        `/passenger/stops/search?query=${inputValue}`
      );
      if (!res.ok) throw new Error("Failed to search destinations");
      const data = await res.json();
      return data.map((stop) => ({
        value: stop.stopName,
        label: stop.stopName,
        id: stop.stopId,
        lat: stop.stopLatitude,
        lon: stop.stopLongitude,
      }));
    } catch (error) {
      console.error("Destination search failed:", error);
      return [];
    }
  }, []);

  // Navigation Handler
  const handleRequest = async () => {
    if (tripId.trim()) {
      setMessage(`🔍 Searching trip ID: ${tripId}...`);

      try {
        const res = await fetchWithAuth(
          `/trips/${tripId.trim()}`
        );
        if (!res.ok) {
           setMessage("Trip not found!");
           return;
        }
        const tripData = await res.json();

       

       

        navigate("/eta", {
          state: {
            tripId: tripId.trim(),
            
            tripData:tripData,
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
    <div className="relative min-h-screen font-inter bg-white flex flex-col items-center justify-start pt-24 px-6 lg:px-16 overflow-hidden">

      {/* Header */}
      <motion.div
        initial={{ y: -25, opacity: 0 }}
        animate={{ y: 0, opacity: 1 }}
        transition={{ duration: 1 }}
        className="text-center mb-14"
      >
        <h1 className="text-5xl md:text-6xl font-extrabold leading-tight text-gray-900">
          Track ⚡ <span className="bg-gradient-to-r from-blue-600 to-blue-800 bg-clip-text text-transparent">
            Every Bus
          </span>, Every Stop
        </h1>
        <p className="text-lg md:text-xl text-gray-600 mt-3 font-medium">
          All in one smart platform — live & accurate 🚍📍
        </p>
        <div className="mt-6 mx-auto w-48 h-1 bg-gradient-to-r from-blue-500 to-blue-700 rounded-full shadow-md"></div>
      </motion.div>

      {/* Search + Map */}
      <div className="flex flex-col lg:flex-row w-full items-start justify-between gap-10 lg:gap-16">

        {/* LEFT Panel */}
        <motion.div
          initial={{ opacity: 0, x: -50 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.8 }}
          className="w-full lg:w-1/2 text-center lg:text-left space-y-8"
        >

          {/* From-To Select */}
          <div className="bg-white p-8 rounded-2xl shadow-lg border border-gray-200 space-y-5">
            <div className="text-left">
              <label className="block text-sm font-semibold mb-1 text-gray-700">
                From Where? <span className="text-blue-500">(Nearby)</span>
              </label>
              <Select
                options={nearbyStops}
                value={pickup}
                onChange={(selected) => {
                  setPickup(selected);
                  setTripId("");
                }}
                placeholder="Select nearby stop"
                isSearchable
              />
            </div>

            <div className="text-left">
              <label className="block text-sm font-semibold mb-1 text-gray-700">
                To Where?
              </label>

              {/* ✅ Async Select for Destination */}
              <AsyncSelect
                value={destination}
                placeholder="Search destination"
                loadOptions={loadDestinationOptions}
                onChange={(e) => {
                  setDestination(e);
                  setTripId("");
                }}
                isClearable
                isSearchable
                defaultOptions={[]} // initially empty ✅
                cacheOptions
              />
            </div>

            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.97 }}
              onClick={handleRequest}
              className="w-full py-3 bg-blue-600 text-white font-semibold text-lg rounded-xl shadow-md hover:shadow-lg transition"
            >
              Find Buses
            </motion.button>
          </div>

          {/* OR */}
          <div className="flex items-center justify-center space-x-3">
            <hr className="flex-grow border-gray-300" />
            <span className="text-sm font-semibold text-gray-500">OR</span>
            <hr className="flex-grow border-gray-300" />
          </div>

          {/* Trip ID Search */}
          <div className="bg-white p-8 rounded-2xl shadow-lg border border-gray-200 space-y-5">
            <div className="text-left">
              <label className="block text-sm font-semibold mb-1 text-gray-700">
                Track by Trip ID
              </label>
              <input
                type="text"
                value={tripId}
                onChange={(e) => {
                  setTripId(e.target.value);
                  setPickup(null);
                  setDestination(null);
                }}
                placeholder="Enter Trip ID"
                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500 text-gray-800"
              />
            </div>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.97 }}
              onClick={handleRequest}
              className="w-full py-3 bg-blue-600 text-white font-semibold text-lg rounded-xl shadow-md hover:shadow-lg transition"
            >
              Track Trip
            </motion.button>
          </div>

          {message && (
            <p className="mt-3 text-center text-blue-600 italic">{message}</p>
          )}

          <p className="text-sm text-gray-500 mt-2">
            Found {nearbyStops.length} nearby stops
          </p>
        </motion.div>

        {/* Map Section */}
        <motion.div
          initial={{ opacity: 0, x: 60 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 1 }}
          className="relative w-full lg:w-1/2 h-[575px] rounded-2xl shadow-2xl overflow-hidden border border-gray-200 z-0"
        >
          <MapContainer
            center={pickup ? [pickup.lat, pickup.lon] : defaultCenter}
            zoom={15}
            scrollWheelZoom={true}
            className="h-full w-full"
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution="&copy; OpenStreetMap contributors"
            />

            {pickup && (
              <CircleMarker
                center={[pickup.lat, pickup.lon]}
                radius={10}
                color="#2563eb"
                fillColor="#3b82f6"
                fillOpacity={0.7}
              >
                <Popup>Selected Pickup: {pickup.value}</Popup>
              </CircleMarker>
            )}

            {mapNearbyStops.map((stop) => (
              <Marker key={stop.id} position={[stop.lat, stop.lon]}>
                <Popup>
                  <span className="font-semibold text-gray-800">
                    {stop.value}
                  </span>
                  <br />
                  Nearby
                </Popup>
                <Tooltip direction="top">{stop.value}</Tooltip>
              </Marker>
            ))}
          </MapContainer>
        </motion.div>
      </div>
    </div>
  );
};

export default Hero;
