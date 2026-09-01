import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import BASE_URL from '../config/api';

const AdminPortal = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    phone: '',
    password: '',
    role: 'driver', // default
    fullName: '',
    address: '',
    aadhar: '',
    drivingLicense: '',
    employeeId: '',
    experienceYears: '',
    adminSecret: ''
  });

  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage('');
    setError('');

    try {
      const response = await fetch(`${BASE_URL}/auth/register/staff`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          experienceYears: formData.experienceYears ? parseInt(formData.experienceYears) : null
        }),
      });

      const data = await response.json();

      if (response.ok) {
        setMessage(`Successfully registered ${formData.role}: ${formData.username}`);
        // Reset specific fields
        setFormData(prev => ({
          ...prev,
          username: '', email: '', phone: '', password: '', 
          fullName: '', address: '', aadhar: '', drivingLicense: '', 
          employeeId: '', experienceYears: '', adminSecret: ''
        }));
      } else {
        setError(data.message || data.error || 'Failed to register staff');
      }
    } catch (err) {
      setError('Network error occurred while registering');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-20 px-4 sm:px-6 lg:px-8 font-inter">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="max-w-2xl w-full mx-auto bg-white rounded-3xl shadow-xl overflow-hidden mt-10"
      >
        <div className="bg-blue-700 py-8 px-10 text-white">
          <h2 className="text-3xl font-extrabold tracking-tight">Admin & Staff Portal</h2>
          <p className="mt-2 text-blue-100 text-sm">Register new drivers, operators, or admins to the system.</p>
        </div>

        <div className="px-10 py-8">
          {message && (
            <div className="mb-6 bg-green-50 border-l-4 border-green-500 p-4 rounded-md">
              <p className="text-sm text-green-700 font-medium">{message}</p>
            </div>
          )}
          {error && (
            <div className="mb-6 bg-red-50 border-l-4 border-red-500 p-4 rounded-md">
              <p className="text-sm text-red-700 font-medium">{error}</p>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            
            {/* Role Selection */}
            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">Staff Role *</label>
              <select
                name="role"
                value={formData.role}
                onChange={handleChange}
                className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl focus:ring-blue-500 focus:border-blue-500 block p-3 transition-colors"
                required
              >
                <option value="driver">Driver</option>
                <option value="operator">Operator</option>
                <option value="admin">Admin</option>
              </select>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Basic Details */}
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Username *</label>
                <input type="text" name="username" value={formData.username} onChange={handleChange} required className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
              </div>
              
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Email *</label>
                <input type="email" name="email" value={formData.email} onChange={handleChange} required className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Phone *</label>
                <input type="tel" name="phone" value={formData.phone} onChange={handleChange} required className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Password *</label>
                <input type="password" name="password" value={formData.password} onChange={handleChange} required minLength="8" className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Full Name</label>
                <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
              </div>

              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">Address</label>
                <input type="text" name="address" value={formData.address} onChange={handleChange} className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
              </div>
            </div>

            <div className="border-t border-gray-200 my-6 pt-6">
              <h3 className="text-lg font-bold text-gray-900 mb-4">Role Specific Details</h3>
              <AnimatePresence mode="popLayout">
                
                {/* Aadhar is required for everyone except passenger, but in staff it's basically everyone */}
                <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="mb-4">
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Aadhar Number *</label>
                  <input type="text" name="aadhar" value={formData.aadhar} onChange={handleChange} required className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
                </motion.div>

                {(formData.role === 'driver' || formData.role === 'operator') && (
                  <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="mb-4">
                    <label className="block text-sm font-semibold text-gray-700 mb-1">Employee ID *</label>
                    <input type="text" name="employeeId" value={formData.employeeId} onChange={handleChange} required className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
                  </motion.div>
                )}

                {formData.role === 'driver' && (
                  <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-4">
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">Driving License *</label>
                      <input type="text" name="drivingLicense" value={formData.drivingLicense} onChange={handleChange} required className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
                    </div>
                    <div>
                      <label className="block text-sm font-semibold text-gray-700 mb-1">Experience (Years)</label>
                      <input type="number" name="experienceYears" value={formData.experienceYears} onChange={handleChange} className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" />
                    </div>
                  </motion.div>
                )}

                {formData.role === 'admin' && (
                  <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: 'auto' }} exit={{ opacity: 0, height: 0 }} className="mb-4">
                    <label className="block text-sm font-semibold text-gray-700 mb-1">System Admin Secret *</label>
                    <input type="password" name="adminSecret" value={formData.adminSecret} onChange={handleChange} required className="w-full bg-gray-50 border border-gray-300 text-gray-900 rounded-xl px-4 py-2" placeholder="Required to create new admins" />
                  </motion.div>
                )}

              </AnimatePresence>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full text-white font-bold py-4 px-6 rounded-xl shadow-lg transition-all ${
                loading ? 'bg-blue-400 cursor-not-allowed' : 'bg-blue-700 hover:bg-blue-800 hover:shadow-xl hover:-translate-y-1'
              }`}
            >
              {loading ? 'Processing...' : `Register ${formData.role.charAt(0).toUpperCase() + formData.role.slice(1)}`}
            </button>
          </form>
        </div>
      </motion.div>
    </div>
  );
};

export default AdminPortal;
