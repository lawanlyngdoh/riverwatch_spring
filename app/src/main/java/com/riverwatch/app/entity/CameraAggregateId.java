package com.riverwatch.app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Composite primary key for camera_aggregate_15m:
 * (camera_id, bucket_start)
 */
@Embeddable
public class CameraAggregateId implements Serializable {

    @Column(name = "camera_id", nullable = false)
    private Long cameraId;

    @Column(name = "bucket_start", nullable = false)
    private Instant bucketStart;

    // Required by JPA
    public CameraAggregateId() {
    }

    public CameraAggregateId(Long cameraId, Instant bucketStart) {
        this.cameraId = cameraId;
        this.bucketStart = bucketStart;
    }

    public Long getCameraId() {
        return cameraId;
    }

    public void setCameraId(Long cameraId) {
        this.cameraId = cameraId;
    }

    public Instant getBucketStart() {
        return bucketStart;
    }

    public void setBucketStart(Instant bucketStart) {
        this.bucketStart = bucketStart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CameraAggregateId that)) return false;
        return Objects.equals(cameraId, that.cameraId)
                && Objects.equals(bucketStart, that.bucketStart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cameraId, bucketStart);
    }

    @Override
    public String toString() {
        return "CameraAggregateId{" +
                "cameraId=" + cameraId +
                ", bucketStart=" + bucketStart +
                '}';
    }
}
