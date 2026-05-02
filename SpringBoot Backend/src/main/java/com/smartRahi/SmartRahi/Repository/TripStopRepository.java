package com.smartRahi.SmartRahi.Repository; // Aapka package

import com.smartRahi.SmartRahi.Entity.TripStop; // Aapki entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // Ise add karna acchi practice hai
public interface TripStopRepository extends JpaRepository<TripStop, Long> { // Aapka <TripStop, Long> bilkul sahi hai

    /**
     * Yeh naya method add karein (Phase 1: Journey Planner ke liye)
     * Yeh aise trips dhoondhta hai jo 'from' aur 'to' dono stops se guzarte hain,
     * aur 'from' stop hamesha 'to' stop se pehle aata hai.
     *
     * Note: Yahaan 'stopId' ka matlab GTFS wala string ID hai (jaise "S123"),
     * database wala numeric 'id' nahi.
     */
    @Query("SELECT ts1 FROM TripStop ts1 " +
            "JOIN TripStop ts2 ON ts1.trip.tripId = ts2.trip.tripId " +
            "WHERE ts1.stop.stopId = :fromStopId " +
            "AND ts2.stop.stopId = :toStopId " +
            "AND ts1.stopSequence < ts2.stopSequence")
    List<TripStop> findDirectTrips(
            @Param("fromStopId") String fromStopId,
            @Param("toStopId") String toStopId
    );

    Optional<TripStop> findByTrip_GtfsTripIdAndStop_StopId(String gtfsTripId, String stopId);

    /**
     * Kisi specific trip ke liye, do sequence numbers ke beech ke saare stops laata hai.
     */
    List<TripStop> findByTrip_GtfsTripIdAndStopSequenceBetweenOrderByStopSequenceAsc(
            String gtfsTripId,
            int fromSequence,
            int toSequence
    );

}