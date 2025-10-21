package com.riverwatch.app.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("active", "dashboard");
        model.addAttribute("pageTitle", "Environmental Dashboard");
        model.addAttribute("riverName", "Wahumkhrah River");
        model.addAttribute("activeCameras", 12);
        model.addAttribute("detectionsToday", 247);
        model.addAttribute("modelVersion", "v2.1.4");
        model.addAttribute("avgConfidence", "87.2%");
        return "dashboard";
    }

    @GetMapping("/water-quality")
    public String waterQuality(Model model) {
        model.addAttribute("active", "water-quality");
        model.addAttribute("pageTitle", "Water Quality - Turbidity");
        model.addAttribute("riverName", "Wahumkhrah River");
        return "water-quality";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("active", "reports");
        model.addAttribute("pageTitle", "Reports");
        return "reports";
    }
}