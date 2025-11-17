package com.riverwatch.app.metrics;

import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central service that maintains in-memory 7-day buckets per camera.
 * DB integration (read/write) will be added on top of this.
 */
@Service
public class AggregationService {

    // cameraId -> CameraBucketRing
    private final Map<Long, CameraBucketRing> cameras = new ConcurrentHashMap<>();

    /**
     * Ingest a raw detection event.
     *
     * @param cameraId      DB id of the camera
     * @param tsUtc         timestamp of the event in UTC
     * @param detections    number of detections in this event
     * @param avgConfidence average confidence (0..1) for this event
     */
    public void ingestEvent(long cameraId, Instant tsUtc, int detections, double avgConfidence) {
        if (detections <= 0) {
            return;
        }
        CameraBucketRing ring = cameras.computeIfAbsent(cameraId, id -> new CameraBucketRing());
        ring.addEvent(tsUtc, detections, avgConfidence);
    }

    /**
     * Get today's buckets (UTC) for a single camera, ordered by time.
     */
    public List<Bucket15m> getTodayBuckets(long cameraId) {
        CameraBucketRing ring = cameras.get(cameraId);
        if (ring == null) {
            return Collections.emptyList();
        }
        return ring.getTodayBuckets();
    }

    /**
     * Get last N days (<=7) of buckets for a camera.
     */
    public Map<LocalDate, List<Bucket15m>> getLastNDays(long cameraId, int n) {
        CameraBucketRing ring = cameras.get(cameraId);
        if (ring == null) {
            return Collections.emptyMap();
        }
        return ring.getLastNDays(n);
    }

    /**
     * For future use: populate the ring for a camera from DB rows.
     * We'll implement this once the JPA/repository layer is ready.
     */
    public CameraBucketRing getOrCreateRing(long cameraId) {
        return cameras.computeIfAbsent(cameraId, id -> new CameraBucketRing());
    }
}
