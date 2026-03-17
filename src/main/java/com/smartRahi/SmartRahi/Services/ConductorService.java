package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.request.ConductorRequest;
import com.smartRahi.SmartRahi.DTO.response.ConductorResponse;

import java.util.List;
import java.util.UUID;

public interface ConductorService {

    ConductorResponse createConductor(ConductorRequest request);

    ConductorResponse getConductorById(UUID conductorId);

    List<ConductorResponse> getAllConductors();

    ConductorResponse updateConductor(UUID conductorId, ConductorRequest request);

    void deleteConductor(UUID conductorId);

    // ⭐️ REMOVE this line: boolean existsByUser(User user);
}