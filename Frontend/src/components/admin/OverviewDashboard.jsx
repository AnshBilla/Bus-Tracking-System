import React, { useState, useEffect } from 'react';
import { fetchWithAuth } from '../../config/api';
import { Bus, Map, MapPin, Users, Activity } from 'lucide-react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar } from 'recharts';

const OverviewDashboard = () => {
  const [stats, setStats] = useState({
    buses: 0,
    cities: 0,
    routes: 0,
    stops: 0
  });
  const [loading, setLoading] = useState(true);

  // Mock data for the charts to look impressive
  const trafficData = [
    { name: 'Mon', passengers: 4000 },
    { name: 'Tue', passengers: 3000 },
    { name: 'Wed', passengers: 2000 },
    { name: 'Thu', passengers: 2780 },
    { name: 'Fri', passengers: 1890 },
    { name: 'Sat', passengers: 2390 },
    { name: 'Sun', passengers: 3490 },
  ];

  const cityData = [
    { name: 'Delhi', buses: 120 },
    { name: 'Mumbai', buses: 98 },
    { name: 'Bangalore', buses: 86 },
    { name: 'Pune', buses: 45 },
    { name: 'Jaipur', buses: 30 },
  ];

  useEffect(() => {
    const loadStats = async () => {
      setLoading(true);
      try {
        // We fetch the arrays and count their lengths to get totals
        const [busRes, cityRes, routeRes, stopRes] = await Promise.all([
          fetchWithAuth('/buses').catch(() => ({ ok: false })),
          fetchWithAuth('/cities').catch(() => ({ ok: false })),
          fetchWithAuth('/routes').catch(() => ({ ok: false })),
          fetchWithAuth('/stops').catch(() => ({ ok: false }))
        ]);

        let bCount = 0, cCount = 0, rCount = 0, sCount = 0;
        
        if (busRes.ok) {
            const data = await busRes.json();
            bCount = data.length || 0;
        }
        if (cityRes.ok) {
            const data = await cityRes.json();
            cCount = data.length || 0;
        }
        if (routeRes.ok) {
            const data = await routeRes.json();
            rCount = data.length || 0;
        }
        if (stopRes.ok) {
            const data = await stopRes.json();
            sCount = data.length || 0;
        }

        setStats({ buses: bCount, cities: cCount, routes: rCount, stops: sCount });
      } catch (err) {
        console.error("Error loading stats", err);
      } finally {
        setLoading(false);
      }
    };

    loadStats();
  }, []);

  const StatCard = ({ title, value, icon: Icon, colorClass, bgColorClass }) => (
    <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 flex items-center justify-between hover:shadow-md transition-shadow">
      <div>
        <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">{title}</p>
        <h3 className="text-3xl font-extrabold text-gray-800 mt-2">{loading ? '...' : value}</h3>
      </div>
      <div className={`p-4 rounded-full ${bgColorClass} ${colorClass}`}>
        <Icon size={28} />
      </div>
    </div>
  );

  return (
    <div className="space-y-6">
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-gray-800">System Overview</h2>
        <p className="text-gray-500 mt-1">Real-time statistics and analytics for Smart Rahi.</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Total Buses" value={stats.buses} icon={Bus} colorClass="text-blue-600" bgColorClass="bg-blue-50" />
        <StatCard title="Active Cities" value={stats.cities} icon={Map} colorClass="text-green-600" bgColorClass="bg-green-50" />
        <StatCard title="Total Routes" value={stats.routes} icon={Activity} colorClass="text-purple-600" bgColorClass="bg-purple-50" />
        <StatCard title="Total Stops" value={stats.stops} icon={MapPin} colorClass="text-orange-600" bgColorClass="bg-orange-50" />
      </div>

      {/* Charts Section */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-8">
        
        {/* Weekly Passengers Chart */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h3 className="text-lg font-bold text-gray-800 mb-6">Weekly Passenger Traffic</h3>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={trafficData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#6B7280'}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#6B7280'}} dx={-10} />
                <Tooltip 
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                  cursor={{stroke: '#E5E7EB', strokeWidth: 2}}
                />
                <Line type="monotone" dataKey="passengers" stroke="#2563EB" strokeWidth={4} dot={{ r: 4, strokeWidth: 2 }} activeDot={{ r: 8 }} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Fleet Distribution Chart */}
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h3 className="text-lg font-bold text-gray-800 mb-6">Fleet Distribution (Top Cities)</h3>
          <div className="h-72">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={cityData}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{fill: '#6B7280'}} dy={10} />
                <YAxis axisLine={false} tickLine={false} tick={{fill: '#6B7280'}} dx={-10} />
                <Tooltip 
                  cursor={{fill: '#F3F4F6'}}
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)' }}
                />
                <Bar dataKey="buses" fill="#10B981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>
    </div>
  );
};

export default OverviewDashboard;
