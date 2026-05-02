// New File: com/smartRahi/SmartRahi/DTO/response/LiveBusArrivalDTO.java

package com.smartRahi.SmartRahi.DTO.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LiveBusArrivalDTO {

    // Koun si route ki bus hai (e.g., "101")
    private String routeName;

    // Bus kahaan jaa rahi hai (e.g., "Main Street Station")
    private String headsign;

    // Bus ki live location
    private Double busLat;
    private Double busLon;

    // Kitni der mein pahunchegi (minutes mein)
    private Integer etaMinutes;
}