package com.riverwatch.app.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.riverwatch.app.dashboard.DashboardDtos.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardStateService state;

    // Feeder posts here (Python script)
    @PostMapping("/update")
    public ResponseEntity<Void> update(@RequestBody DashboardStatsPayload payload) {
        if (payload.locationId() == null || payload.locationId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        state.upsert(payload.locationId(), payload);
        return ResponseEntity.accepted().build();
    }

    // Dashboard pulls current KPIs
    @GetMapping("/stats")
    public DashboardStatsView stats(@RequestParam(value = "loc", required = false) String loc) {
        // default fallback aligns with your controller default
        return state.getCurrent(loc, "wah_umkhrah");
    }

    // Dashboard pulls time series
    @GetMapping("/series")
    public SeriesResponse series(@RequestParam(value = "loc", required = false) String loc) {
        return state.getSeries(loc, "wah_umkhrah");
    }
}
