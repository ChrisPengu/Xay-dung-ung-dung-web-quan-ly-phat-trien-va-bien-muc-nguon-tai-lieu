package vn.edu.docucatalog.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {
    @Value("${app.seed-data:false}")
    private boolean seedDataEnabled;

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        model.addAttribute("showDemoAccounts", seedDataEnabled);
        return authentication != null && authentication.isAuthenticated() ? "redirect:/dashboard" : "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
}
