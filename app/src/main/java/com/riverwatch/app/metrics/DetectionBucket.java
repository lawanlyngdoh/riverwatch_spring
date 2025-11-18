package com.riverwatch.app.metrics;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory 15-minute bucket of detection confidences for a single camera.
 * AggregationService persists its stats into CameraAggregate15m.
 */
public class DetectionBucket {

    // Keep this consistent with your 15-minute alignment logic
    private static final Duration BUCKET_WIDTH = Duration.ofMinutes(15);

    private final String cameraId;
    private final Instant bucketStartUtc;
    private final Instant bucketEndUtc;

    private final List<Double> confidences = new ArrayList<>();

    public DetectionBucket(String cameraId, Instant bucketStartUtc) {
        this.cameraId = cameraId;
        this.bucketStartUtc = bucketStartUtc;
        this.bucketEndUtc = bucketStartUtc.plus(BUCKET_WIDTH);
    }

    /** Add a single detection confidence into this bucket. */
    public void addDetection(double confidence) {
        confidences.add(confidence);
    }

    public String getCameraId() {
        return cameraId;
    }

    public Instant getBucketStartUtc() {
        return bucketStartUtc;
    }

    public Instant getBucketEndUtc() {
        return bucketEndUtc;
    }

    public int getCount() {
        return confidences.size();
    }

    public List<Double> getConfidences() {
        return confidences;
    }

    public double getAvg() {
        if (confidences.isEmpty()) {
            return 0.0;
        }
        double sum = 0.0;
        for (double c : confidences) {
            sum += c;
        }
        return sum / confidences.size();
    }

    public double getMin() {
        if (confidences.isEmpty()) {
            return 0.0;
        }
        double min = Double.POSITIVE_INFINITY;
        for (double c : confidences) {
            if (c < min) {
                min = c;
            }
        }
        return min;
    }

    public double getMax() {
        if (confidences.isEmpty()) {
            return 0.0;
        }
        double max = Double.NEGATIVE_INFINITY;
        for (double c : confidences) {
            if (c > max) {
                max = c;
            }
        }
        return max;
    }

    public double getMedian() {
        if (confidences.isEmpty()) {
            return 0.0;
        }
        List<Double> copy = new ArrayList<>(confidences);
        Collections.sort(copy);
        int n = copy.size();
        if (n % 2 == 1) {
            return copy.get(n / 2);
        } else {
            return (copy.get(n / 2 - 1) + copy.get(n / 2)) / 2.0;
        }
    }

    public double getStddev() {
        int n = confidences.size();
        if (n <= 1) {
            return 0.0;
        }
        double mean = getAvg();
        double sumSq = 0.0;
        for (double c : confidences) {
            double d = c - mean;
            sumSq += d * d;
        }
        double variance = sumSq / n;
        return Math.sqrt(variance);
    }
}
