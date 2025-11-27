package com.riverwatch.app.metrics;

import java.time.*;
import java.util.*;

/**
 * Maintains a rolling 7-day window of DayBuckets for a single camera.
 * Dates are stored in UTC.
 */
public class CameraBucketRing {

    private static final int DAYS = 7;

    // Map<Date, DayBuckets>; we'll keep at most 7 entries
    private final NavigableMap<LocalDate, DayBuckets> days = new TreeMap<>();

    public void addEvent(Instant tsUtc, int count, double avgConfidence) {
        LocalDate date = tsUtc.atZone(ZoneOffset.UTC).toLocalDate();
        DayBuckets day = days.get(date);
        if (day == null) {
            day = new DayBuckets(date);
            days.put(date, day);
            trimOldDays();
        }
        Bucket15m bucket = day.getOrCreateBucket(tsUtc);
        bucket.addEvent(count, avgConfidence);
    }

    /**
     * Get buckets for a given date (UTC); returns empty list if no data.
     */
    public List<Bucket15m> getBucketsForDate(LocalDate dateUtc) {
        DayBuckets day = days.get(dateUtc);
        if (day == null) {
            return Collections.emptyList();
        }
        return day.getAllBucketsSorted();
    }

    /**
     * Get today's buckets (UTC).
     */
    public List<Bucket15m> getTodayBuckets() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return getBucketsForDate(today);
    }

    /**
     * Get buckets for the last N days (including today),
     * capped by the 7-day window.
     */
    public Map<LocalDate, List<Bucket15m>> getLastNDays(int n) {
        int limit = Math.min(n, DAYS);
        Map<LocalDate, List<Bucket15m>> result = new LinkedHashMap<>();
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        for (int i = 0; i < limit; i++) {
            LocalDate d = today.minusDays(i);
            List<Bucket15m> buckets = getBucketsForDate(d);
            if (!buckets.isEmpty()) {
                result.put(d, buckets);
            }
        }
        return result;
    }

    private void trimOldDays() {
        while (days.size() > DAYS) {
            LocalDate first = days.firstKey();
            days.remove(first);
        }
    }
}
