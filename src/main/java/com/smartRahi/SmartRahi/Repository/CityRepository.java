package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {
    // By extending JpaRepository, you get all these methods for free:
    // - save()
    // - findById()
    // - findAll()
    // - deleteById()
    // - ...and many more!

    // You can add custom queries here if you need them, for example:
    // Optional<City> findByCityName(String cityName);
}