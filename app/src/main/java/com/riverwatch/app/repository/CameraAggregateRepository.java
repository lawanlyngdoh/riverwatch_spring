package com.riverwatch.app.repository;

import com.riverwatch.app.entity.CameraAggregate15m;
import com.riverwatch.app.entity.CameraAggregateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CameraAggregateRepository extends JpaRepository<CameraAggregate15m, CameraAggregateId> {

    @Query("SELECT c FROM CameraAggregate15m c " +
            "WHERE c.id.cameraId = :cameraId " +
            "AND c.id.bucketStart >= :after " +
            "ORDER BY c.id.bucketStart ASC")
    List<CameraAggregate15m> findAllAfter(
            @Param("cameraId") Long cameraId,
            @Param("after") Instant after
    );
}
