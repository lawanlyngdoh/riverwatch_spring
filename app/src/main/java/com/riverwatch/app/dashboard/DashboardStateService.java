package com.riverwatch.app.dashboard;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.riverwatch.app.dashboard.DashboardDtos.*;

@Service
public class DashboardStateService {

    private final Map<String, DashboardStatsView> current = new ConcurrentHashMap<>();
    private final Map<String, Deque<TimePoint>> series = new ConcurrentHashMap<>();

    private static final int MAX_POINTS = 500;

    // For a smooth 2s cadence on the UI
    private static final long TICK_MS = 2000L;
    // How many points to show if we have no data yet (flat line)
    private static final int DEFAULT_POINTS = 30; // ~1 minute at 2s

    public void upsert(String loc, DashboardStatsPayload payload) {
        var view = new DashboardStatsView(
                loc,
                nz(payload.activeCameras(), 0),
                nz(payload.detectionsToday(), 0),
                payload.modelVersion() == null ? "v0.0.0" : payload.modelVersion(),
                payload.modelStage() == null ? "dev" : payload.modelStage(),
                payload.avgConfidence() == null ? 0.0 : payload.avgConfidence()
        );
        current.put(loc, view);

        if (payload.detectionsSeries() != null && !payload.detectionsSeries().isEmpty()) {
            var dq = series.computeIfAbsent(loc, k -> new ArrayDeque<>());
            for (var p : payload.detectionsSeries()) {
                dq.addLast(p);
            }
            while (dq.size() > MAX_POINTS) dq.removeFirst();
        }
    }

    public DashboardStatsView getCurrent(String loc, String fallbackLoc) {
        if (loc == null || loc.isBlank()) loc = fallbackLoc;
        return current.getOrDefault(loc,
                new DashboardStatsView(loc == null ? "default" : loc, 0, 0, "v0.0.0", "dev", 0.0));
    }

    public SeriesResponse getSeries(String loc, String fallbackLoc) {
        if (loc == null || loc.isBlank()) loc = fallbackLoc;
        var dq = series.getOrDefault(loc, new ArrayDeque<>());

        // If empty, return a flat zero line aligned to 2s ticks
        if (dq.isEmpty()) {
            long now = System.currentTimeMillis();
            long alignedNow = (now / TICK_MS) * TICK_MS;
            List<TimePoint> pad = new ArrayList<>(DEFAULT_POINTS);
            long start = alignedNow - (DEFAULT_POINTS - 1) * TICK_MS;
            for (int i = 0; i < DEFAULT_POINTS; i++) {
                pad.add(new TimePoint(start + i * TICK_MS, 0));
            }
            return new SeriesResponse(loc, pad, Instant.now());
        }

        // Otherwise, return the actual deque as-is
        return new SeriesResponse(loc, List.copyOf(dq), Instant.now());
    }

    private static Integer nz(Integer v, int d) { return v == null ? d : v; }
}
