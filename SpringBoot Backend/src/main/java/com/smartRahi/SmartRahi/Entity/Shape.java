package com.smartRahi.SmartRahi.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shapes")
@Data
@NoArgsConstructor
public class Shape {
    @Id
    private String shapeId; // GTFS natural key

    public Shape(String shapeId) {
        this.shapeId = shapeId;
    }
}