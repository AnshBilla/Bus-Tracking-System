package com.smartRahi.SmartRahi.Entity;


import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "city")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {

    @Id
    @GeneratedValue
    @Column(name = "city_id", updatable = false, nullable = false)
    private UUID cityId;

    @Column(name = "city_name", unique = true, nullable = false)
    private String cityName;

    // Relation: One city has many stops
    @OneToMany(mappedBy = "city", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stop> stops;
    public City(String cityName) {
        this.cityName = cityName;
    }
}