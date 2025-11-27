package com.riverwatch.app.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.riverwatch.app.entity.CameraAggregate15m;
import com.riverwatch.app.entity.CameraAggregateId;
import com.riverwatch.app.repository.CameraAggregateRepository;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AggregationService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final Duration RETENTION = Duration.ofDays(7);

    private final CameraAggregateRepository repo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // cameraId -> (bucketStartUtc -> bucket)
    private final Map<String, NavigableMap<Instant, DetectionBucket>> buckets = new ConcurrentHashMap<>();

    public AggregationService(CameraAggregateRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void initFromDatabase() {
        Instant cutoff = Instant.now().minus(RETENTION);
        try {
            List<CameraAggregate15m> rows = repo.findAllAfterAnyCamera(cutoff);

            for (CameraAggregate15m row : rows) {
                String cameraId = row.getId().getCameraId();
                Instant bucketStartUtc = row.getId().getBucketStartUtc();

                NavigableMap<Instant, DetectionBucket> cameraMap =
                        buckets.computeIfAbsent(cameraId, k -> new TreeMap<>());

                DetectionBucket bucket = new DetectionBucket(cameraId, bucketStartUtc);
                int count = row.getDetectionsCount();
                double avg = row.getAvgConfidence();

                for (int i = 0; i < count; i++) {
                    bucket.addDetection(avg);
                }

                cameraMap.put(bucketStartUtc, bucket);
            }

            System.out.println("AggregationService initFromDatabase: restored "
                    + rows.size() + " buckets from DB");
        } catch (Exception ex) {
            // POC-safe: don't kill the app if schema/table isn't ready yet.
            System.out.println("AggregationService initFromDatabase: skipped due to error: "
                    + ex.getMessage());
        }
    }


    public void ingestDetection(String cameraId, double confidence, Instant detectionTimeUtc) {
        if (detectionTimeUtc == null) {
            detectionTimeUtc = Instant.now();
        }

        Instant bucketStartUtc = computeBucketStartIstAligned(detectionTimeUtc);
        DetectionBucket bucket = getOrCreateBucket(cameraId, bucketStartUtc);
        bucket.addDetection(confidence);

        pruneOldBuckets(cameraId);
        syncBucketToDb(bucket);
    }

    private Instant computeBucketStartIstAligned(Instant detectionTimeUtc) {
        ZonedDateTime istTime = detectionTimeUtc.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(IST);
        int bucketMinute = istTime.getMinute();
        ZonedDateTime bucketStartIst = istTime
                .withMinute(bucketMinute)
                .withSecond(0)
                .withNano(0);
        return bucketStartIst.withZoneSameInstant(ZoneOffset.UTC).toInstant();
    }

    private DetectionBucket getOrCreateBucket(String cameraId, Instant bucketStartUtc) {
        NavigableMap<Instant, DetectionBucket> cameraMap =
                buckets.computeIfAbsent(cameraId, k -> new TreeMap<>());

        return cameraMap.computeIfAbsent(bucketStartUtc,
                k -> new DetectionBucket(cameraId, bucketStartUtc));
    }

    private void pruneOldBuckets(String cameraId) {
        NavigableMap<Instant, DetectionBucket> cameraMap = buckets.get(cameraId);
        if (cameraMap == null) return;

        Instant cutoff = Instant.now().minus(RETENTION);
        NavigableMap<Instant, DetectionBucket> head = cameraMap.headMap(cutoff, false);
        Set<Instant> toRemove = new HashSet<>(head.keySet());
        for (Instant key : toRemove) {
            cameraMap.remove(key);
        }
    }

    private void syncBucketToDb(DetectionBucket bucket) {
        CameraAggregateId id = new CameraAggregateId(
                bucket.getCameraId(),
                bucket.getBucketStartUtc()
        );
        CameraAggregate15m entity = repo.findById(id).orElse(new CameraAggregate15m(id));

        entity.setBucketEndUtc(bucket.getBucketEndUtc());
        entity.setDetectionsCount(bucket.getCount());
        entity.setAvgConfidence(bucket.getAvg());
        entity.setMinConfidence(bucket.getMin());
        entity.setMaxConfidence(bucket.getMax());
        entity.setMedianConfidence(bucket.getMedian());
        entity.setStddevConfidence(bucket.getStddev());
        entity.setUpdatedAtUtc(Instant.now());

        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("confidences_sample", bucket.getConfidences().size() <= 20
                ? bucket.getConfidences()
                : bucket.getConfidences().subList(0, 20));
        try {
            entity.setMetaJson(objectMapper.writeValueAsString(meta));
        } catch (JsonProcessingException e) {
            entity.setMetaJson("{}");
        }

        repo.save(entity);
    }

    public List<BucketPointDto> getSeries(String cameraId, Duration window) {
        Instant now = Instant.now();
        Instant from = now.minus(window);

        NavigableMap<Instant, DetectionBucket> cameraMap =
                buckets.getOrDefault(cameraId, new TreeMap<>());

        List<BucketPointDto> result = new ArrayList<>();

        cameraMap.tailMap(from, true).forEach((instant, bucket) -> {
            ZonedDateTime ist = instant.atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(IST);
            result.add(new BucketPointDto(
                    instant,
                    ist.toLocalDateTime().toString(),
                    bucket.getCount(),
                    bucket.getAvg(),
                    bucket.getMin(),
                    bucket.getMax(),
                    bucket.getMedian(),
                    bucket.getStddev()
            ));
        });

        result.sort(Comparator.comparing(BucketPointDto::getBucketStartUtc));
        return result;
    }
    /**
     * Returns the last N 15-minute buckets for a camera, oldest first,
     * for use by /api/dashboard/series.
     */
    public List<BucketPointDto> getSeriesForCamera(String cameraId, int limit) {
        // 15 minutes * limit = window to look back
        Duration window = Duration.ofMinutes(15L * limit);

        List<BucketPointDto> series = getSeries(cameraId, window);
        int size = series.size();
        if (size <= limit) {
            return series;
        }
        // Keep only the last N points
        return series.subList(size - limit, size);
    }



    public static class BucketPointDto {
        private final Instant bucketStartUtc;
        private final String bucketStartLocalIst;
        private final int count;
        private final double avg;
        private final double min;
        private final double max;
        private final double median;
        private final double stddev;

        public BucketPointDto(Instant bucketStartUtc,
                              String bucketStartLocalIst,
                              int count,
                              double avg,
                              double min,
                              double max,
                              double median,
                              double stddev) {
            this.bucketStartUtc = bucketStartUtc;
            this.bucketStartLocalIst = bucketStartLocalIst;
            this.count = count;
            this.avg = avg;
            this.min = min;
            this.max = max;
            this.median = median;
            this.stddev = stddev;
        }

        public Instant getBucketStartUtc() {
            return bucketStartUtc;
        }

        public String getBucketStartLocalIst() {
            return bucketStartLocalIst;
        }

        public int getCount() {
            return count;
        }

        public int getDetectionsCount() {
            return count;
        }


        public double getAvg() {
            return avg;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double getMedian() {
            return median;
        }

        public double getStddev() {
            return stddev;
        }
    }
}
