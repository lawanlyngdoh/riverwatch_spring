package com.riverwatch.app.metrics;

import lombok.Getter;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds 15-minute buckets for a single calendar date (UTC).
 */
public class DayBuckets {

    @Getter
    private final LocalDate dateUtc;
    private final Bucket15m[] buckets; // size 96 (24 * 4)

    public static final int BUCKETS_PER_DAY = 24 * 4; // 96

    public DayBuckets(LocalDate dateUtc) {
        this.dateUtc = dateUtc;
        this.buckets = new Bucket15m[BUCKETS_PER_DAY];
    }

    /**
     * Get or create the bucket that corresponds to the given timestamp.
     */
    public Bucket15m getOrCreateBucket(Instant tsUtc) {
        ZonedDateTime zdt = tsUtc.atZone(ZoneOffset.UTC);
        if (!zdt.toLocalDate().equals(dateUtc)) {
            throw new IllegalArgumentException("Timestamp not in this DayBuckets date");
        }
        int index = bucketIndexFor(zdt.getHour(), zdt.getMinute());
        Bucket15m bucket = buckets[index];
        if (bucket == null) {
            Instant bucketStart = alignedBucketStart(tsUtc);
            bucket = new Bucket15m(bucketStart);
            buckets[index] = bucket;
        }
        return bucket;
    }

    /**
     * Get all non-null buckets in chronological order.
     */
    public List<Bucket15m> getAllBucketsSorted() {
        List<Bucket15m> result = new ArrayList<>();
        for (Bucket15m b : buckets) {
            if (b != null) {
                result.add(b);
            }
        }
        return result;
    }

    private static int bucketIndexFor(int hour, int minute) {
        int slot = minute / 15; // 0..3
        return hour * 4 + slot; // 0..95
    }

    private static Instant alignedBucketStart(Instant tsUtc) {
        long epochMillis = tsUtc.toEpochMilli();
        long minutes = epochMillis / (60_000L);
        long alignedMinutes = (minutes / 15) * 15;
        return Instant.ofEpochMilli(alignedMinutes * 60_000L);
    }
}
