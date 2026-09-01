import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Bus, Map, Users, Settings, LogOut, Menu, X, LayoutDashboard } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import CityManager from '../components/admin/CityManager';
import BusManager from '../components/admin/BusManager';
import OverviewDashboard from '../components/admin/OverviewDashboard';
import AdminPortal from './AdminPortal'; // Ensure this points to the right place.

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

const AdminDashboard = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [isSidebarOpen, setIsSidebarOpen] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);
  const [isChecking, setIsChecking] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const checkAuth = () => {
      const token = localStorage.getItem("accessToken");
      if (!token) return false;
      
      const decoded = parseJwt(token);
      if (!decoded) return false;

      let isAdminRole = false;
      if (typeof decoded.role === 'string' && decoded.role.toLowerCase() === 'admin') isAdminRole = true;
      if (typeof decoded.roles === 'string' && decoded.roles.toLowerCase() === 'admin') isAdminRole = true;
      
      if (Array.isArray(decoded.roles)) {
        isAdminRole = decoded.roles.some(r => 
          (typeof r === 'string' && (r.toLowerCase() === 'admin' || r.toLowerCase() === 'role_admin')) || 
          (r.authority && r.authority.toLowerCase() === 'role_admin') ||
          (r.authority && r.authority.toLowerCase() === 'admin')
        );
      }
      
      return isAdminRole;
    };

    if (!checkAuth()) {
      navigate('/loginSignup');
    } else {
      setIsAdmin(true);
    }
    setIsChecking(false);
  }, [navigate]);

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'cities', label: 'Cities', icon: Map },
    { id: 'buses', label: 'Buses', icon: Bus },
    { id: 'staff', label: 'Staff Setup', icon: Users },
    { id: 'settings', label: 'Settings', icon: Settings, disabled: true },
  ];

  if (isChecking) {
    return <div className="min-h-screen flex items-center justify-center font-bold text-gray-500">Verifying Admin Access...</div>;
  }

  if (!isAdmin) return null;

  return (
    <div className="min-h-screen bg-gray-100 flex flex-col md:flex-row mt-16 font-inter">
      {/* Mobile Menu Toggle */}
      <div className="md:hidden bg-white p-4 flex justify-between items-center border-b border-gray-200">
        <h1 className="font-bold text-xl text-gray-800">Admin Control</h1>
        <button onClick={() => setIsSidebarOpen(!isSidebarOpen)} className="text-gray-600">
          {isSidebarOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Sidebar */}
      <AnimatePresence>
        {isSidebarOpen && (
          <motion.div 
            initial={{ x: -300 }} animate={{ x: 0 }} exit={{ x: -300 }}
            className="w-full md:w-64 bg-white border-r border-gray-200 flex flex-col h-[calc(100vh-64px)] sticky top-16 z-40 shadow-sm"
          >
            <div className="p-6 border-b border-gray-100 hidden md:block">
              <h2 className="text-2xl font-extrabold text-blue-700 tracking-tight">Admin<span className="text-gray-800">Panel</span></h2>
              <p className="text-xs text-gray-500 mt-1">Smart Rahi Infrastructure</p>
            </div>

            <div className="flex-1 overflow-y-auto py-4">
              <nav className="space-y-1 px-3">
                {menuItems.map((item) => {
                  const Icon = item.icon;
                  return (
                    <button
                      key={item.id}
                      onClick={() => !item.disabled && setActiveTab(item.id)}
                      disabled={item.disabled}
                      className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 text-left
                        ${activeTab === item.id 
                          ? 'bg-blue-50 text-blue-700 font-bold shadow-sm' 
                          : item.disabled 
                            ? 'text-gray-400 cursor-not-allowed opacity-50' 
                            : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900 font-medium'
                        }`}
                    >
                      <Icon size={20} className={activeTab === item.id ? 'text-blue-600' : ''} />
                      {item.label}
                      {item.disabled && <span className="ml-auto text-[10px] bg-gray-100 px-2 py-0.5 rounded-full">Coming Soon</span>}
                    </button>
                  );
                })}
              </nav>
            </div>
            
            <div className="p-4 border-t border-gray-100">
              <button onClick={() => navigate('/')} className="w-full flex items-center gap-3 px-4 py-3 text-red-600 hover:bg-red-50 rounded-xl transition-colors font-medium">
                <LogOut size={20} /> Exit Admin
              </button>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* Main Content Area */}
      <div className="flex-1 overflow-y-auto p-4 md:p-8">
        <motion.div
          key={activeTab}
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="max-w-6xl mx-auto"
        >
          {activeTab === 'dashboard' && <OverviewDashboard />}
          {activeTab === 'cities' && <CityManager />}
          {activeTab === 'buses' && <BusManager />}
          {activeTab === 'staff' && (
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden relative">
               {/* Embed the AdminPortal without its huge outer margins */}
               <div className="-mt-16 pb-10">
                  <AdminPortal />
               </div>
            </div>
          )}
        </motion.div>
      </div>
    </div>
  );
};

export default AdminDashboard;
