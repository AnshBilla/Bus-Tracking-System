import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { LogIn, UserPlus, Mail, Lock, User, Phone } from "lucide-react";
import { useNavigate } from "react-router-dom";
import BASE_URL from "../config/api";

const LoginSignup = () => {
  const [isLoginView, setIsLoginView] = useState(true);
  const navigate = useNavigate();

  // Form State
  const [formData, setFormData] = useState({
    username: "",
    fullName: "",
    email: "",
    phone: "",
    password: "",
    confirmPassword: "",
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.id]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      if (!isLoginView && formData.password !== formData.confirmPassword) {
        throw new Error("Passwords do not match!");
      }

      const endpoint = isLoginView ? "/auth/login" : "/auth/register/passenger";
      
      // Map state to Spring Boot DTOs
      const payload = isLoginView
        ? { username: formData.username, password: formData.password }
        : {
            username: formData.username,
            email: formData.email,
            phone: formData.phone,
            password: formData.password,
            fullName: formData.fullName,
          };

      const res = await fetch(`${BASE_URL}${endpoint}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.message || "Authentication failed. Please try again.");
      }

      const data = await res.json();
      
      // Save Token and Redirect
      localStorage.setItem("accessToken", data.accessToken);
      navigate("/"); 
      
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const InputField = ({ id, label, type, placeholder, Icon }) => (
    <div className="relative">
      <label htmlFor={id} className="block text-sm font-semibold text-gray-700 mb-1">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          type={type}
          required
          placeholder={placeholder}
          value={formData[id]}
          onChange={handleInputChange}
          className="w-full pl-10 pr-4 py-3 border border-gray-300 rounded-lg
                      focus:border-blue-500 focus:ring-2 focus:ring-blue-400
                      outline-none transition-all duration-200"
        />
        <Icon
          size={20}
          className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"
        />
      </div>
    </div>
  );

  const GoogleIcon = () => (
    <svg viewBox="0 0 48 48" className="w-5 h-5 mr-3">
      <path fill="#FFC107" d="M43.6 20.4H24v7.7h11.7c-.5 3-2.3 5.7-4.9 7.4v4.5h6c3.7-3.4 5.8-8.4 5.8-14.6 0-1.8-.2-3.6-.6-5.3z" />
      <path fill="#FF3D00" d="M24 43.6c-5.5 0-10.4-1.8-13.8-4.9l-6 4.5c4.6 4.4 10.9 6.8 19.8 6.8 8.9 0 15.2-2.4 19.8-6.8l-6-4.5c-3.4 3.1-8.3 4.9-13.8 4.9z" />
      <path fill="#4CAF50" d="M10.2 28.1c-.4-1.2-.6-2.5-.6-3.8s.2-2.6.6-3.8V16h-6c-.7 1.4-1.1 3-1.1 4.7s.4 3.3 1.1 4.7z" />
      <path fill="#1976D2" d="M24 10.2c3 0 5.8 1.1 7.9 3.1l5.3-5.3C33.2 2.6 28.8 0 24 0 14.8 0 8.5 2.4 3.9 6.8l6 4.5c3.4-3.1 8.3-4.9 13.8-4.9z" />
    </svg>
  );

  return (
    <div className="pt-24 min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-100 via-blue-50 to-indigo-100 p-4 overflow-hidden">
      <div className="relative w-full max-w-6xl h-[700px] flex rounded-3xl shadow-2xl overflow-hidden bg-white/90 backdrop-blur-md border border-gray-200">
        {/* HERO PANEL */}
        <motion.div
          key={isLoginView ? "leftHero" : "rightHero"}
          initial={{ x: isLoginView ? "-100%" : "100%", opacity: 0 }}
          animate={{ x: 0, opacity: 1 }}
          exit={{ x: isLoginView ? "100%" : "-100%", opacity: 0 }}
          transition={{ duration: 0.8, ease: "easeInOut" }}
          className="hidden lg:flex flex-col justify-center items-center p-12 w-1/2
                      bg-gradient-to-br from-blue-600 to-blue-800 text-white
                      space-y-8 relative overflow-hidden left-0 top-0 h-full"
        >
          <div className="absolute inset-0 opacity-30">
            <div className="absolute top-10 left-10 w-32 h-32 bg-blue-400 rounded-full blur-3xl animate-pulse"></div>
            <div className="absolute bottom-10 right-10 w-40 h-40 bg-indigo-400 rounded-full blur-3xl animate-pulse delay-200"></div>
          </div>
          <div className="relative z-10 text-center px-6">
            <h2 className="text-4xl font-bold mb-2">
              {isLoginView ? "Welcome Back!" : "Join Smart Rahi Today!"}
            </h2>
            <p className="text-lg opacity-90">
              {isLoginView
                ? "Continue your journey with real-time transit tracking."
                : "Create your account and never miss a bus again!"}
            </p>
          </div>
        </motion.div>

        {/* FORM SECTION */}
        <div className="w-full lg:w-1/2 flex flex-col items-center justify-center p-8 sm:p-12 relative z-10 overflow-y-auto">
          <AnimatePresence mode="wait">
            <motion.div
              key={isLoginView ? "loginForm" : "signupForm"}
              initial={{ x: isLoginView ? "100%" : "-100%", opacity: 0 }}
              animate={{ x: 0, opacity: 1 }}
              exit={{ x: isLoginView ? "-100%" : "100%", opacity: 0 }}
              transition={{ duration: 0.8, ease: "easeInOut" }}
              className="w-full max-w-sm py-8"
            >
              {/* Centered Logo */}
              <div className="text-center mb-6">
                <h1 className="text-3xl font-extrabold text-gray-800">
                  <span className="text-black text-4xl">Smart Rahi</span>
                </h1>
                <p className="text-gray-500 mt-2">
                  {isLoginView ? "Sign in to your account" : "Create your new account"}
                </p>
              </div>

              {/* Error Message */}
              {error && (
                <div className="mb-4 p-3 bg-red-50 border-l-4 border-red-500 text-red-700 text-sm rounded">
                  {error}
                </div>
              )}

              {/* Google Auth */}
              <button className="flex items-center justify-center w-full py-3 border border-gray-300 rounded-lg text-gray-700 font-semibold mb-6 bg-white hover:bg-gray-50 transition duration-200 shadow-md">
                <GoogleIcon />
                {isLoginView ? "Sign in with Google" : "Sign up with Google"}
              </button>

              {/* Divider */}
              <div className="flex items-center mb-6">
                <div className="flex-grow border-t border-gray-300"></div>
                <span className="flex-shrink mx-4 text-gray-400 text-sm">or</span>
                <div className="flex-grow border-t border-gray-300"></div>
              </div>

              {/* Form */}
              <form className="space-y-4" onSubmit={handleSubmit}>
                {!isLoginView && (
                  <InputField id="fullName" label="Full Name" type="text" placeholder="John Smith" Icon={User} />
                )}
                <InputField
                  id="username"
                  label="Username"
                  type="text"
                  placeholder="johnsmith007"
                  Icon={User}
                />
                {!isLoginView && (
                  <>
                    <InputField id="email" label="Email Address" type="email" placeholder="john@example.com" Icon={Mail} />
                    <InputField id="phone" label="Phone Number" type="tel" placeholder="+91 9876543210" Icon={Phone} />
                  </>
                )}
                <InputField id="password" label="Password" type="password" placeholder="********" Icon={Lock} />
                {!isLoginView && (
                  <InputField id="confirmPassword" label="Confirm Password" type="password" placeholder="Confirm your password" Icon={Lock} />
                )}

                {isLoginView && (
                  <div className="flex justify-end">
                    <a href="#" className="text-sm text-blue-600 hover:underline font-medium">Forgot password?</a>
                  </div>
                )}

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={loading}
                  className="w-full py-3 mt-4 rounded-lg text-white font-bold
                              bg-gradient-to-r from-blue-600 to-blue-800
                             hover:from-indigo-600 hover:to-blue-600 transition-all duration-300
                              shadow-md hover:shadow-xl hover:scale-[1.02] flex items-center justify-center space-x-2 disabled:opacity-70"
                >
                  {loading ? "Processing..." : isLoginView ? "Sign In" : "Sign Up"}
                  {!loading && (isLoginView ? <LogIn size={18} /> : <UserPlus size={18} />)}
                </button>
              </form>

              {/* Toggle */}
              <p className="mt-8 text-center text-sm text-gray-600">
                {isLoginView ? "New here?" : "Already have an account?"}
                <button
                  type="button" 
                  onClick={() => {
                    setIsLoginView(!isLoginView);
                    setError("");
                    setFormData({ username: "", fullName: "", email: "", phone: "", password: "", confirmPassword: "" });
                  }} 
                  className="ml-1 font-semibold text-blue-600 hover:underline"
                >
                  {isLoginView ? "Create one" : "Sign in"}
                </button>
              </p>
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </div>
  );
};

export default LoginSignup;