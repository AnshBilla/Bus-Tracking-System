package com.smartRahi.SmartRahi.DTO.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResponse {
    @JsonProperty("address")
    private Address address;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Address {
        @JsonProperty("city")
        private String city;
        @JsonProperty("town")
        private String town;
        @JsonProperty("village")
        private String village;
        @JsonProperty("county")
        private String county;
    }
}