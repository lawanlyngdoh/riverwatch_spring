package com.riverwatch.app.analytics;

import com.riverwatch.app.entity.CameraAggregate15m;
import com.riverwatch.app.repository.CameraAggregateRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final CameraAggregateRepository repo;

    public AnalyticsService(CameraAggregateRepository repo) {
        this.repo = repo;
    }

    /**
     * Compute analytics summary for all cameras between [from, to).
     */
    public AnalyticsSummary computeSummary(Instant from, Instant to) {
        List<CameraAggregate15m> rows = repo.findInRange(from, to);

        long totalDetections = 0L;
        double weightedConfSum = 0.0;
        long weightedConfCount = 0L;

        Map<String, Long> perCamera = new HashMap<>();
        Map<LocalDate, Long> perDay = new TreeMap<>();

        for (CameraAggregate15m row : rows) {
            int det = row.getDetectionsCount();
            double avgConf = row.getAvgConfidence();
            String cameraId = row.getId().getCameraId();
            Instant bucketStart = row.getId().getBucketStartUtc();

            totalDetections += det;

            if (det > 0) {
                weightedConfSum += avgConf * det;
                weightedConfCount += det;
            }

            perCamera.merge(cameraId, (long) det, Long::sum);

            LocalDate day = bucketStart
                    .atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(IST)
                    .toLocalDate();
            perDay.merge(day, (long) det, Long::sum);
        }

        double weightedAvgConf = weightedConfCount > 0
                ? weightedConfSum / weightedConfCount
                : 0.0;

        List<CameraDetections> cameraDetections = perCamera.entrySet().stream()
                .map(e -> new CameraDetections(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(CameraDetections::getDetections).reversed())
                .collect(Collectors.toList());

        List<DailyDetections> dailyDetections = perDay.entrySet().stream()
                .map(e -> new DailyDetections(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new AnalyticsSummary(
                totalDetections,
                weightedAvgConf,
                cameraDetections,
                dailyDetections
        );
    }

    // ===== DTOs with getters (Thymeleaf-friendly) =====

    public static class CameraDetections {
        private final String cameraId;
        private final long detections;

        public CameraDetections(String cameraId, long detections) {
            this.cameraId = cameraId;
            this.detections = detections;
        }

        public String getCameraId() {
            return cameraId;
        }

        public long getDetections() {
            return detections;
        }
    }

    public static class DailyDetections {
        private final LocalDate date;
        private final long detections;

        public DailyDetections(LocalDate date, long detections) {
            this.date = date;
            this.detections = detections;
        }

        public LocalDate getDate() {
            return date;
        }

        public long getDetections() {
            return detections;
        }
    }

    public static class AnalyticsSummary {
        private final long totalDetections;
        private final double avgConfidence;
        private final List<CameraDetections> cameraDetections;
        private final List<DailyDetections> dailyDetections;

        public AnalyticsSummary(long totalDetections,
                                double avgConfidence,
                                List<CameraDetections> cameraDetections,
                                List<DailyDetections> dailyDetections) {
            this.totalDetections = totalDetections;
            this.avgConfidence = avgConfidence;
            this.cameraDetections = cameraDetections;
            this.dailyDetections = dailyDetections;
        }

        public long getTotalDetections() {
            return totalDetections;
        }

        public double getAvgConfidence() {
            return avgConfidence;
        }

        public List<CameraDetections> getCameraDetections() {
            return cameraDetections;
        }

        public List<DailyDetections> getDailyDetections() {
            return dailyDetections;
        }
    }
}
