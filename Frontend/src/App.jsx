import './App.css'
import Navbar from './components/navbar.jsx'
import Hero from './components/hero.jsx'
import Footer from './components/footer.jsx'
import ETAModule from './components/ETAModule.jsx'
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import BusResultsPage from './components/BusResultsPage.jsx'
import AboutUs from './components/AboutUs.jsx'
import LoginSignup from './components/loginSignup.jsx'
import ScrollToTop from './components/ScrollToTop.jsx'
import FavouriteTrips from './components/FavouriteTrips.jsx'  
import FloatingToggle from './components/FloatingToggle.jsx'


// --- HomeTag function jismein Hero + ETAModule hai ---
function HomeTag() {
  return (
    <>
      <Hero />
      <FavouriteTrips />
 
    </>
  )
}

function BusTag() {
  return (
    <>
      <ETAModule />
       <FloatingToggle />
    </>
  )
}

function App() {
  return (
    
    <Router>
        <ScrollToTop />
      <Navbar />
      <hr className="border-t border-gray-200 shadow-2xs" />

      <Routes>
        {/* Hero + ETAModule */}
        <Route path="/" element={<HomeTag />} />
        <Route path="/AboutUs" element={<AboutUs />} />
        <Route path="/loginSignup" element={<LoginSignup />} />
       <Route path="/eta" element={<BusTag />} />


        {/* Bus results page */}
        <Route path="/buses" element={<BusResultsPage />} />
      </Routes>

      <Footer />
    </Router>
  
  )
}

export default App
