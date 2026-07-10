package com.smartRahi.SmartRahi.DTO.request;



import com.smartRahi.SmartRahi.enums.BusType;
import com.smartRahi.SmartRahi.enums.OccupancyStatus;
import com.smartRahi.SmartRahi.enums.OperationalStatus;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusRequest {

    private String busId;
    private String busNumber;
    private BusType busType;
    private Integer capacity;
    private OperationalStatus operationalStatus;
    private OccupancyStatus occupancyStatus;
    private Double currentLat;
    private Double currentLon;
    private String routeId;
    private String nextStopId;
}
///Used when creating or updating bus details. Example: bus number, capacity, type.