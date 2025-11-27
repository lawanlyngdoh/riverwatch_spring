package com.riverwatch.app.metrics;

import lombok.Getter;

import java.time.Instant;

/**
 * Represents a single 15-minute aggregate bucket for one camera.
 */
public class Bucket15m {

    @Getter
    private final Instant bucketStartUtc;

    @Getter
    private int detectionsCount;          // total detections in this bucket
    private int detectionEvents;          // number of events contributing (for safety)
    private double sumConfWeighted;       // sum(avg_confidence * count) for weighted average
    private double minConfidence;
    private double maxConfidence;
    @Getter
    private double peakDensity;           // max detections in a single event/window

    public Bucket15m(Instant bucketStartUtc) {
        this.bucketStartUtc = bucketStartUtc;
        this.detectionsCount = 0;
        this.detectionEvents = 0;
        this.sumConfWeighted = 0.0;
        this.minConfidence = 1.0; // start high, so first update lowers it
        this.maxConfidence = 0.0; // start low, so first update raises it
        this.peakDensity = 0.0;
    }

    public double getAvgConfidence() {
        if (detectionsCount == 0) {
            return 0.0;
        }
        return sumConfWeighted / detectionsCount;
    }

    public double getMinConfidence() {
        return detectionsCount == 0 ? 0.0 : minConfidence;
    }

    public double getMaxConfidence() {
        return detectionsCount == 0 ? 0.0 : maxConfidence;
    }

    /**
     * Update this bucket with a new detection event.
     *
     * @param count         number of detections in this event
     * @param avgConfidence average confidence (0..1) for this event
     */
    public void addEvent(int count, double avgConfidence) {
        if (count <= 0) {
            return;
        }
        double conf = clamp(avgConfidence, 0.0, 1.0);

        detectionsCount += count;
        detectionEvents += 1;

        sumConfWeighted += conf * count;

        if (conf < minConfidence) {
            minConfidence = conf;
        }
        if (conf > maxConfidence) {
            maxConfidence = conf;
        }

        if (count > peakDensity) {
            peakDensity = count;
        }
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
