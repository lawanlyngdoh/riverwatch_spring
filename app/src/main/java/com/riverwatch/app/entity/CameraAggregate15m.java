package com.riverwatch.app.entity;

import jakarta.persistence.*;

import java.time.Instant;

/**
 * One 15-minute aggregated bucket of detections for a given camera.
 */
@Entity
@Table(
        name = "camera_aggregate_15m",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"camera_id", "bucket_start_utc"})
        }
)
public class CameraAggregate15m {

    @EmbeddedId
    private CameraAggregateId id;

    @Column(name = "bucket_end_utc", nullable = false)
    private Instant bucketEndUtc;

    @Column(name = "detections_count", nullable = false)
    private int detectionsCount;

    @Column(name = "avg_confidence", nullable = false)
    private double avgConfidence;

    @Column(name = "min_confidence", nullable = false)
    private double minConfidence;

    @Column(name = "max_confidence", nullable = false)
    private double maxConfidence;

    @Column(name = "median_confidence", nullable = false)
    private double medianConfidence;

    @Column(name = "stddev_confidence", nullable = false)
    private double stddevConfidence;

    // For future analytics: JSON blob containing extra info
    @Lob
    @Column(name = "meta_json")
    private String metaJson;

    @Column(name = "updated_at_utc", nullable = false)
    private Instant updatedAtUtc;

    public CameraAggregate15m() {
    }

    public CameraAggregate15m(CameraAggregateId id) {
        this.id = id;
        this.bucketEndUtc = id.getBucketStartUtc().plusSeconds(15 * 60L);
        this.updatedAtUtc = Instant.now();
    }

    public CameraAggregateId getId() {
        return id;
    }

    public void setId(CameraAggregateId id) {
        this.id = id;
    }

    public Instant getBucketEndUtc() {
        return bucketEndUtc;
    }

    public void setBucketEndUtc(Instant bucketEndUtc) {
        this.bucketEndUtc = bucketEndUtc;
    }

    public int getDetectionsCount() {
        return detectionsCount;
    }

    public void setDetectionsCount(int detectionsCount) {
        this.detectionsCount = detectionsCount;
    }

    public double getAvgConfidence() {
        return avgConfidence;
    }

    public void setAvgConfidence(double avgConfidence) {
        this.avgConfidence = avgConfidence;
    }

    public double getMinConfidence() {
        return minConfidence;
    }

    public void setMinConfidence(double minConfidence) {
        this.minConfidence = minConfidence;
    }

    public double getMaxConfidence() {
        return maxConfidence;
    }

    public void setMaxConfidence(double maxConfidence) {
        this.maxConfidence = maxConfidence;
    }

    public double getMedianConfidence() {
        return medianConfidence;
    }

    public void setMedianConfidence(double medianConfidence) {
        this.medianConfidence = medianConfidence;
    }

    public double getStddevConfidence() {
        return stddevConfidence;
    }

    public void setStddevConfidence(double stddevConfidence) {
        this.stddevConfidence = stddevConfidence;
    }

    public String getMetaJson() {
        return metaJson;
    }

    public void setMetaJson(String metaJson) {
        this.metaJson = metaJson;
    }

    public Instant getUpdatedAtUtc() {
        return updatedAtUtc;
    }

    public void setUpdatedAtUtc(Instant updatedAtUtc) {
        this.updatedAtUtc = updatedAtUtc;
    }
}
