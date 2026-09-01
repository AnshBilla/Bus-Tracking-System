import React, { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../config/api';
import { Edit2, Trash2, Plus, X } from 'lucide-react';

const BusManager = () => {
  const [buses, setBuses] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [stops, setStops] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingBus, setEditingBus] = useState(null);
  const [error, setError] = useState('');

  const [formData, setFormData] = useState({
    busId: '',
    busNumber: '',
    busType: 'Diesel',
    capacity: 40,
    operationalStatus: 'ACTIVE',
    occupancyStatus: 'EMPTY',
    currentLat: 0.0,
    currentLon: 0.0,
    routeId: '',
    nextStopId: ''
  });

  const loadData = async () => {
    setLoading(true);
    try {
      const [busRes, routeRes, stopRes] = await Promise.all([
        fetchWithAuth('/buses'),
        fetchWithAuth('/routes'),
        fetchWithAuth('/stops')
      ]);
      
      if (busRes.ok) setBuses(await busRes.json());
      if (routeRes.ok) setRoutes(await routeRes.json());
      if (stopRes.ok) setStops(await stopRes.json());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleInputChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    
    // Ensure numbers
    const payload = {
      ...formData,
      capacity: parseInt(formData.capacity),
      currentLat: parseFloat(formData.currentLat),
      currentLon: parseFloat(formData.currentLon)
    };

    try {
      const url = editingBus ? `/buses/${editingBus.busId}` : '/buses';
      const method = editingBus ? 'PUT' : 'POST';
      
      const res = await fetchWithAuth(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setIsModalOpen(false);
        setEditingBus(null);
        loadData();
      } else {
        const errData = await res.json().catch(() => ({}));
        setError(errData.message || 'Failed to save bus');
      }
    } catch (err) {
      setError('Network error');
    }
  };

  const handleDelete = async (busId) => {
    if (!window.confirm('Are you sure you want to delete this bus?')) return;
    try {
      const res = await fetchWithAuth(`/buses/${busId}`, { method: 'DELETE' });
      if (res.ok) loadData();
    } catch (err) {
      console.error(err);
    }
  };

  const openEdit = (bus) => {
    setEditingBus(bus);
    setFormData({
      busId: bus.busId,
      busNumber: bus.busNumber,
      busType: bus.busType || 'Diesel',
      capacity: bus.capacity || 40,
      operationalStatus: bus.operationalStatus || 'ACTIVE',
      occupancyStatus: bus.occupancyStatus || 'EMPTY',
      currentLat: bus.currentLat || 0.0,
      currentLon: bus.currentLon || 0.0,
      routeId: bus.routeId || '',
      nextStopId: bus.nextStopId || ''
    });
    setIsModalOpen(true);
  };

  const openAdd = () => {
    setEditingBus(null);
    setFormData({
      busId: '', busNumber: '', busType: 'Diesel', capacity: 40,
      operationalStatus: 'ACTIVE', occupancyStatus: 'EMPTY',
      currentLat: 0.0, currentLon: 0.0, routeId: '', nextStopId: ''
    });
    setIsModalOpen(true);
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center bg-gray-50">
        <div>
          <h2 className="text-xl font-bold text-gray-800">Manage Buses</h2>
          <p className="text-sm text-gray-500 mt-1">Register new buses, assign routes, and manage capacity.</p>
        </div>
        <button onClick={openAdd} className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-semibold flex items-center gap-2 transition-colors">
          <Plus size={18} /> Add Bus
        </button>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-100 text-gray-600 text-sm uppercase tracking-wider">
              <th className="px-6 py-3 font-semibold">Bus ID</th>
              <th className="px-6 py-3 font-semibold">Number</th>
              <th className="px-6 py-3 font-semibold">Route</th>
              <th className="px-6 py-3 font-semibold">Type</th>
              <th className="px-6 py-3 font-semibold">Status</th>
              <th className="px-6 py-3 font-semibold text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 text-sm">
            {loading ? (
              <tr><td colSpan="6" className="px-6 py-8 text-center text-gray-500">Loading buses...</td></tr>
            ) : buses.length === 0 ? (
              <tr><td colSpan="6" className="px-6 py-8 text-center text-gray-500">No buses found.</td></tr>
            ) : (
              buses.map((bus) => (
                <tr key={bus.busId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 font-mono text-xs">{bus.busId}</td>
                  <td className="px-6 py-4 font-medium">{bus.busNumber}</td>
                  <td className="px-6 py-4">{bus.routeId || 'N/A'}</td>
                  <td className="px-6 py-4">{bus.busType}</td>
                  <td className="px-6 py-4">
                    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${bus.operationalStatus === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {bus.operationalStatus}
                    </span>
                  </td>
                  <td className="px-6 py-4 flex justify-end gap-3">
                    <button onClick={() => openEdit(bus)} className="p-2 text-blue-600 hover:bg-blue-50 rounded-md"><Edit2 size={16} /></button>
                    <button onClick={() => handleDelete(bus.busId)} className="p-2 text-red-600 hover:bg-red-50 rounded-md"><Trash2 size={16} /></button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm overflow-y-auto">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-2xl my-8">
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center sticky top-0 bg-white rounded-t-xl z-10">
              <h3 className="text-lg font-bold">{editingBus ? 'Edit Bus' : 'Add New Bus'}</h3>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600"><X size={20} /></button>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {error && <div className="p-3 bg-red-50 text-red-700 text-sm rounded-md">{error}</div>}
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Bus ID *</label>
                  <input type="text" name="busId" value={formData.busId} onChange={handleInputChange} required disabled={!!editingBus} className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 bg-gray-50" />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Bus Number (Plate) *</label>
                  <input type="text" name="busNumber" value={formData.busNumber} onChange={handleInputChange} required className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Bus Type</label>
                  <select name="busType" value={formData.busType} onChange={handleInputChange} className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500">
                    <option value="Electric">Electric</option>
                    <option value="Diesel">Diesel</option>
                    <option value="CNG">CNG</option>
                    <option value="Hybrid">Hybrid</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Capacity</label>
                  <input type="number" name="capacity" value={formData.capacity} onChange={handleInputChange} required className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500" />
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Route Assignment *</label>
                  <select name="routeId" value={formData.routeId} onChange={handleInputChange} required className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500">
                    <option value="">Select a Route...</option>
                    {routes.map(r => <option key={r.routeId} value={r.routeId}>{r.routeName} ({r.routeId})</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Next Stop *</label>
                  <select name="nextStopId" value={formData.nextStopId} onChange={handleInputChange} required className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500">
                    <option value="">Select Next Stop...</option>
                    {stops.map(s => <option key={s.stopId} value={s.stopId}>{s.stopName} ({s.stopId})</option>)}
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Operational Status</label>
                  <select name="operationalStatus" value={formData.operationalStatus} onChange={handleInputChange} className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500">
                    <option value="ACTIVE">Active</option>
                    <option value="INACTIVE">Inactive</option>
                    <option value="MAINTENANCE">Maintenance</option>
                    <option value="UNDER_REPAIR">Under Repair</option>
                    <option value="OUT_OF_SERVICE">Out of Service</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-1">Occupancy</label>
                  <select name="occupancyStatus" value={formData.occupancyStatus} onChange={handleInputChange} className="w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500">
                    <option value="EMPTY">Empty</option>
                    <option value="LOW">Low</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="CROWDED">Crowded</option>
                    <option value="FULL">Full</option>
                    <option value="OVERLOADED">Overloaded</option>
                  </select>
                </div>
              </div>

              <div className="pt-4 flex justify-end gap-3 sticky bottom-0 bg-white mt-4 border-t border-gray-100 py-4">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg font-medium">Cancel</button>
                <button type="submit" className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium">
                  {editingBus ? 'Update Bus' : 'Save Bus'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default BusManager;
