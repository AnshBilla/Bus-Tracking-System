package com.smartRahi.SmartRahi.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopDetailDTO {

    private String stopName;
    private int stopSequence;
    private String arrivalTime;
    private String departureTime;
}