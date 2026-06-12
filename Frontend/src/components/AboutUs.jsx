import React from 'react';
import { Clock, Route, Users, Bus } from 'lucide-react'; // Using lucide-react for icons
import { HashLink } from 'react-router-hash-link';

// Component for a single feature card
// eslint-disable-next-line no-unused-vars
const FeatureCard = ({ icon: Icon, title, description, color }) => (
  <div className={`p-6 bg-white border-t-4 border-${color}-500 rounded-2xl shadow-md hover:shadow-2xl transition-transform duration-300 ease-in-out transform hover:-translate-y-2`}>
    <div className={`flex items-center justify-center w-12 h-12 rounded-full bg-gradient-to-br from-${color}-200 to-${color}-400 mb-4`}>
      <Icon className={`w-6 h-6 text-${color}-700`} />
    </div>
    <h3 className="text-xl font-bold text-gray-800 mb-2">{title}</h3>
    <p className="text-gray-600">{description}</p>
  </div>
);

const AboutUs = () => {
  const features = [
    {
      icon: Clock,
      title: "Real-Time Accuracy",
      description: "Stop guessing. Our system uses advanced GPS to provide estimated arrival times you can rely on, updating every 30 seconds.",
      color: "blue",
    },
    {
      icon: Route,
      title: "Optimized Routing",
      description: "We analyze live traffic conditions to suggest the fastest routes, helping the entire public transport network run efficiently.",
      color: "green",
    },
    {
      icon: Users,
      title: "Community Focused",
      description: "Built for every passenger, our interface is simple, accessible, and provides timely service updates and alerts.",
      color: "orange",
    },
  ];

  return (
    <div className="min-h-screen bg-gradient-to-b from-gray-50 via-white to-gray-100 font-inter py-16 sm:py-24">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

        {/* Header */}
        <div className="text-center mb-16 pt-10">
          <h2 className="mt-4 text-4xl sm:text-5xl lg:text-6xl font-extrabold text-gray-900 tracking-tight">
            Stop Waiting, Start Moving.
          </h2>
          <p className="mt-6 max-w-3xl mx-auto text-lg sm:text-xl text-gray-600">
            Public transit should be predictable. No more standing in the rain—just reliable information at your fingertips.
          </p>
        </div>

        {/* Mission Section */}
        <div className="md:flex items-center bg-white rounded-3xl shadow-2xl overflow-hidden mb-16 p-6 md:p-12 hover:scale-102 transition-transform duration-500">
          <div className="md:w-1/2 p-4">
            <h3 className="text-3xl font-bold text-gray-800 mb-4">Our Commitment to Commuters</h3>
            <p className="text-lg text-gray-600 mb-6">
              Every minute counts. Our tracking algorithm ensures the most accurate data available, helping you plan your day with confidence. We partner directly with transit authorities for reliable real-time updates.
            </p>
            <div className="flex items-center text-blue-600 font-semibold">
              <Bus className="w-5 h-5 mr-2 animate-bounce" />
              Serving millions of commutes daily.
            </div>
          </div>
          <div className="md:w-1/2 mt-8 md:mt-0 md:pl-8 flex justify-center items-center">
            <div className="w-full h-64 md:h-96 rounded-2xl overflow-hidden shadow-lg transform hover:scale-105 transition-transform duration-500">
              <img 
                src="https://placehold.co/800x600/1e40af/ffffff?text=Reliable+Bus+Tracking" 
                alt="Map showing real-time bus tracking route" 
                className="w-full h-full object-cover"
                onError={(e) => { e.target.onerror = null; e.target.src = "https://placehold.co/800x600/1e40af/ffffff?text=Map+View"; }}
              />
            </div>
          </div>
        </div>

        {/* Feature Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-10">
          {features.map((feature, index) => (
            <FeatureCard key={index} {...feature} />
          ))}
        </div>
      </div>

      {/* CTA Section */}
      <div className="mt-20 text-center bg-gradient-to-r from-blue-100 via-indigo-100 to-purple-100 p-12 rounded-3xl shadow-inner max-w-4xl mx-auto transform hover:scale-102 transition-transform duration-500">
        <h4 className="text-2xl font-bold text-gray-800">Ready for a smoother commute?</h4>
        <p className="mt-3 text-lg text-gray-600">Check the live map now and experience the difference.</p>
        
        {/* 🚍 Fixed Button: Jumps directly to Hero section */}
        <HashLink
          smooth
          to="/#hero"
          className="inline-block mt-6 px-10 py-3 bg-gradient-to-r from-blue-500 to-indigo-600 text-white text-lg font-semibold rounded-full shadow-lg hover:from-indigo-600 hover:to-blue-500 transition-all duration-300 transform hover:scale-105"
        >
          Track Your Bus Live
        </HashLink>
      </div>
    </div>
  );
};

export default AboutUs;
