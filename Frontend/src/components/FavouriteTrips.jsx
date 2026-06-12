import React, { useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  MapContainer,
  TileLayer,
  Marker,
  Popup,
  Polyline,
} from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import iconUrl from "leaflet/dist/images/marker-icon.png";
import iconShadowUrl from "leaflet/dist/images/marker-shadow.png";
import { fetchWithAuth } from "../config/api";

/* ------------------------- */
/* Leaflet Fix */
/* ------------------------- */
const DefaultIcon = L.icon({
  iconUrl,
  shadowUrl: iconShadowUrl,
  iconAnchor: [12, 41],
});
L.Marker.prototype.options.icon = DefaultIcon;

/* ------------------------- */
/* Config */
/* ------------------------- */
const TRIP_IDS = ["1", "10003", "10160", "10182", "10205"];

/* ------------------------- */
/* Utils */
/* ------------------------- */
const toLocalDate = (timeStr) => {
  if (!timeStr) return null;
  const now = new Date();
  const [h, m] = timeStr.split(":");
  return new Date(now.getFullYear(), now.getMonth(), now.getDate(), h, m);
};

const formatClock = (d) =>
  d ? d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }) : "";

const calculateRemaining = (d) => {
  if (!d) return "Live";
  const diff = Math.round((d - Date.now()) / 60000);
  if (diff <= 0) return "Arrived";
  return diff < 60 ? `${diff}m` : `${Math.floor(diff / 60)}h ${diff % 60}m`;
};

/* ------------------------- */
/* ETA MODAL */
/* ------------------------- */
const ETAModule = ({ trip, onClose }) => {
  const [stops, setStops] = useState([]);
  const [map, setMap] = useState(null);

  const fetchStops = useCallback(() => {
    let parsed = [];

    if (typeof trip.stops === "string") {
      try {
        parsed = JSON.parse(trip.stops);
      } catch {}
    } else {
      parsed = trip.stops || [];
    }

    const mapped = parsed
      .sort((a, b) => a.stopSequence - b.stopSequence)
      .map((s) => ({
        name: s.stopName,
        coords: [s.stopLat, s.stopLon],
        time: s.expectedArrivalTime
          ? toLocalDate(s.expectedArrivalTime)
          : null,
      }));

    setStops(mapped);
  }, [trip]);

  useEffect(() => {
    fetchStops();
    const id = setInterval(fetchStops, 60000);
    return () => clearInterval(id);
  }, [fetchStops]);

  useEffect(() => {
    if (!map || stops.length === 0) return;
    setTimeout(() => {
      map.invalidateSize();
      const bounds = L.latLngBounds(stops.map((s) => s.coords));
      map.fitBounds(bounds, { padding: [50, 50] });
    }, 300);
  }, [map, stops]);

  return (
    <motion.div
      className="fixed inset-0 bg-black/30 flex items-center justify-center"
      onClick={onClose}
    >
      <motion.div
        className="bg-white w-full max-w-5xl grid lg:grid-cols-2 rounded-xl overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* LEFT */}
        <div className="p-6 overflow-y-auto max-h-[600px]">
          <h2 className="text-xl font-bold mb-4">Route Details</h2>

          {stops.map((s, i) => (
            <div key={i} className="mb-4">
              <p className="font-semibold">{s.name}</p>
              <p className="text-sm text-blue-600">
                ETA: {calculateRemaining(s.time)}{" "}
                {s.time && `(${formatClock(s.time)})`}
              </p>
            </div>
          ))}
        </div>

        {/* MAP */}
        <div className="h-[500px]">
          <MapContainer
            center={stops[0]?.coords || [28.6, 77.2]}
            zoom={14}
            whenCreated={setMap}
            className="h-full"
          >
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />

            {stops.length > 1 && (
              <Polyline positions={stops.map((s) => s.coords)} />
            )}

            {stops.map((s, i) => (
              <Marker key={i} position={s.coords}>
                <Popup>{s.name}</Popup>
              </Marker>
            ))}
          </MapContainer>
        </div>
      </motion.div>
    </motion.div>
  );
};

/* ------------------------- */
/* MAIN */
/* ------------------------- */
const FavouriteTrips = () => {
  const [trips, setTrips] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTrips = async () => {
      const results = await Promise.all(
        TRIP_IDS.map(async (id) => {
          try {
            const res = await fetchWithAuth(`/trips/${id}`);
            if (!res.ok) return null;

            const trip = await res.json();

            let stops = [];
            if (typeof trip.stops === "string") {
              stops = JSON.parse(trip.stops);
            }

            const first = stops[0];
            const last = stops[stops.length - 1];

            const eta = last?.expectedArrivalTime
              ? calculateRemaining(toLocalDate(last.expectedArrivalTime))
              : "Live";

            return {
              id: trip.tripId,
              pickup: first?.stopName,
              destination: last?.stopName,
              stopsCount: stops.length,
              eta,
              raw: trip,
            };
          } catch {
            return null;
          }
        })
      );

      setTrips(results.filter(Boolean));
      setLoading(false);
    };

    fetchTrips();
  }, []);

  return (
    <div className="p-10 bg-gray-50 min-h-screen">
      <h1 className="text-3xl font-bold mb-6 text-center">
        Popular Routes
      </h1>

      <div className="grid md:grid-cols-3 gap-6">
        {loading ? (
          <p>Loading...</p>
        ) : (
          trips.map((t) => (
            <motion.div
              key={t.id}
              whileHover={{ y: -5 }}
              className="bg-white p-5 rounded-lg shadow"
            >
              <h2 className="font-semibold">
                {t.pickup} → {t.destination}
              </h2>

              <p className="text-sm text-gray-500">
                Stops: {t.stopsCount}
              </p>

              <p className="text-blue-600 font-semibold mt-2">
                ETA: {t.eta}
              </p>

              <div className="flex gap-2 mt-4">
                <button
                  onClick={() => setSelected(t.raw)}
                  className="flex-1 bg-blue-600 text-white py-2 rounded"
                >
                  Show ETA
                </button>

                <button
                  onClick={() =>
                    setTrips((prev) =>
                      prev.filter((p) => p.id !== t.id)
                    )
                  }
                  className="px-3 bg-gray-200 rounded"
                >
                  ✕
                </button>
              </div>
            </motion.div>
          ))
        )}
      </div>

      <AnimatePresence>
        {selected && (
          <ETAModule trip={selected} onClose={() => setSelected(null)} />
        )}
      </AnimatePresence>
    </div>
  );
};

export default FavouriteTrips;