package com.smartRahi.SmartRahi.DTO.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/// Used when adding or updating driver details. Example: name, license number.
public class DriverRequest {
    /**
     * Uss User ki ID jise driver banana hai.
     * Yeh user registration ke time ban chuka hai.
     */
    private String userId; // Ya UUID, jo bhi aapki User ID ka type hai

    /**
     * (Optional) Uss Bus ki ID jo is driver ko assign ki jaa rahi hai.
     * Agar yeh null hai, toh driver ban jaayega par bus assign nahi hogi.
     */
    private String assignedBusId; // Ya UUID
}