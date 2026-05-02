package com.smartRahi.SmartRahi.serviceImpl;

import com.smartRahi.SmartRahi.DTO.request.ConductorRequest;
import com.smartRahi.SmartRahi.DTO.response.ConductorResponse;
import com.smartRahi.SmartRahi.Entity.Bus;
import com.smartRahi.SmartRahi.Entity.Conductor;
import com.smartRahi.SmartRahi.Entity.ConductorProfile; // ⭐️ Import Profile
import com.smartRahi.SmartRahi.Entity.User;
import com.smartRahi.SmartRahi.Repository.BusRepository;
import com.smartRahi.SmartRahi.Repository.ConductorProfileRepository; // ⭐️ Import Profile Repo
import com.smartRahi.SmartRahi.Repository.ConductorRepository;
import com.smartRahi.SmartRahi.Repository.UserRepository;
import com.smartRahi.SmartRahi.Services.ConductorService;
import com.smartRahi.SmartRahi.mapper.ConductorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
// ⭐️ Import ResourceNotFoundException if you created it
// import com.smartRahi.SmartRahi.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConductorServiceImpl implements ConductorService {

    private final ConductorRepository conductorRepository; // Operational repo
    private final ConductorProfileRepository conductorProfileRepository; // ⭐️ Credential repo (Needs to be injected)
    private final BusRepository busRepository;
    private final UserRepository userRepository;

    @Override // ⭐️ Add @Override
    @Transactional
    public ConductorResponse createConductor(ConductorRequest request) {

        User user = userRepository.findById(UUID.fromString(request.getUserId()))
                // ⭐️ Use specific exception if available
                .orElseThrow(() -> new RuntimeException("User not found. Conductor must register first."));
        // .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        if (conductorRepository.existsByUser(user)) {
            throw new RuntimeException("This user is already an active operational conductor.");
        }

        // ⭐️ Also find the profile. We need it for the response.
        ConductorProfile profile = conductorProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("ConductorProfile not found for this user."));
        // .orElseThrow(() -> new ResourceNotFoundException("ConductorProfile not found for user: " + user.getId()));

        Bus assignedBus = null;
        if (request.getAssignedBusId() != null && !request.getAssignedBusId().isEmpty()) {
            assignedBus = busRepository.findById(UUID.fromString(request.getAssignedBusId()))
                    .orElseThrow(() -> new RuntimeException("Assigned bus not found"));
            // .orElseThrow(() -> new ResourceNotFoundException("Bus not found with ID: " + request.getAssignedBusId()));
        }

        Conductor conductor = Conductor.builder()
                .user(user)
                .assignedBus(assignedBus)
                .build();

        Conductor savedConductor = conductorRepository.save(conductor);

        // ⭐️ FIX: Use the new 3-argument mapper
        return ConductorMapper.toResponse(savedConductor, user, profile);
    }

    @Override // ⭐️ Add @Override
    public ConductorResponse getConductorById(UUID conductorId) {
        // ⭐️ FIX: Fetch all 3 linked entities for a complete response
        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor (operational) not found"));
        // .orElseThrow(() -> new ResourceNotFoundException("Conductor not found with ID: " + conductorId));

        User user = conductor.getUser();
        ConductorProfile profile = conductorProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("ConductorProfile not found for user: " + user.getId()));
        // .orElseThrow(() -> new ResourceNotFoundException("ConductorProfile not found for user: " + user.getId()));

        // ⭐️ FIX: Call the 3-argument mapper
        return ConductorMapper.toResponse(conductor, user, profile);
    }

    @Override // ⭐️ Add @Override
    public List<ConductorResponse> getAllConductors() {
        // ⭐️ FIX: Use lambda to fetch related entities for the mapper
        return conductorRepository.findAll().stream()
                .map(conductor -> {
                    User user = conductor.getUser();
                    // Find profile, handle potential missing profile (data integrity issue)
                    ConductorProfile profile = conductorProfileRepository.findByUser(user).orElse(null);
                    if (profile == null) {
                        // Log a warning or handle appropriately
                        System.err.println("Warning: ConductorProfile missing for user: " + user.getId());
                        return null; // Skip this conductor in the response list
                    }
                    // Call the 3-argument mapper
                    return ConductorMapper.toResponse(conductor, user, profile);
                })
                .filter(response -> response != null) // Filter out any nulls from data issues
                .collect(Collectors.toList());
    }

    @Override // ⭐️ Add @Override
    @Transactional
    public ConductorResponse updateConductor(UUID conductorId, ConductorRequest request) {

        Conductor conductor = conductorRepository.findById(conductorId)
                .orElseThrow(() -> new RuntimeException("Conductor (operational) not found"));
        // .orElseThrow(() -> new ResourceNotFoundException("Conductor not found with ID: " + conductorId));

        Bus newBus = null;
        // ⭐️ FIX: Typo getAsssignedBusId -> getAssignedBusId
        if (request.getAssignedBusId() != null && !request.getAssignedBusId().isEmpty()) {
            newBus = busRepository.findById(UUID.fromString(request.getAssignedBusId()))
                    .orElseThrow(() -> new RuntimeException("Bus not found"));
            // .orElseThrow(() -> new ResourceNotFoundException("Bus not found with ID: " + request.getAssignedBusId()));
        }

        conductor.setAssignedBus(newBus);
        Conductor updatedConductor = conductorRepository.save(conductor);

        // ⭐️ FIX: Get related entities for the response
        User user = updatedConductor.getUser();
        ConductorProfile profile = conductorProfileRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("ConductorProfile not found for user: " + user.getId()));
        // .orElseThrow(() -> new ResourceNotFoundException("ConductorProfile not found for user: " + user.getId()));

        // ⭐️ FIX: Call the 3-argument mapper
        return ConductorMapper.toResponse(updatedConductor, user, profile);
    }

    @Override // ⭐️ Add @Override
    @Transactional
    public void deleteConductor(UUID conductorId) {
        if (!conductorRepository.existsById(conductorId)) {
            throw new RuntimeException("Conductor not found");
            // throw new ResourceNotFoundException("Conductor not found with ID: " + conductorId);
        }
        conductorRepository.deleteById(conductorId);
    }
}