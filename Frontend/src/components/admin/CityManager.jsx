import React, { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../config/api';
import { Edit2, Trash2, Plus, X } from 'lucide-react';

const CityManager = () => {
  const [cities, setCities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingCity, setEditingCity] = useState(null);
  const [cityName, setCityName] = useState('');
  const [error, setError] = useState('');

  const loadCities = async () => {
    setLoading(true);
    try {
      const res = await fetchWithAuth('/cities');
      if (res.ok) {
        const data = await res.json();
        setCities(data);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCities();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const url = editingCity ? `/cities/${editingCity.id}` : '/cities';
      const method = editingCity ? 'PUT' : 'POST';
      
      const res = await fetchWithAuth(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ cityName })
      });

      if (res.ok) {
        setIsModalOpen(false);
        setCityName('');
        setEditingCity(null);
        loadCities();
      } else {
        const errData = await res.json().catch(() => ({}));
        setError(errData.message || 'Failed to save city');
      }
    } catch (err) {
      setError('Network error');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this city?')) return;
    try {
      const res = await fetchWithAuth(`/cities/${id}`, { method: 'DELETE' });
      if (res.ok) {
        loadCities();
      }
    } catch (err) {
      console.error(err);
    }
  };

  const openEdit = (city) => {
    setEditingCity(city);
    setCityName(city.cityName);
    setIsModalOpen(true);
  };

  const openAdd = () => {
    setEditingCity(null);
    setCityName('');
    setIsModalOpen(true);
  };

  return (
    <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      {/* Header */}
      <div className="px-6 py-4 border-b border-gray-200 flex justify-between items-center bg-gray-50">
        <div>
          <h2 className="text-xl font-bold text-gray-800">Manage Cities</h2>
          <p className="text-sm text-gray-500 mt-1">Add, update, or remove operational cities.</p>
        </div>
        <button 
          onClick={openAdd}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-lg font-semibold flex items-center gap-2 transition-colors"
        >
          <Plus size={18} /> Add City
        </button>
      </div>

      {/* Table */}
      <div className="overflow-x-auto">
        <table className="w-full text-left border-collapse">
          <thead>
            <tr className="bg-gray-100 text-gray-600 text-sm uppercase tracking-wider">
              <th className="px-6 py-3 font-semibold">ID (UUID)</th>
              <th className="px-6 py-3 font-semibold">City Name</th>
              <th className="px-6 py-3 font-semibold text-right">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200 text-sm">
            {loading ? (
              <tr><td colSpan="3" className="px-6 py-8 text-center text-gray-500">Loading cities...</td></tr>
            ) : cities.length === 0 ? (
              <tr><td colSpan="3" className="px-6 py-8 text-center text-gray-500">No cities found.</td></tr>
            ) : (
              cities.map((city) => (
                <tr key={city.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 font-mono text-xs text-gray-500">{city.id}</td>
                  <td className="px-6 py-4 font-medium text-gray-900">{city.cityName}</td>
                  <td className="px-6 py-4 flex justify-end gap-3">
                    <button onClick={() => openEdit(city)} className="p-2 text-blue-600 hover:bg-blue-50 rounded-md transition-colors" title="Edit">
                      <Edit2 size={16} />
                    </button>
                    <button onClick={() => handleDelete(city.id)} className="p-2 text-red-600 hover:bg-red-50 rounded-md transition-colors" title="Delete">
                      <Trash2 size={16} />
                    </button>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Modal Form */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
            <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
              <h3 className="text-lg font-bold">{editingCity ? 'Edit City' : 'Add New City'}</h3>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={20} />
              </button>
            </div>
            
            <form onSubmit={handleSubmit} className="p-6 space-y-4">
              {error && <div className="p-3 bg-red-50 text-red-700 text-sm rounded-md">{error}</div>}
              
              <div>
                <label className="block text-sm font-semibold text-gray-700 mb-1">City Name *</label>
                <input 
                  type="text" 
                  value={cityName} 
                  onChange={(e) => setCityName(e.target.value)} 
                  required
                  placeholder="e.g. Bangalore"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none"
                />
              </div>

              <div className="pt-4 flex justify-end gap-3">
                <button type="button" onClick={() => setIsModalOpen(false)} className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded-lg font-medium transition-colors">
                  Cancel
                </button>
                <button type="submit" className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors shadow-md">
                  {editingCity ? 'Update City' : 'Save City'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default CityManager;
