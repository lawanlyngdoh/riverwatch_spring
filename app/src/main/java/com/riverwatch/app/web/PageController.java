package com.riverwatch.app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PageController {

    // --- Simple POJO for the dropdown ---
    public static class LocationOption {
        private String id;
        private String name;

        public LocationOption() {}
        public LocationOption(String id, String name) { this.id = id; this.name = name; }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // --- Provide a demo list until DB is wired ---
    private List<LocationOption> demoLocations() {
        List<LocationOption> list = new ArrayList<>();
        list.add(new LocationOption("wah_umkhrah", "Wah Umkhrah"));
        list.add(new LocationOption("wah_umshyrpi", "Wah Umshyrpi"));
        list.add(new LocationOption("umroi_cp", "Umroi Checkpoint"));
        list.add(new LocationOption("bhagam_river", "Bhagam River"));
        return list;
    }

    // --- Make 'locations' available to the layout whenever /dashboard is rendered ---
    @ModelAttribute("locations")
    public List<LocationOption> locationsModel() {
        return demoLocations();
    }

    // --- Resolve the currently selected location from ?loc=... (default = first) ---
    @ModelAttribute("currentLocation")
    public LocationOption currentLocation(@RequestParam(name = "loc", required = false) String locId) {
        List<LocationOption> locs = demoLocations();
        if (locId == null || locId.isBlank()) {
            return locs.get(0);
        }
        return locs.stream().filter(l -> l.getId().equals(locId)).findFirst().orElse(locs.get(0));
    }

    // --- Routes ---

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "Environmental Dashboard");
        model.addAttribute("active", "dashboard");
        // keep any existing KPI/series you already add here
        return "dashboard";
    }

    @GetMapping("/water-quality")
    public String waterQuality(Model model) {
        model.addAttribute("pageTitle", "Water Quality - Turbidity");
        model.addAttribute("active", "water-quality");
        return "water-quality";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("pageTitle", "Reports");
        model.addAttribute("active", "reports");
        return "reports";
    }
}
