package com.riverwatch.app.dashboard;

import java.time.Instant;
import java.util.List;

public class DashboardDtos
{

    public record TimePoint(long t, int value) {} // t = epoch millis (for chart)

    public record DashboardStatsPayload(
            String locationId,
            Integer activeCameras,
            Integer detectionsToday,
            String modelVersion,
            String modelStage,
            Double avgConfidence,
            List<TimePoint> detectionsSeries // optional
    ) {}

    public record DashboardStatsView(
            String locationId,
            Integer activeCameras,
            Integer detectionsToday,
            String modelVersion,
            String modelStage,
            Double avgConfidence
    ) {}

    public record SeriesResponse(String locationId, List<TimePoint> series, Instant serverTime) {}
}
