package com.riverwatch.app.web;

import com.riverwatch.app.metrics.AggregationService;
import com.riverwatch.app.metrics.AggregationService.BucketPointDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DetectionController {

    private final AggregationService aggregationService;

    public DetectionController(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    // Feeder endpoint: Python script calls this
    @PostMapping("/detections")
    public ResponseEntity<Void> ingestDetection(@RequestBody DetectionRequest request) {
        String cameraId = request.cameraId() != null ? request.cameraId() : "cam_01";
        double conf = request.confidence() != null ? request.confidence() : 0.8;

        Instant ts;
        if (request.timestampUtc() != null && !request.timestampUtc().isBlank()) {
            ts = Instant.parse(request.timestampUtc());
        } else {
            ts = Instant.now();
        }

        aggregationService.ingestDetection(cameraId, conf, ts);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


    public record DetectionRequest(
            String cameraId,
            Double confidence,
            String timestampUtc
    ) {}
}
