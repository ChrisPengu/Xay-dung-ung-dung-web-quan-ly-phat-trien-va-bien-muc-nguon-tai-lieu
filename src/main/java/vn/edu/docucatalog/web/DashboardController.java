package vn.edu.docucatalog.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.docucatalog.service.DashboardService;

@Controller
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("data", dashboardService.getData());
        return "dashboard";
    }
}
