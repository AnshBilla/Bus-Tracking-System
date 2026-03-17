package com.smartRahi.SmartRahi.Repository.Projections;

    public interface NearbyStopProjection {

        // Query mein 's.stop_id AS "stopId"' se match karta hai
        String getStopId();

        // Query mein 's.stop_name AS "stopName"' se match karta hai
        String getStopName();

        // Query mein 's.stop_lat AS "stopLat"' se match karta hai
        Double getStopLat();

        // Query mein 's.stop_lon AS "stopLon"' se match karta hai
        Double getStopLon();

        // Query mein 'distance' AS "distance"' se match karta hai
        Double getDistance();

    }

