package com.smartRahi.SmartRahi.DTO.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Yeh DTO ek pehle se registered User (jiska role OPERATOR hai) ko
 * ek "Operational Conductor" banane ke liye use hota hai.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConductorRequest {

    /**
     * Uss User ki ID jise conductor banana hai.
     */
    private String userId; // Ya UUID

    /**
     * (Optional) Uss Bus ki ID jo is conductor ko assign ki jaa rahi hai.
     */
    private String assignedBusId; // Ya UUID
}