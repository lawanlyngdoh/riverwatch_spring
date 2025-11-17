package com.riverwatch.app.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * JPA entity representing one 15-minute aggregated bucket for a camera.
 */
@Entity
@Table(
        name = "camera_aggregate_15m",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"camera_id", "bucket_start"})
        }
)
public class CameraAggregate15m {

    @EmbeddedId
    private CameraAggregateId id;

    @Column(name = "bucket_end", nullable = false)
    private Instant bucketEnd;

    @Column(name = "detections_count", nullable = false)
    private int detectionsCount;

    @Column(name = "avg_confidence", nullable = false)
    private float avgConfidence;

    @Column(name = "min_confidence", nullable = false)
    private float minConfidence;

    @Column(name = "max_confidence", nullable = false)
    private float maxConfidence;

    @Column(name = "peak_density", nullable = false)
    private float peakDensity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Required no-args constructor
    public CameraAggregate15m() {}

    public CameraAggregate15m(CameraAggregateId id) {
        this.id = id;
        this.bucketEnd = id.getBucketStart().plusSeconds(15 * 60);
        this.updatedAt = Instant.now();
    }

    // ---------------- GETTERS & SETTERS ----------------

    public CameraAggregateId getId() {
        return id;
    }

    public void setId(CameraAggregateId id) {
        this.id = id;
    }

    public Instant getBucketEnd() {
        return bucketEnd;
    }

    public void setBucketEnd(Instant bucketEnd) {
        this.bucketEnd = bucketEnd;
    }

    public int getDetectionsCount() {
        return detectionsCount;
    }

    public void setDetectionsCount(int detectionsCount) {
        this.detectionsCount = detectionsCount;
    }

    public float getAvgConfidence() {
        return avgConfidence;
    }

    public void setAvgConfidence(float avgConfidence) {
        this.avgConfidence = avgConfidence;
    }

    public float getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(float minConfidence) {
        this.minConfidence = minConfidence;
    }

    public float getMaxConfidence() {
        return maxConfidence;
    }

    public void setMaxConfidence(float maxConfidence) {
        this.maxConfidence = maxConfidence;
    }

    public float getPeakDensity() {
        return peakDensity;
    }

    public void setPeakDensity(float peakDensity) {
        this.peakDensity = peakDensity;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
