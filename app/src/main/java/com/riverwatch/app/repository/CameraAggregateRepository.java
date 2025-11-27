package com.riverwatch.app.repository;

import com.riverwatch.app.entity.CameraAggregate15m;
import com.riverwatch.app.entity.CameraAggregateId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface CameraAggregateRepository extends JpaRepository<CameraAggregate15m, CameraAggregateId> {

    @Query("""
        SELECT c FROM CameraAggregate15m c
        WHERE c.id.cameraId = :cameraId
          AND c.id.bucketStartUtc >= :after
        ORDER BY c.id.bucketStartUtc ASC
        """)
    List<CameraAggregate15m> findAllAfter(
            @Param("cameraId") String cameraId,
            @Param("after") Instant after
    );

    @Query("""
        SELECT c FROM CameraAggregate15m c
        WHERE c.id.bucketStartUtc >= :after
        ORDER BY c.id.cameraId ASC, c.id.bucketStartUtc ASC
        """)
    List<CameraAggregate15m> findAllAfterAnyCamera(
            @Param("after") Instant after
    );

    // Range query for analytics (note: parameter names are NOT from/to)
    @Query("""
        SELECT c FROM CameraAggregate15m c
        WHERE c.id.bucketStartUtc >= :startTs
          AND c.id.bucketStartUtc < :endTs
        ORDER BY c.id.bucketStartUtc ASC
        """)
    List<CameraAggregate15m> findInRange(
            @Param("startTs") Instant startTs,
            @Param("endTs") Instant endTs
    );
}
