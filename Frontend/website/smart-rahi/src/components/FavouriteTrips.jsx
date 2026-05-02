// FavouriteTripsWithETA.jsx — Elegant Light UI (Theme matched to Hero.jsx)
// Primary gradient: from-blue-500 to-blue-800

import React, { useState, useEffect, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { MapContainer, TileLayer, Marker, Popup, Polyline } from "react-leaflet";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import iconUrl from "leaflet/dist/images/marker-icon.png";
import iconShadowUrl from "leaflet/dist/images/marker-shadow.png";
import { fetchWithAuth } from "../config/api";

/* -------------------------
   Leaflet default marker fix
   ------------------------- */
const DefaultIcon = L.icon({
  iconUrl,
  shadowUrl: iconShadowUrl,
  iconAnchor: [12, 41],
});
L.Marker.prototype.options.icon = DefaultIcon;

/* -------------------------
   Config (using your provided baseUrl)
   ------------------------- */
   const TRIP_IDS = ["1", "10003", "10160", "10182", "10205", "10253"];
const BASE = "https://smart-rahi-gun5.onrender.com/api";
const API_URL = (id) => `${BASE}/trip/${id}?includeTripStops=true&limit=100000000`;

/* -------------------------
   Utils
   ------------------------- */
const toLocalDateFromTimeStr = (timeStr) => {
  // timeStr expected "HH:mm:ss" or "HH:mm"
  if (!timeStr) return null;
  const now = new Date();
  const [hh, mm, ss = "0"] = timeStr.split(":");
  if (hh == null || mm == null) return null;
  const d = new Date(now.getFullYear(), now.getMonth(), now.getDate(), parseInt(hh, 10), parseInt(mm, 10), parseInt(ss, 10));
  return d;
};

const formatClock = (date) => {
  if (!date || isNaN(date.getTime())) return "Unknown";
  return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
};

const calculateRemaining = (dateOrIso) => {
  if (!dateOrIso) return "Unknown";
  const date = typeof dateOrIso === "string" ? new Date(dateOrIso) : dateOrIso;
  if (isNaN(date.getTime())) return "Unknown";
  const diffMin = Math.round((date.getTime() - Date.now()) / 60000);
  if (diffMin <= 0) return "Arrived";
  if (diffMin < 60) return `${diffMin}m`;
  const h = Math.floor(diffMin / 60);
  const m = diffMin % 60;
  return `${h}h ${m ? `${m}m` : ""}`;
};

/* -------------------------
   Small Icon components
   ------------------------- */
const RouteIcon = ({ className = "w-5 h-5" }) => (
  <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden>
    <path d="M3 12h4l2-3 3 6 2-4 4 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

const ClockIcon = ({ className = "w-5 h-5" }) => (
  <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden>
    <path d="M12 7v5l3 1" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.5" />
  </svg>
);

const CloseIcon = ({ className = "w-5 h-5" }) => (
  <svg viewBox="0 0 24 24" fill="none" className={className} aria-hidden>
    <path d="M6 6l12 12M6 18L18 6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

/* -------------------------
   ETAModule: modal with timeline + map
   (includes leaflet invalidateSize + fitBounds fix)
   ------------------------- */
const ETAModule = ({ trip, onClose }) => {
  const [stops, setStops] = useState([]);
  const [map, setMap] = useState(null); // store leaflet map instance

  const mapCenter =
    trip?.tripStops && trip.tripStops.length > 0
      ? [trip.tripStops[0].stop.stopLat, trip.tripStops[0].stop.stopLon]
      : (trip?.pickupCoords?.length === 2 ? trip.pickupCoords : [29.47, 77.7]);

  const resolveStopTime = (stopObj) => {
    const calc = stopObj.calculatedArrivalTime ?? stopObj.calculatedDepartureTime;
    if (calc) {
      const d = new Date(calc);
      if (!isNaN(d.getTime())) return d;
    }
    const expected = stopObj.expectedArrivalTime ?? stopObj.expectedDepartureTime;
    if (expected && typeof expected === "string" && expected.match(/^\d{1,2}:\d{2}(:\d{2})?$/)) {
      const d = toLocalDateFromTimeStr(expected);
      if (d) return d;
    }
    return null;
  };

  const fetchStops = useCallback(async () => {
    try {
      if (trip?.tripStops && Array.isArray(trip.tripStops) && trip.tripStops.length > 0) {
        const mapped = trip.tripStops
          .slice()
          .sort((a, b) => (a.stopSequence ?? 0) - (b.stopSequence ?? 0))
          .map((ts) => {
            const resolved = resolveStopTime(ts) ?? (ts.expectedArrivalTime ? toLocalDateFromTimeStr(ts.expectedArrivalTime) : null);
            return {
              id: ts.id ?? `${ts.stop?.stopId}-${ts.stopSequence}`,
              name: ts.stop?.stopName ?? `Stop ${ts.stopSequence}`,
              coords: [ts.stop?.stopLat, ts.stop?.stopLon],
              resolvedDate: resolved,
              original: ts,
            };
          });

        setStops(mapped);
        return;
      }

      // fallback to route-details (if available)
      const res = await fetch(`${BASE}/route-details?routeId=${trip?.routeId ?? trip?.route?.id}`);
      if (res.ok) {
        const json = await res.json();
        const routeStops = json?.data?.stops ?? [];
        const mapped = routeStops.map((rs, idx) => ({
          id: rs.stopId ?? idx,
          name: rs.stopName ?? `Stop ${idx + 1}`,
          coords: [rs.stopLat, rs.stopLon],
          resolvedDate: null,
        }));
        setStops(mapped);
        return;
      }

      setStops([]);
    } catch (err) {
      console.error("ETAModule fetchStops error:", err);
      setStops([]);
    }
  }, [trip]);

  useEffect(() => {
    fetchStops();
    const id = setInterval(fetchStops, 60000);
    return () => clearInterval(id);
  }, [fetchStops]);

  // When map or stops change, invalidate size and fit bounds (after small delay to allow modal animation/layout)
  useEffect(() => {
    if (!map) return;
    // give modal time to finish animation/layout
    const t = setTimeout(() => {
      try {
        map.invalidateSize({ debounceMoveend: true });
        if (stops.length > 0) {
          // create bounds from stops coords
          const latlngs = stops.map((s) => s.coords);
          // filter out invalid coords
          const valid = latlngs.filter((c) => Array.isArray(c) && c.length === 2 && !isNaN(c[0]) && !isNaN(c[1]));
          if (valid.length === 1) {
            map.setView(valid[0], 14); // single point: zoom closer
          } else if (valid.length > 1) {
            const bounds = L.latLngBounds(valid);
            map.fitBounds(bounds, { padding: [60, 60] });
          }
        }
      } catch (e) {
        // ignore map errors
        console.warn("Map fit/invalidate error:", e);
      }
    }, 260); // matches modal animation timing; tweak if needed

    return () => clearTimeout(t);
  }, [map, stops]);

  const finalStop = stops.length ? stops[stops.length - 1] : null;
  const finalETA = finalStop?.resolvedDate ? calculateRemaining(finalStop.resolvedDate) : "N/A";
  const finalTimeStamp = finalStop?.resolvedDate ? formatClock(finalStop.resolvedDate) : "—";

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm p-4"
      onClick={onClose}
    >
      <motion.div
        initial={{ y: 20, scale: 0.98 }}
        animate={{ y: 0, scale: 1 }}
        exit={{ y: 20, scale: 0.98 }}
        className="w-full max-w-5xl bg-white rounded-2xl shadow-2xl grid grid-cols-1 lg:grid-cols-2 overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* TIMELINE */}
        <div className="p-6 max-h-[560px] overflow-y-auto">
          <div className="flex items-start justify-between mb-4">
            <div>
              <h3 className="text-xl font-bold text-gray-900">
                {trip?.route?.routeName ?? trip?.headsign ?? `${trip?.routeId ?? ""}`} {trip?.shapeId ? `· ${trip.shapeId}` : ""}
              </h3>
              <p className="text-sm text-gray-500 mt-1">
                Final ETA: <span className="font-semibold text-blue-600">{finalETA} {finalTimeStamp !== "—" ? `(${finalTimeStamp})` : ""}</span>
              </p>
            </div>
            <button onClick={onClose} className="text-gray-500 hover:text-gray-800 p-2 rounded-md">
              <CloseIcon />
            </button>
          </div>

          <div className="mt-4 space-y-4">
            <div className="p-3 bg-blue-50 border-l-4 border-blue-500 rounded-md text-sm text-gray-700">
              Live updates refresh every 60s.
            </div>

            <ol className="border-l border-gray-200 pl-5 mt-3 space-y-6">
              {stops.length === 0 ? (
                <div className="text-sm text-gray-500">No stops available</div>
              ) : (
                stops.map((s, idx) => {
                  const isLast = idx === stops.length - 1;
                  const remaining = s.resolvedDate ? calculateRemaining(s.resolvedDate) : "Unknown";
                  const ts = s.resolvedDate ? formatClock(s.resolvedDate) : (s.original?.expectedArrivalTime ?? "—");
                  return (
                    <li key={s.id} className="relative">
                      <span className={`absolute -left-6 top-2 w-3 h-3 rounded-full ${isLast ? "bg-green-500" : "bg-blue-500"} border-2 border-white shadow`} />
                      <div>
                        <div className="flex justify-between items-center">
                          <h4 className="font-semibold text-gray-800">{s.name}</h4>
                          <div className="flex items-center gap-2 text-sm">
                            <span className="text-gray-400">ETA</span>
                            <span className={`font-semibold ${isLast ? "text-green-600" : "text-blue-600"}`}>{remaining} {ts ? `(${ts})` : ""}</span>
                          </div>
                        </div>
                        {s.address && <p className="text-sm text-gray-500 mt-1">{s.address}</p>}
                        <div className="text-xs text-gray-400 mt-1">Lat: {s.coords?.[0] ?? "—"}, Lon: {s.coords?.[1] ?? "—"}</div>
                      </div>
                    </li>
                  );
                })
              )}
            </ol>

            <div className="mt-6 flex gap-3">
              <button onClick={onClose} className="flex-1 py-2 rounded-lg border border-gray-300 text-gray-700 hover:bg-gray-100">Close</button>
              <button onClick={() => alert("Open booking flow (not implemented)")} className="py-2 px-4 rounded-lg bg-gradient-to-r from-blue-500 to-blue-800 text-white font-semibold">Book</button>
            </div>
          </div>
        </div>

        {/* MAP */}
        <div className="w-full h-96 lg:min-h-[560px]">

          {/* Use whenCreated to capture map instance; style ensures full height */}
          <MapContainer
            center={mapCenter}
            zoom={15}
            scrollWheelZoom
            className="w-full h-full"
            whenCreated={(m) => setMap(m)}
            style={{ height: "100%", width: "100%" }}
          >
            <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            {stops.length > 0 && (
              <>
                {/* Road Polyline using OSRM routing */}
{stops.length > 1 && (
  <Polyline
    positions={[]}
    ref={async (ref) => {
      if (!ref || stops.length < 2) return;

      try {
        const coords = stops.map((s) => `${s.coords[1]},${s.coords[0]}`).join(";");
        const url = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;

        const res = await fetch(url);
        const json = await res.json();
        const route = json?.routes?.[0]?.geometry?.coordinates ?? [];

        const latLngs = route.map((c) => [c[1], c[0]]);
        ref.setLatLngs(latLngs);
      } catch (err) {
        console.warn("Routing failed, falling back to straight lines", err);
        ref.setLatLngs(stops.map((s) => s.coords));
      }
    }}
    pathOptions={{ color: "#2563eb", weight: 4 }}
  />
)}

                {stops.map((s, i) => (
                  <Marker key={i} position={s.coords}>
                    <Popup>
                      <div className="min-w-[160px]">
                        <div className="font-semibold">{s.name}</div>
                        <div className="text-xs text-gray-500 mt-1">ETA: {s.resolvedDate ? calculateRemaining(s.resolvedDate) + ` (${formatClock(s.resolvedDate)})` : (s.original?.expectedArrivalTime ?? "Unknown")}</div>
                        <div className="text-xs text-gray-400 mt-1">Lat: {s.coords?.[0]}, Lon: {s.coords?.[1]}</div>
                      </div>
                    </Popup>
                  </Marker>
                ))}
              </>
            )}
          </MapContainer>
        </div>
      </motion.div>
    </motion.div>
  );
};

/* -------------------------
   FavouriteTrips (Main)
   ------------------------- */
const FavouriteTrips = () => {
  const [favTrips, setFavTrips] = useState([]);
  const [selectedTrip, setSelectedTrip] = useState(null);
  const [loading, setLoading] = useState(true);

 useEffect(() => {
  const fetchFav = async () => {
    try {
      const results = await Promise.all(
        TRIP_IDS.map(async (tid) => {
          try {
            const res = await fetch(API_URL(tid));
            const json = await res.json();
            const trip = json?.data ?? null;
            if (!trip) return null;

            // Sort stops safely
            if (Array.isArray(trip.tripStops)) {
              trip.tripStops.sort(
                (a, b) =>
                  (a.stopSequence ?? 0) - (b.stopSequence ?? 0)
              );
            }

            const first = trip.tripStops?.[0];
            const last =
              trip.tripStops?.[trip.tripStops.length - 1];

            const formatETA = () => {
              if (!last) return "Live";
              const calc =
                last.calculatedArrivalTime ??
                last.calculatedDepartureTime;
              if (calc) {
                const d = new Date(calc);
                return isNaN(d.getTime())
                  ? "Live"
                  : `${calculateRemaining(d)} (${formatClock(d)})`;
              }
              if (last.expectedArrivalTime) {
                const d = toLocalDateFromTimeStr(
                  last.expectedArrivalTime
                );
                return d
                  ? `${calculateRemaining(d)} (${formatClock(d)})`
                  : "Live";
              }
              return "Live";
            };

            return {
              id:
                trip.tripId ??
                trip.gtfsTripId ??
                Math.random(),
              type: "route",
              pickup:
                first?.stop?.stopName ??
                trip.startStopId ??
                "Unknown",
              destination:
                last?.stop?.stopName ??
                trip.finalStopId ??
                "Unknown",
              shapeId: trip.shapeId ?? "—",
              stopsCount: trip.tripStops?.length ?? "—",
              pickupCoords:
                first?.stop
                  ? [
                      first.stop.stopLat,
                      first.stop.stopLon,
                    ]
                  : undefined,
              destinationCoords:
                last?.stop
                  ? [
                      last.stop.stopLat,
                      last.stop.stopLon,
                    ]
                  : undefined,
              __rawTrip: trip,
              eta: formatETA(),
            };
          } catch (e) {
            console.warn("Trip fetch failed", e);
            return null;
          }
        })
      );

      // Filter out failed trips
      const valid = results.filter(Boolean);
      setFavTrips(valid);
    } catch (err) {
      console.warn("Failed to fetch trips", err);
      setFavTrips([]);
    } finally {
      setLoading(false);
    }
  };

  fetchFav();
}, []);


  return (
    <div className="min-h-screen bg-gradient-to-b from-white via-slate-50 to-white font-inter py-12 px-6">
      <div className="max-w-7xl mx-auto">
        {/* Header (theme matched to Hero.jsx) */}
        <motion.div initial={{ y: -10, opacity: 0 }} animate={{ y: 0, opacity: 1 }} transition={{ duration: 0.6 }} className="text-center mb-8">
          <h2 className="text-3xl md:text-4xl font-extrabold tracking-tight text-gray-900">
            <span className="bg-gradient-to-r from-blue-600 to-blue-800  bg-clip-text text-transparent">Popular Bus Routes</span>
          </h2>
          <p className="text-gray-500 mt-2">Quick access to frequently taken trips — open ETA, view route, or book quickly.</p>
          <div className="mt-4 mx-auto w-36 h-1 bg-gradient-to-r from-blue-500 to-blue-800 rounded-full shadow-sm"></div>
        </motion.div>

        {/* Cards grid */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {loading ? (
            // skeleton grid
            Array.from({ length: 3 }).map((_, i) => (
              <div key={i} className="animate-pulse bg-white rounded-2xl p-6 border border-gray-100 shadow-sm">
                <div className="h-6 bg-gray-200 rounded w-3/4 mb-4"></div>
                <div className="h-4 bg-gray-200 rounded w-1/2 mb-6"></div>
                <div className="flex gap-3">
                  <div className="h-10 rounded-lg bg-gray-200 flex-1"></div>
                  <div className="h-10 rounded-lg bg-gray-200 w-20"></div>
                </div>
              </div>
            ))
          ) : favTrips.length === 0 ? (
            <div className="col-span-full bg-white rounded-2xl p-8 border border-gray-100 text-center text-gray-500 shadow-sm">
              No popular routes found
            </div>
          ) : (
            favTrips.map((t) => (
              <motion.div
                key={t.id}
                whileHover={{ translateY: -8, boxShadow: "0 12px 30px rgba(2,6,23,0.06)" }}
                className="bg-white rounded-2xl p-6 border border-gray-100 shadow-sm"
              >
                <div className="flex items-start justify-between gap-4">
                  <div className="flex items-center gap-3">
                    <div className="p-3 rounded-lg bg-gradient-to-br from-blue-50 to-indigo-50 border border-blue-100">
                      <RouteIcon className="w-6 h-6 text-blue-600" />
                    </div>
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900">{t.pickup} → {t.destination}</h3>
                      <p className="text-sm text-gray-500 mt-1 capitalize">
                        {t.type} {t.shapeId ? <span className="ml-2 text-xs text-gray-400">· {t.shapeId}</span> : null}
                      </p>
                    </div>
                  </div>

                  <div className="text-right">
                    <div className="text-xs text-gray-400">Stops</div>
                    <div className="text-sm font-medium text-gray-700">{t.stopsCount ?? "—"}</div>
                  </div>
                </div>

                <div className="mt-4 flex items-center gap-3">
                  <div className="flex-1">
                    <div className="text-xs text-gray-400">From</div>
                    <div className="font-medium text-gray-800">{t.pickup}</div>
                    <div className="text-xs text-gray-400 mt-2">To</div>
                    <div className="font-medium text-gray-800">{t.destination}</div>
                  </div>

                  <div className="w-28 text-right">
                    <div className="text-xs text-gray-400">ETA</div>
                    <div className="text-sm font-semibold text-blue-600 mt-1">{t.eta ?? "Live"}</div>
                  </div>
                </div>

                <div className="mt-5 flex gap-3">
                  <button
                    onClick={() => {
                      // when opening modal, pass the full trip object for modal to use
                      setSelectedTrip(t.__rawTrip ?? null);
                    }}
                    className="flex-1 py-2 rounded-lg bg-gradient-to-r from-blue-500 to-blue-800 text-white font-semibold hover:opacity-95 transition"
                  >
                    Show ETA
                  </button>
                  <button
                    onClick={() => setFavTrips((prev) => prev.filter((p) => p.id !== t.id))}
                    className="py-2 px-4 rounded-lg bg-gray-100 text-gray-700 hover:bg-gray-200"
                  >
                    Remove
                  </button>
                </div>
              </motion.div>
            ))
          )}
        </div>
      </div>

      {/* AnimatePresence for modal */}
      <AnimatePresence>
        {selectedTrip && <ETAModule trip={selectedTrip} onClose={() => setSelectedTrip(null)} />}
      </AnimatePresence>
    </div>
  );
};

export default FavouriteTrips;
