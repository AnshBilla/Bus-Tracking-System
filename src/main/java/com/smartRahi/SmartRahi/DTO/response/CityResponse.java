package com.smartRahi.SmartRahi.DTO.response;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CityResponse {

    private String cityId;
    private String cityName;

    // This is the key: It uses StopResponse, which does NOT link back to City.
    private List<StopResponse> stops;
}