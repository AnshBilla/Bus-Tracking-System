import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion"; // optional for smooth animation

const FloatingToggle = () => {
  const [insideBus, setInsideBus] = useState(false);
  const [open, setOpen] = useState(false);

  return (
    <>
      {/* Floating toggle button (like chatbot icon) */}
      <button
        className="fixed bottom-6 right-6 bg-blue-600 text-white w-14 h-14 rounded-full shadow-lg font-semibold flex items-center justify-center z-[1000]"
        onClick={() => setOpen(!open)}
      >
        {open ? "×" : "Bus"}
      </button>

      {/* Toggle Panel */}
      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 20 }}
            className="fixed bottom-24 right-6 w-64 bg-white shadow-2xl rounded-xl p-4 z-[999]"
          >
            <h3 className="text-lg font-bold text-gray-800 mb-3">
              Inside this bus?
            </h3>

            {/* Toggle Switch */}
            <label className="flex items-center cursor-pointer">
              <div className="relative">
                <input
                  type="checkbox"
                  className="sr-only"
                  checked={insideBus}
                  onChange={() => setInsideBus(!insideBus)}
                />
                <div className="w-12 h-6 bg-gray-300 rounded-full shadow-inner"></div>
                <div
                  className={`dot absolute w-6 h-6 bg-white rounded-full shadow -left-1 -top-0.5 transition ${
                    insideBus ? "translate-x-full bg-blue-600" : ""
                  }`}
                ></div>
              </div>
              <span className="ml-3 font-medium text-gray-700">
                {insideBus ? "Yes" : "No"}
              </span>
            </label>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
};

export default FloatingToggle;