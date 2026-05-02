package com.smartRahi.SmartRahi.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shape_points", indexes = {
        @Index(name = "idx_shape_id", columnList = "shapeId")
})
@Data
@NoArgsConstructor
public class ShapePoint {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String shapeId; // Foreign Key to Shape

    @Column(nullable = false)
    private double shapePtLat;

    @Column(nullable = false)
    private double shapePtLon;

    @Column(nullable = false)
    private int shapePtSequence;

    private Double shapeDistTraveled;
}