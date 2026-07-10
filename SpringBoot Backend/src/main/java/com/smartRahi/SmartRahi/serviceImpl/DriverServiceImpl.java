package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.DriverRequest;
import com.smartRahi.SmartRahi.DTO.request.StartTripRequest;
import com.smartRahi.SmartRahi.DTO.response.DriverResponse;
import com.smartRahi.SmartRahi.Entity.*;
import com.smartRahi.SmartRahi.Repository.*;
import com.smartRahi.SmartRahi.Services.DriverService;
import com.smartRahi.SmartRahi.enums.TripStatus;
import com.smartRahi.SmartRahi.mapper.DriverMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {
    private final TripRepository tripRepository;
    private final DriverRepository driverRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository; // ⭐️ Inject Profile Repo
    @Override
    @Transactional
    public void startTrip(String driverUsername, StartTripRequest request) {

        // 1. Logged-in Driver ko dhoondein
        User user = userRepository.findByUsername(driverUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + driverUsername));
        Driver driver = driverRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Driver profile not found for user: " + driverUsername));

        // 2. Trip ko GTFS ID se dhoondein
        Trip trip = tripRepository.findByGtfsTripId(request.getGtfsTripId())
                .orElseThrow(() -> new RuntimeException("Trip not found: " + request.getGtfsTripId()));

        // 3. Bus ko Bus ID se dhoondein
        Bus bus = busRepository.findByBusId(request.getBusId())
                .orElseThrow(() -> new RuntimeException("Bus not found: " + request.getBusId()));

        // 4. Validations (Checks)
        if (trip.getStatus() == TripStatus.ACTIVE) {
            throw new RuntimeException("Trip is already active.");
        }
        if (trip.getStatus() == TripStatus.COMPLETED) {
            throw new RuntimeException("This trip has already been completed.");
        }

        // Check karein ki yeh bus pehle se hi kisi doosri active trip par toh nahi hai
        tripRepository.findByBusAndStatus(bus, TripStatus.ACTIVE).ifPresent(activeTrip -> {
            throw new RuntimeException("Bus " + bus.getBusId() + " is already on an active trip: " + activeTrip.getGtfsTripId());
        });

        // 5. Trip ko update aur link karein
        trip.setStatus(TripStatus.ACTIVE);
        trip.setBus(bus);
        trip.setDriver(driver);
        trip.setActualDepartureTime(LocalDateTime.now()); // Asli nikalne ka time

        tripRepository.save(trip);

        log.info("Driver {} started Trip {} with Bus {}", driverUsername, trip.getGtfsTripId(), bus.getBusId());
    }

    @Override
    @Transactional
    public void endTrip(String driverUsername) {

        // 1. Logged-in Driver ko dhoondein
        User user = userRepository.findByUsername(driverUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + driverUsername));
        Driver driver = driverRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Driver profile not found for user: " + driverUsername));

        // 2. Is driver ki 'ACTIVE' trip dhoondein
        //    (Iske liye repository mein naya method chahiye hoga)
        Trip activeTrip = tripRepository.findByDriverAndStatus(driver, TripStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("No active trip found for driver: " + driverUsername));

        // 3. Trip ko 'COMPLETED' set karein
        activeTrip.setStatus(TripStatus.COMPLETED);
        activeTrip.setActualArrivalTime(LocalDateTime.now()); // Asli pahunchne ka time

        // Optional: Trip poori hone par links hata dein
        activeTrip.setBus(null);
        activeTrip.setDriver(null);

        tripRepository.save(activeTrip);

        log.info("Driver {} ended Trip {}", driverUsername, activeTrip.getGtfsTripId());
    }

    @Override
    @Transactional
    public DriverResponse createDriver(DriverRequest request) {

        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                .orElseThrow(() -> new RuntimeException("User not found. Driver must register first."));

        if (driverRepository.existsByUser(user)) {
            throw new RuntimeException("This user is already an active operational driver.");
        }

        // ⭐️ Also find the profile. We need it for the response.
        DriverProfile profile = (DriverProfile) driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("DriverProfile not found for this user."));

        Bus assignedBus = null;
        if (request.getAssignedBusId() != null && !request.getAssignedBusId().isEmpty()) {
            assignedBus = busRepository.findById(UUID.fromString(request.getAssignedBusId()))
                    .orElseThrow(() -> new RuntimeException("Assigned bus not found"));
        }

        Driver driver = Driver.builder()
                .user(user)
                .assignedBus(assignedBus)
                .build();

        Driver savedDriver = driverRepository.save(driver);

        // ⭐️ Use the new mapper with all 3 entities
        return DriverMapper.toResponse(savedDriver, user, profile);
    }

    @Override
    public DriverResponse getDriverById(UUID driverId) {
        // ⭐️ Fetch all 3 linked entities for a complete response
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver (operational) not found"));

        User user = driver.getUser();
        DriverProfile profile = (DriverProfile) driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("DriverProfile not found for user: " + user.getId()));

        return DriverMapper.toResponse(driver, user, profile);
    }

    @Override
    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll().stream()
                .map(driver -> {
                    // ⭐️ This is slower but more correct for a list
                    User user = driver.getUser();
                    DriverProfile profile = (DriverProfile) driverProfileRepository.findByUser(user).orElse(null);
                    // Pass all data to the mapper
                    return DriverMapper.toResponse(driver, user, profile);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DriverResponse updateDriver(UUID driverId, DriverRequest request) {

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver (operational) not found"));

        Bus newBus = null;
        if (request.getAssignedBusId() != null && !request.getAssignedBusId().isEmpty()) {
            newBus = busRepository.findById(UUID.fromString(request.getAssignedBusId()))
                    .orElseThrow(() -> new RuntimeException("Bus not found"));
        }

        driver.setAssignedBus(newBus);
        Driver updatedDriver = driverRepository.save(driver);

        // ⭐️ Get related entities for the response
        User user = updatedDriver.getUser();
        DriverProfile profile = (DriverProfile) driverProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("DriverProfile not found for user: " + user.getId()));

        return DriverMapper.toResponse(updatedDriver, user, profile);
    }

    @Override
    @Transactional
    public void deleteDriver(UUID driverId) {
        if (!driverRepository.existsById(driverId)) {
            throw new RuntimeException("Driver not found");
        }
        driverRepository.deleteById(driverId);
    }
}