package com.smartRahi.SmartRahi.Services;

import com.smartRahi.SmartRahi.DTO.response.NominatimResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeocodingService {

    private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);
    private final RestTemplate restTemplate;

    public GeocodingService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public String getCityFromCoordinates(double lat, double lon) {
        String url = String.format("https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f", lat, lon);
        log.info("Performing reverse geocoding for lat: {}, lon: {}...", lat, lon);

        try {
            // Nominatim requires a custom User-Agent
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "GTFS-Ingest-Script (Java/Spring)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<NominatimResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, NominatimResponse.class
            );

            if (response.getBody() != null && response.getBody().getAddress() != null) {
                NominatimResponse.Address address = response.getBody().getAddress();
                String cityName = address.getCity() != null ? address.getCity() :
                        address.getTown() != null ? address.getTown() :
                                address.getVillage() != null ? address.getVillage() :
                                        address.getCounty();

                if (cityName != null) {
                    log.info("Geocoding successful. Found city: {}", cityName);
                    return cityName;
                }
            }
            log.warn("Geocoding response did not contain a city name.");
            return null;
        } catch (Exception e) {
            log.error("Reverse geocoding failed: {}", e.getMessage());
            return null;
        }
    }
}