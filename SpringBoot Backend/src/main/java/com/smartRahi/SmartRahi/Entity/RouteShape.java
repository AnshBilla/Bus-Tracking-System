package com.smartRahi.SmartRahi.Entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "route_shapes",
        indexes = {
                @Index(name = "idx_routeshape_route_sequence", columnList = "route_id, shape_pt_sequence")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteShape {

    // Primary Key (auto-increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Integer id;

    // Relationship to Route
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // Unique identifier for shape (GTFS standard)
    @Column(name = "shape_id", nullable = false)
    private String shapeId;

    // Shape point latitude and longitude
    @Column(name = "shape_pt_lat", nullable = false)
    private Double shapePtLat;

    @Column(name = "shape_pt_lon", nullable = false)
    private Double shapePtLon;

    // Sequence of shape point along the route
    @Column(name = "shape_pt_sequence", nullable = false)
    private Integer shapePtSequence;

    // Optional distance traveled along the shape
    @Column(name = "shape_dist_traveled")
    private Double shapeDistTraveled;
}
