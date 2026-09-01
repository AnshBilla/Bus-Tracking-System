import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import BASE_URL from '../config/api';

// Helper to parse JWT
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    return null;
  }
};

// Reusable NavLink
const NavLink = ({ children, to, onClick, className }) => (
  <Link
    to={to}
    onClick={onClick}
    className={`block text-base font-medium text-gray-800 hover:text-black hover:font-bold hover:shadow-2xl transition-all duration-200 px-3 py-2 rounded-md ${className}`}
  >
    {children}
  </Link>
);

const Logo = () => (
  <Link
    to="/"
    className="flex items-center space-x-1"
    onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}
  >
    <span className="text-3xl font-extrabold text-gray-900">Smartराही</span>
  </Link>
);

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  const handleCloseMenu = () => setIsOpen(false);

  useEffect(() => {
    const checkUserToken = () => {
      const token = localStorage.getItem("accessToken");
      if (token) {
        const decoded = parseJwt(token);
        let isGuest = false;

        if (decoded) {
          // String checks
          if (typeof decoded.role === 'string' && decoded.role.toLowerCase().includes('guest')) {
            isGuest = true;
          }
          if (typeof decoded.roles === 'string' && decoded.roles.toLowerCase().includes('guest')) {
            isGuest = true;
          }
          // Array checks
          if (Array.isArray(decoded.roles)) {
            const hasGuest = decoded.roles.some(r => 
              (typeof r === 'string' && r.toLowerCase().includes('guest')) || 
              (r.authority && r.authority.toLowerCase().includes('guest'))
            );
            if (hasGuest) isGuest = true;
          }
        }

        if (decoded && !isGuest) {
          setUser(decoded);
        } else {
          setUser(null);
        }
      } else {
        setUser(null);
      }
    };

    // Check on mount
    checkUserToken();

    // Listen to changes from login form
    window.addEventListener("auth-change", checkUserToken);
    
    return () => window.removeEventListener("auth-change", checkUserToken);
  }, []);

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    if (refreshToken) {
      try {
        await fetch(`${BASE_URL}/auth/logout`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ refreshToken })
        });
      } catch (e) {
        console.warn("Logout API failed, clearing local tokens anyway");
      }
    }
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setUser(null);
    navigate("/");
    window.location.reload();
  };

  return (
    <nav className="w-full border-b border-gray-100 shadow-sm bg-white/30 fixed top-0 z-50 backdrop-blur-md">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">

          {/* Logo */}
          <div className="flex-shrink-0">
            <Logo />
          </div>

          {/* Desktop Nav Links */}
          <div className="hidden md:flex md:ml-6 flex-grow justify-start">
            <div className="flex space-x-10 ml-16">
              <NavLink to="/AboutUs" onClick={() => window.scrollTo({ top: 0, behavior: "smooth" })}>About</NavLink>
              <NavLink to="#footer" onClick={() => window.scrollTo({ top: document.getElementById("footer").offsetTop, behavior: "smooth" })}>Contact us</NavLink>
              <NavLink to="/" onClick={() => window.scrollTo({ top: window.scrollY, behavior: "smooth" })}>FeedBack</NavLink>
            </div>
          </div>

          {/* Desktop Action Buttons */}
          <div className="hidden md:flex items-center space-x-6">
            {user ? (
              <div className="flex items-center space-x-4">
                <span className="text-sm font-semibold text-gray-700">Hi, {user.sub || user.username || "User"}</span>
                <button
                  onClick={handleLogout}
                  className="px-6 py-2 bg-red-600 text-white hover:bg-red-700 transition-colors duration-200 focus:outline-none rounded-lg font-semibold"
                >
                  Log Out
                </button>
              </div>
            ) : (
              <NavLink
                to="/loginSignup"
                className="px-6 py-2 bg-black text-white hover:bg-blue-600 transition-colors duration-200 focus:outline-none rounded-lg"
              >
                Log In
              </NavLink>
            )}
          </div>

          {/* Mobile Menu Button */}
          <div className="md:hidden">
            <button
              onClick={() => setIsOpen(!isOpen)}
              type="button"
              className="inline-flex items-center justify-center p-2 rounded-md text-gray-400 hover:text-gray-900 focus:outline-none"
              aria-label="Toggle menu"
            >
              <svg
                className="block h-6 w-6"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                aria-hidden="true"
              >
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16m-7 6h7" />
              </svg>
            </button>
          </div>

        </div>
      </div>

      {/* Mobile Dropdown Menu */}
      {isOpen && (
        <div className="md:hidden bg-white border-t border-gray-100 shadow-md">
          <div className="px-2 pt-2 pb-3 space-y-1">
            <NavLink to="/AboutUs" onClick={handleCloseMenu}>About</NavLink>
            <NavLink to="#footer" onClick={handleCloseMenu}>Contact us</NavLink>
            <NavLink to="/" onClick={handleCloseMenu}>Feedback</NavLink>

            {/* Mobile Action Buttons */}
            <div className="mt-3 space-y-2">
              {user ? (
                <>
                  <div className="px-6 py-2 text-gray-700 font-semibold text-center">
                    Hi, {user.sub || user.username || "User"}
                  </div>
                  <button
                    onClick={() => { handleCloseMenu(); handleLogout(); }}
                    className="w-full px-6 py-2 bg-red-600 text-white hover:bg-red-700 rounded-lg transition-colors duration-200 text-center font-semibold"
                  >
                    Log Out
                  </button>
                </>
              ) : (
                <NavLink
                  to="/loginSignup"
                  onClick={handleCloseMenu}
                  className="w-full px-6 py-2 bg-black text-white hover:bg-sky-700 rounded-lg transition-colors duration-200 text-center"
                >
                  Log In
                </NavLink>
              )}
            </div>
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
