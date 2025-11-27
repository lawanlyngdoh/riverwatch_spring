package com.riverwatch.app.analytics;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class AnalyticsController {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    /**
     * Tiny in-memory “sample dataset” for analytics.
     * Pretend these are 15-minute buckets from cam_01 / cam_02 / cam_03
     * over 3 days, with detection counts + avg confidence.
     */
    private static final List<BucketSample> BUCKETS = List.of(
            // day 1
            new BucketSample("cam_01", LocalDate.now(IST).minusDays(2), 5, 0.82),
            new BucketSample("cam_01", LocalDate.now(IST).minusDays(2), 7, 0.88),
            new BucketSample("cam_02", LocalDate.now(IST).minusDays(2), 4, 0.79),
            new BucketSample("cam_03", LocalDate.now(IST).minusDays(2), 3, 0.75),

            // day 2
            new BucketSample("cam_01", LocalDate.now(IST).minusDays(1), 10, 0.90),
            new BucketSample("cam_01", LocalDate.now(IST).minusDays(1), 6, 0.86),
            new BucketSample("cam_02", LocalDate.now(IST).minusDays(1), 8, 0.83),
            new BucketSample("cam_03", LocalDate.now(IST).minusDays(1), 5, 0.80),

            // day 3 (today)
            new BucketSample("cam_01", LocalDate.now(IST), 12, 0.91),
            new BucketSample("cam_01", LocalDate.now(IST), 9, 0.89),
            new BucketSample("cam_02", LocalDate.now(IST), 14, 0.87),
            new BucketSample("cam_03", LocalDate.now(IST), 5, 0.82)
    );

    @GetMapping("/analytics")
    public String analytics(Model model) {
        LocalDate today = LocalDate.now(IST);
        LocalDate from = today.minusDays(2); // 3-day window

        // Filter to range (here everything fits, but logic is “real”)
        List<BucketSample> inRange = BUCKETS.stream()
                .filter(b -> !b.date().isBefore(from) && !b.date().isAfter(today))
                .collect(Collectors.toList());

        // --- Aggregate totals & weighted avg confidence ---
        long totalDetections = inRange.stream()
                .mapToLong(BucketSample::count)
                .sum();

        double weightedConfSum = inRange.stream()
                .mapToDouble(b -> b.avgConfidence() * b.count())
                .sum();
        long weightedCount = inRange.stream()
                .mapToLong(BucketSample::count)
                .sum();

        double avgConfidence = weightedCount > 0
                ? weightedConfSum / weightedCount
                : 0.0;
        double avgConfidencePct = avgConfidence * 100.0;

        // --- Per-camera totals ---
        Map<String, Long> byCamera = inRange.stream()
                .collect(Collectors.groupingBy(
                        BucketSample::cameraId,
                        Collectors.summingLong(BucketSample::count)
                ));

        List<CameraRow> cameraStats = byCamera.entrySet().stream()
                .map(e -> new CameraRow(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingLong(CameraRow::getDetections).reversed())
                .collect(Collectors.toList());

        // --- Per-day totals ---
        Map<LocalDate, Long> byDay = inRange.stream()
                .collect(Collectors.groupingBy(
                        BucketSample::date,
                        TreeMap::new,
                        Collectors.summingLong(BucketSample::count)
                ));

        List<DailyRow> dailyStats = byDay.entrySet().stream()
                .map(e -> new DailyRow(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // --- Simple insights ---
        CameraRow topCamera = cameraStats.stream()
                .findFirst()
                .orElse(null);

        DailyRow busiestDay = dailyStats.stream()
                .max(Comparator.comparingLong(DailyRow::getDetections))
                .orElse(null);

        // trend: compare today vs previous day
        DailyRow todayRow = dailyStats.stream()
                .filter(d -> d.getDate().equals(today))
                .findFirst()
                .orElse(null);

        DailyRow prevRow = dailyStats.stream()
                .filter(d -> d.getDate().equals(today.minusDays(1)))
                .findFirst()
                .orElse(null);

        String trendLabel = "No change";
        double trendDeltaPct = 0.0;
        if (todayRow != null && prevRow != null && prevRow.getDetections() > 0) {
            double delta = todayRow.getDetections() - prevRow.getDetections();
            trendDeltaPct = (delta / prevRow.getDetections()) * 100.0;
            trendLabel = delta >= 0 ? "Increase" : "Decrease";
        }

        // ----- MODEL -----
        model.addAttribute("pageTitle", "Analytics");
        model.addAttribute("active", "analytics");

        model.addAttribute("fromDate", from);
        model.addAttribute("toDate", today);

        model.addAttribute("totalDetections", totalDetections);
        model.addAttribute("avgConfidencePct", avgConfidencePct);

        model.addAttribute("cameraStats", cameraStats);
        model.addAttribute("dailyStats", dailyStats);

        model.addAttribute("topCamera", topCamera);
        model.addAttribute("busiestDay", busiestDay);
        model.addAttribute("trendLabel", trendLabel);
        model.addAttribute("trendDeltaPct", trendDeltaPct);

        return "analytics";
    }

    // ===== Internal sample / DTO classes =====

    private record BucketSample(String cameraId, LocalDate date, int count, double avgConfidence) {}

    public static class CameraRow {
        private final String cameraId;
        private final long detections;

        public CameraRow(String cameraId, long detections) {
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

    public static class DailyRow {
        private final LocalDate date;
        private final long detections;

        public DailyRow(LocalDate date, long detections) {
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
}
