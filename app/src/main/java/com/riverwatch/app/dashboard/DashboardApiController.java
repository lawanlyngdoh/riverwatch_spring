package com.riverwatch.app.dashboard;

import com.riverwatch.app.metrics.AggregationService;
import com.riverwatch.app.metrics.AggregationService.BucketPointDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class DashboardApiController {

    private final AggregationService aggregationService;

    public DashboardApiController(AggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    /**
     * Returns 15-minute bucketed detections for a given camera.
     *
     * Response shape stays compatible with your existing frontend:
     *
     * {
     *   "locationId": "wah_umkhrah",
     *   "series": [ { "t": 1234567890000, "value": 5 }, ... ],
     *   "serverTime": "2025-11-17T05:40:15Z"
     * }
     */
    @GetMapping("/api/dashboard/series")
    public Map<String, Object> series(
            @RequestParam(defaultValue = "cam_01") String cameraId) {

        // 96 = 24 hours / 15 minutes
        List<BucketPointDto> buckets =
                aggregationService.getSeriesForCamera(cameraId, 96);

        // Adapt to {t, value} for your existing chart JS
        List<Map<String, Object>> series = buckets.stream()
                .map(b -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("t", b.getBucketStartUtc().toEpochMilli());   // X axis
                    point.put("value", b.getDetectionsCount());             // Y axis
                    return point;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("locationId", "wah_umkhrah");        // or cameraId, if you prefer
        response.put("series", series);
        response.put("serverTime", Instant.now().toString());
        return response;
    }
}
