package com.smartRahi.SmartRahi.serviceImpl;

// Aapke DTOs
import com.smartRahi.SmartRahi.DTO.response.JourneyLeg;
import com.smartRahi.SmartRahi.DTO.response.JourneyOption;
import com.smartRahi.SmartRahi.DTO.response.JourneyPlanResponse;
import com.smartRahi.SmartRahi.DTO.response.StopDetailDTO;

// Aapki Entity aur Repository
import com.smartRahi.SmartRahi.Entity.TripStop;
import com.smartRahi.SmartRahi.Repository.TripStopRepository;
import com.smartRahi.SmartRahi.Services.JourneyPlannerService;

// Baaki imports
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JourneyPlannerServiceImpl implements JourneyPlannerService {

    // Hum naya (aur sahi) 'TripStopRepository' istemaal kar rahe hain
    private final TripStopRepository tripStopRepository;

    // Time ko format karne ke liye helper
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * PHASE 1: Journey Planner
     */
    @Override
    @Transactional(readOnly = true)
    public JourneyPlanResponse planJourney(String fromStopName, String toStopName) {

        log.info("Planning journey from {} to {}", fromStopName, toStopName);

        // 1. Sidhe TripStopRepository se direct trips dhoondhein
        List<TripStop> startingTripStops = tripStopRepository.findDirectTripsByName(fromStopName, toStopName);

        if (startingTripStops.isEmpty()) {
            log.info("No direct trips found between {} and {}", fromStopName, toStopName);
            return JourneyPlanResponse.builder().options(Collections.emptyList()).build();
        }

        List<JourneyOption> journeyOptions = new ArrayList<>();

        // 2. Har mili hui trip ke liye, JourneyOption banayein
        for (TripStop fromTripStop : startingTripStops) {

            // 3. Ussi trip ka 'toStop' (destination) data nikaalein
            Optional<TripStop> toTripStopOpt = fromTripStop.getTrip().getTripStops().stream()
                    .filter(ts -> ts.getStop().getStopName().equals(toStopName))
                    .findFirst();

            if (toTripStopOpt.isPresent()) {
                TripStop toTripStop = toTripStopOpt.get();

                // 4. Safar ka samay (duration) calculate karein
                long durationMinutes = Duration.between(
                        fromTripStop.getExpectedDepartureTime(),
                        toTripStop.getExpectedArrivalTime()
                ).toMinutes();

                // 5. JourneyLeg (safar ka hissa) banayein
                JourneyLeg leg = JourneyLeg.builder()
                        .gtfsTripId(fromTripStop.getTrip().getGtfsTripId())
                        .routeId(fromTripStop.getTrip().getRoute().getRouteId())
                        .routeName(fromTripStop.getTrip().getRoute().getRouteName())
                        .fromStopId(fromTripStop.getStop().getStopId())
                        .fromStopName(fromTripStop.getStop().getStopName())
                        .toStopId(toTripStop.getStop().getStopId())
                        .toStopName(toTripStop.getStop().getStopName())
                        .estimatedDeparture(fromTripStop.getExpectedDepartureTime().format(TIME_FORMATTER))
                        .estimatedArrival(toTripStop.getExpectedArrivalTime().format(TIME_FORMATTER))
                        .liveBuses(Collections.emptyList()) // Phase 3 mein live bus data yahaan aayega
                        .build();

                // 6. JourneyOption (poora plan) banayein
                JourneyOption option = JourneyOption.builder()
                        .type("DIRECT")
                        .legs(Collections.singletonList(leg))
                        .estimatedDuration(durationMinutes + " mins")
                        .build();

                journeyOptions.add(option);
            }
        }

        log.info("Found {} direct options between {} and {}", journeyOptions.size(), fromStopName, toStopName);

        return JourneyPlanResponse.builder()
                .options(journeyOptions)
                .build();
    }

    /**
     * PHASE 2: Detailed Route View
     */
    @Override
    @Transactional(readOnly = true)
    public List<StopDetailDTO> getTripStopDetails(String gtfsTripId, String fromStopId, String toStopId) {

        log.info("Fetching stop details for trip: {} between {} and {}", gtfsTripId, fromStopId, toStopId);

        // 1. 'from' aur 'to' stops ke sequence numbers nikaalein
        TripStop fromTripStop = tripStopRepository.findByTrip_GtfsTripIdAndStop_StopId(gtfsTripId, fromStopId)
                .orElseThrow(() -> new RuntimeException("From stop not found for this trip."));

        TripStop toTripStop = tripStopRepository.findByTrip_GtfsTripIdAndStop_StopId(gtfsTripId, toStopId)
                .orElseThrow(() -> new RuntimeException("To stop not found for this trip."));

        int fromSequence = fromTripStop.getStopSequence();
        int toSequence = toTripStop.getStopSequence();

        // 2. Un sequence numbers ke beech ke saare stops dhoondhein
        List<TripStop> tripStops = tripStopRepository
                .findByTrip_GtfsTripIdAndStopSequenceBetweenOrderByStopSequenceAsc(
                        gtfsTripId,
                        fromSequence,
                        toSequence
                );

        // 3. Unhein DTO List mein convert karein
        return tripStops.stream()
                .map(ts -> StopDetailDTO.builder()
                        .stopName(ts.getStop().getStopName())
                        .stopSequence(ts.getStopSequence())
                        .arrivalTime(ts.getExpectedArrivalTime().format(TIME_FORMATTER))
                        .departureTime(ts.getExpectedDepartureTime().format(TIME_FORMATTER))
                        .build())
                .collect(Collectors.toList());
    }
}