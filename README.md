# 🚌 SmartRahi - Bus Tracking System

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

SmartRahi is a comprehensive, real-time Bus Tracking and Journey Planning system designed to make public transportation smarter, more accessible, and highly efficient. It integrates real-time GPS tracking, GTFS data, and crowd-sourced passenger updates to deliver accurate ETA and route information.

## ✨ Key Features

- **📍 Real-Time Tracking**: Track buses in real-time on an interactive map.
- **🗺️ Journey Planner**: Find the best direct and connecting routes between stops.
- **⏱️ ETA Module**: Accurate Estimated Time of Arrival updates using live traffic and bus speed data.
- **🤝 Crowd-Sourced Locations**: Passengers can actively contribute to live location tracking for better accuracy.
- **🔐 Secure Authentication**: Role-based access control (Admin, Passenger, Driver, Conductor) using Spring Security & JWT.
- **⭐ Favourite Trips**: Save your daily routes for quick one-tap access.
- **🎛️ Admin Dashboard**: Manage routes, buses, and personnel seamlessly.

## 🛠️ Tech Stack

### Frontend
- **Framework**: React.js (Vite)
- **Styling**: Tailwind CSS
- **Routing**: React Router DOM
- **Authentication**: JWT (Local Storage)

### Backend
- **Framework**: Java Spring Boot
- **Database**: PostgreSQL (JPA/Hibernate)
- **Security**: Spring Security & JWT
- **Services Integration**: GTFS Realtime Services, Geocoding API (Nominatim)

## 📂 Project Structure

```text
Bus-Tracking-System/
├── Frontend/                 # React UI, Components, Pages
└── SpringBoot Backend/       # Spring Boot REST API, Entities, Services
```

## 🚀 Getting Started

### Prerequisites
- Node.js (v16 or higher)
- Java (JDK 17 or higher)
- Maven
- PostgreSQL

### 1. Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd "SpringBoot Backend"
   ```
2. Configure your PostgreSQL credentials in `src/main/resources/application.properties`.
3. Run the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```

### 2. Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd Frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```

## 🤝 Contributing
Contributions are welcome! Please follow these steps:
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License
This project is licensed under the MIT License.
