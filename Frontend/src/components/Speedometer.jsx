import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';

const Speedometer = ({ onClose }) => {
  const [speed, setSpeed] = useState(0);
  const [error, setError] = useState(null);

  useEffect(() => {
    let watchId;
    
    if (!navigator.geolocation) {
      setError("Geolocation is not supported by your browser");
      return;
    }

    const handleSuccess = (position) => {
      // Speed is in meters per second, convert to km/h
      let speedMs = position.coords.speed;
      
      // If speed is null, the device cannot determine it (e.g. desktop)
      if (speedMs === null || isNaN(speedMs)) {
        // Just for demo purposes if not moving
        speedMs = 0;
      }
      
      const speedKmh = (speedMs * 3.6).toFixed(1);
      setSpeed(speedKmh);
    };

    const handleError = (err) => {
      console.warn("Geolocation Error:", err);
      setError("Failed to track location");
    };

    watchId = navigator.geolocation.watchPosition(handleSuccess, handleError, {
      enableHighAccuracy: true,
      maximumAge: 0,
      timeout: 5000
    });

    return () => {
      navigator.geolocation.clearWatch(watchId);
    };
  }, []);

  // Calculate rotation for speedometer needle (-90 to 90 degrees)
  // Max speed on dial = 120 km/h
  const maxSpeed = 120;
  const clampedSpeed = Math.min(speed, maxSpeed);
  const rotation = -90 + (clampedSpeed / maxSpeed) * 180;

  return (
    <motion.div 
      initial={{ opacity: 0, scale: 0.9, y: 20 }}
      animate={{ opacity: 1, scale: 1, y: 0 }}
      className="fixed bottom-6 right-6 md:right-10 bg-white rounded-2xl shadow-2xl p-6 border-2 border-blue-100 z-[999] w-72"
    >
      <button 
        onClick={onClose}
        className="absolute top-2 right-3 text-gray-400 hover:text-red-500 font-bold"
      >
        ×
      </button>
      
      <div className="text-center mb-4">
        <h3 className="font-bold text-gray-800">Live Bus Speed</h3>
        <p className="text-xs text-green-600 font-semibold animate-pulse">● You are tracking this bus</p>
      </div>

      {error ? (
        <div className="text-sm text-red-500 text-center py-4">{error}</div>
      ) : (
        <div className="relative flex flex-col items-center">
          {/* Speedometer Arc SVG */}
          <div className="relative w-48 h-24 overflow-hidden">
            <svg viewBox="0 0 100 50" className="w-full h-full drop-shadow-md">
              <path 
                d="M 10 50 A 40 40 0 0 1 90 50" 
                fill="none" 
                stroke="#e2e8f0" 
                strokeWidth="12" 
                strokeLinecap="round" 
              />
              <path 
                d="M 10 50 A 40 40 0 0 1 90 50" 
                fill="none" 
                stroke="url(#gradient)" 
                strokeWidth="12" 
                strokeLinecap="round" 
                strokeDasharray={`${(clampedSpeed / maxSpeed) * 125} 125`}
              />
              <defs>
                <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                  <stop offset="0%" stopColor="#3b82f6" />
                  <stop offset="50%" stopColor="#8b5cf6" />
                  <stop offset="100%" stopColor="#ef4444" />
                </linearGradient>
              </defs>
            </svg>
            
            {/* Needle */}
            <motion.div 
              className="absolute bottom-0 left-1/2 w-1 h-20 bg-gray-800 rounded-full origin-bottom -ml-[2px]"
              animate={{ rotate: rotation }}
              transition={{ type: "spring", stiffness: 60, damping: 15 }}
            />
            {/* Center dot */}
            <div className="absolute bottom-[-6px] left-1/2 -ml-2 w-4 h-4 bg-gray-900 rounded-full border-2 border-white shadow-sm" />
          </div>

          <div className="mt-2 text-center">
            <span className="text-4xl font-black text-gray-800 tracking-tighter">{speed}</span>
            <span className="text-sm font-semibold text-gray-500 ml-1">km/h</span>
          </div>
        </div>
      )}
    </motion.div>
  );
};

export default Speedometer;
