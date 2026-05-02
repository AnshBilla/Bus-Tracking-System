package com.smartRahi.SmartRahi.DTO.request;


// This record is a modern Java way to create a simple, immutable data-only class.
// It ensures the user can *only* send the city name, nothing else.
public record CityRequest(String cityName) {
}