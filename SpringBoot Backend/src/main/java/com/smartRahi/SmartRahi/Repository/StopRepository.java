package com.smartRahi.SmartRahi.Repository;

import com.smartRahi.SmartRahi.Entity.Stop;
import com.smartRahi.SmartRahi.Repository.Projections.NearbyStopProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StopRepository extends JpaRepository<Stop, UUID> {

    // Admin/CRUD features ke liye
    Optional<Stop> findByStopId(String stopId);
    boolean existsByStopId(String stopId);
    void deleteByStopId(String stopId);

    // Geocoding ke liye (jaisa GtfsImportService mein use ho sakta hai)
    Optional<Stop> findFirstBy();

    // PostGIS query (Nearby Stops ke liye)
    @Query(value = """
            SELECT s.stop_id AS "stopId",
                   s.stop_name AS "stopName",
                   s.stop_lat AS "stopLat",
                   s.stop_lon AS "stopLon",
                   ST_Distance(
                       ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                       ST_SetSRID(ST_MakePoint(s.stop_lon, s.stop_lat), 4326)::geography
                   ) AS distance
            FROM stops s
            WHERE ST_DWithin(
                       ST_SetSRID(ST_MakePoint(s.stop_lon, s.stop_lat), 4326)::geography,
                       ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                       :radius
                   )
            ORDER BY distance ASC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<NearbyStopProjection> findNearbyStops(
            @Param("lat") double lat,
            @Param("lon") double lon,
            @Param("radius") double radius,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    // Stop Search ke liye (behtar wala search)
    @Query("SELECT s FROM Stop s WHERE lower(s.stopName) LIKE lower(concat('%', :query, '%')) " +
            "ORDER BY length(s.stopName) ASC")
    List<Stop> findByNameContainingIgnoreCaseOrderByLength(
            @Param("query") String query
    );
}