// Footer.jsx
import React from 'react';
import {
  Mail,
  Phone,
  MapPin,
  Globe,
  Linkedin,
  Instagram,
  Facebook,
  Youtube,
  ChevronDown
} from 'lucide-react';

const NavLink = ({ children, to, onClick, className }) => (
  <Link
    to={to}
    onClick={onClick}
    className={`block text-base font-medium text-gray-800 hover:text-black hover:font-bold hover:shadow-2xl transition-all duration-200 px-3 py-2 rounded-md ${className}`}
  >
    {children}
  </Link>
);

// Footer Link Column Component
// const FooterLinkColumn = ({ title, links }) => (
//   <div>
//     <h4 className="text-sm font-bold tracking-wider text-gray-900 uppercase mb-5">
//       {title}
//     </h4>
//     <ul className="space-y-3">
//       {links.map((link, index) => (
//         <li key={index}>
//           <a
//             href="#"
//             className="text-gray-600 hover:text-black font-semibold text-sm transition-colors"
//             onClick={(e) => e.preventDefault()}
//           >
//             {link}
//           </a>
//         </li>
//       ))}
//     </ul>
//   </div>
// );

// Contact Detail Component
// const ContactDetail = ({ icon: Icon, text, link, isEmail = false }) => (
//   <div className="flex items-start mb-4">
//     {Icon && <Icon className="w-5 h-5 text-blue-600 mr-3 mt-1 flex-shrink-0" />}
//     <a
//       href={isEmail ? `mailto:${link}` : link}
//       className="text-gray-600 text-sm hover:text-black font-semibold transition-colors"
//       onClick={isEmail ? null : (e) => e.preventDefault()}
//     >
//       {text}
//     </a>
//   </div>
// );

// Social Icon Component with brand colors
// const SocialIcon = ({ icon: Icon, platform }) => {
//   const colors = {
//     linkedin: '#0077B5',
//     instagram: '#E4405F',
//     facebook: '#1877F2',
//     youtube: '#FF0000'
//   };

//   return (
//     <a
//       href="#"
//       target="_blank"
//       rel="noopener noreferrer"
//       className="p-2 rounded-full hover:bg-gray-100 transition-transform hover:scale-110"
//       onClick={(e) => e.preventDefault()}
//     >
//       {Icon && <Icon className="w-5 h-5" style={{ color: colors[platform] }} />}
//     </a>
//   );
// };

// Main Footer Component
const Footer = () => {
  // const companyLinks = ['About Us', 'Services', 'Contact Us'];
  // const navigationLinks = ['Main Benefits', 'Our Services', 'Why Smartराही'];

  return (
    <footer id="footer" className="bg-white border-t border-gray-100 mt-20 font-inter ">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 ">

        {/* Top Section: Logo and Language */}
        {/* <div className="flex flex-col md:flex-row justify-between items-start md:items-center pb-12 border-b border-gray-100">
          <div className="mb-8 md:mb-0">
            <div className="flex items-center text-2xl font-bold mb-6">
              Smartराही
            </div>
            <p className="text-gray-700 text-base md:text-lg">
              One-stop solution for live bus tracking and route management.
            </p>
          </div>

          {/* <div className="relative">
            <button className="flex items-center justify-between w-40 px-4 py-2 text-gray-700 border border-gray-300 rounded-lg shadow-sm hover:bg-gray-50 transition-colors">
              <Globe className="w-5 h-5 mr-2" />
              English
              <ChevronDown className="w-4 h-4 ml-2" />
            </button>
          </div> */}
        {/* </div> */} 

        {/* Middle Section: Link Columns & Contact */}
        {/* <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-10 pt-12 pb-16">
          <FooterLinkColumn title="Privacy" links={companyLinks} />
          <FooterLinkColumn title="Terms and Condition" links={navigationLinks} /> */}

          {/* Contact Info */}
          {/* <div className="col-span-2 lg:col-span-1">
            <h4 className="text-sm font-bold tracking-wider text-gray-900 uppercase mb-5">Contact</h4>
            <ContactDetail icon={Mail} text="info@smartrahi.com" link="info@smartrahi.com" isEmail />
            <ContactDetail icon={Phone} text="+91 9988XXXXX" link="tel:+919988XXXXX" />
            <ContactDetail icon={MapPin} text="Uttar Pradesh, India" link="#" />
          </div> */}

          {/* Social Icons */}
          {/* <div className="flex flex-col items-start md:items-end lg:items-start space-y-4 md:col-start-4 lg:col-start-4 lg:col-span-2 md:mt-0 mt-8">
            <div className="flex space-x-4">
              <SocialIcon icon={Linkedin} platform="linkedin" />
              <SocialIcon icon={Instagram} platform="instagram" />
              <SocialIcon icon={Facebook} platform="facebook" />
              <SocialIcon icon={Youtube} platform="youtube" />
            </div>
          </div> */}
        {/* </div> */}

        {/* Bottom Section: Copyright & Legal */}
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center pt-8 border-t border-gray-100 text-xs text-gray-500 space-y-4 md:space-y-0">
          <p>© 2025 Smartराही. All Rights Reserved</p>
          <div className="flex space-x-6 ml-auto">
            <a href="#" className="hover:text-black font-semibold transition-colors" onClick={(e) => e.preventDefault()}>Terms and Conditions</a>
            <a href="#" className="hover:text-black font-semibold transition-colors" onClick={(e) => e.preventDefault()}>Privacy</a>
            <a href="#" className="hover:text-black font-semibold transition-colors" onClick={(e) => e.preventDefault()}>Cookies</a>
          </div>
        </div>

      </div>
    </footer>
  );
};

export default Footer;