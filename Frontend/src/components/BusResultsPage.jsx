import React, { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { motion, AnimatePresence } from "framer-motion";
import "leaflet/dist/leaflet.css";
import BASE_URL from "../config/api";
import { fetchWithAuth } from "../config/api";

const BusRouteCard = ({ option, index, onViewMap }) => {
  const firstLeg = option.legs?.[0];
  if (!firstLeg) return null;
 const startStopName = firstLeg.fromStopName || "TBD";
  const endStopName = firstLeg.toStopName || "TBD";
  const departure = firstLeg.estimatedDeparture || "TBD";
  const arrival = firstLeg.estimatedArrival || "TBD";
  const duration = option.estimatedDuration || "TBD";

  return (
    <motion.li
      initial={{ opacity: 0, y: 40 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.05, duration: 0.4 }}
      className="bg-white rounded-2xl p-6 border border-gray-200 shadow-lg hover:shadow-xl hover:-translate-y-1 transition-all duration-300"
    >
      <div className="text-xs font-bold text-blue-700 mb-2 tracking-wider uppercase">
        Route
      </div>

      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
        <div>
          <div className="text-3xl font-extrabold text-blue-700 leading-tight">
          {firstLeg.routeName}
          </div>
          <p className="text-gray-600 text-sm mt-1">
            {startStopName} ➜ {endStopName}
          </p>
        </div>

        <div className="flex gap-10 text-center">
          <div>
            <div className="text-sm text-gray-500">Departure</div>
            <div className="font-bold text-lg text-gray-800">{departure}</div>
          </div>
          <div>
            <div className="text-sm text-gray-500">Duration</div>
            <div className="font-semibold text-lg text-orange-600">
              {duration}
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-500">Arrival</div>
            <div className="font-bold text-lg text-gray-800">{arrival}</div>
          </div>
        </div>

        <motion.button
          onClick={() => onViewMap(firstLeg.gtfsTripId)}
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.97 }}
          className="px-6 py-2 bg-blue-700 text-white rounded-xl shadow-md hover:shadow-lg text-sm font-semibold mt-2 md:mt-0"
        >
          View Route Map
        </motion.button>
      </div>
    </motion.li>
  );
};

const BusResultsPage = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const { pickupId, destinationId, pickup, destination } = location.state || {};

  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTrips = async () => {
      setLoading(true);
try {
  const encodedFrom = encodeURIComponent(pickup);
        const encodedTo = encodeURIComponent(destination);
        const res = await fetchWithAuth(`/passenger/journey?fromStopName=${encodedFrom}&toStopName=${encodedTo}`);
        if (!res.ok) throw new Error("Failed to fetch journeys");
        
        const data = await res.json();
        setResults(data.options || []); // Spring boot returns JourneyPlanResponse with 'options'
      } catch (err) {
        console.error(err);
        setResults([]);
      } finally {
        setLoading(false);
      }
    };
    if (pickup && destination) {
       fetchTrips();
    }
  }, [pickup, destination]);
 const handleMapView = (tripId) => {
    navigate("/eta", {
      state: { tripId: tripId },
    });
  };

  return (
    <div className="min-h-screen bg-white font-inter pt-28 pb-20">
      <div className="max-w-7xl mx-auto px-6 lg:px-10">
        <motion.div
          initial={{ opacity: 0, y: -20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.7 }}
          className="text-center mb-14"
        >
          <h1 className="text-5xl font-extrabold text-gray-900">
            Bus Schedules
          </h1>
          <p className="text-gray-600 mt-2 text-lg">
            From{" "}
            <span className="font-bold text-blue-700">{pickup}</span> to{" "}
            <span className="font-bold text-blue-700">{destination}</span>
          </p>
        </motion.div>

        {loading ? (
          <div className="flex justify-center items-center h-48">
            <motion.div
              animate={{ rotate: 360 }}
              transition={{ repeat: Infinity, duration: 1, ease: "linear" }}
              className="h-14 w-14 border-4 border-blue-700 border-t-transparent rounded-full"
            ></motion.div>
          </div>
        ) : results.length === 0 ? (
          <div className="text-center text-gray-600 text-lg font-semibold py-10">
            ❌ No buses found between{" "}
            <span className="text-blue-700">{pickup}</span> and{" "}
            <span className="text-blue-700">{destination}</span>
          </div>
        ) : (
          <AnimatePresence>
  <motion.ul initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="space-y-6">
    {results.map((r, i) => (
      <BusRouteCard
        key={r.legs?.[0]?.gtfsTripId || i} // Fixed Key
        option={r}                         // Fixed Prop name (changed 'route' to 'option')
        index={i}
        onViewMap={handleMapView}
      />
    ))}
  </motion.ul>
</AnimatePresence>

        )}
      </div>
    </div>
  );
};

export default BusResultsPage;
