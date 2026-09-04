package com.guardianai;

import org.junit.Test;
import static org.junit.Assert.*;

public class LocationDeduplicationTest {

    private float calculateDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (float) (6371000 * c);
    }

    @Test
    public void testDistanceAndTimeThresholds() {
        double lat1 = 37.7749, lon1 = -122.4194;
        long time1 = 1000000L;

        double lat2 = 37.7749, lon2 = -122.4194;
        long time2 = 1000500L;

        float distance = calculateDistanceMeters(lat1, lon1, lat2, lon2);
        long timeDiff = Math.abs(time2 - time1);

        assertTrue(distance < 1.0f);
        assertTrue(timeDiff < 1000L);
    }

    @Test
    public void testLegitimateMovementPassesFilter() {
        double lat1 = 37.7749, lon1 = -122.4194;
        long time1 = 1000000L;

        double lat2 = 37.7800, lon2 = -122.4100;
        long time2 = 1030000L;

        float distance = calculateDistanceMeters(lat1, lon1, lat2, lon2);
        long timeDiff = Math.abs(time2 - time1);

        assertTrue(distance >= 1.0f);
        assertTrue(timeDiff >= 1000L);
    }
}
