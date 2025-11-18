package com.riverwatch.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Composite primary key for camera_aggregate_15m:
 * (camera_id, bucket_start_utc)
 */
@Embeddable
public class CameraAggregateId implements Serializable {

    @Column(name = "camera_id", nullable = false, length = 50)
    private String cameraId;

    @Column(name = "bucket_start_utc", nullable = false)
    private Instant bucketStartUtc;

    public CameraAggregateId() {
    }

    public CameraAggregateId(String cameraId, Instant bucketStartUtc) {
        this.cameraId = cameraId;
        this.bucketStartUtc = bucketStartUtc;
    }

    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    public Instant getBucketStartUtc() {
        return bucketStartUtc;
    }

    public void setBucketStartUtc(Instant bucketStartUtc) {
        this.bucketStartUtc = bucketStartUtc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraAggregateId that)) return false;
        return Objects.equals(cameraId, that.cameraId)
                && Objects.equals(bucketStartUtc, that.bucketStartUtc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cameraId, bucketStartUtc);
    }

    @Override
    public String toString() {
        return "CameraAggregateId{" +
                "cameraId='" + cameraId + '\'' +
                ", bucketStartUtc=" + bucketStartUtc +
                '}';
    }
}
