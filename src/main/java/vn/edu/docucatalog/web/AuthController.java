package vn.edu.docucatalog.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.docucatalog.domain.UserRole;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.UserAccountService;
import vn.edu.docucatalog.web.form.RegistrationForm;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final UserAccountService userAccountService;

    @Value("${app.seed-data:false}")
    private boolean seedDataEnabled;

    @GetMapping("/login")
    public String login(Authentication authentication, Model model) {
        model.addAttribute("showDemoAccounts", seedDataEnabled);
        return authentication != null && authentication.isAuthenticated() ? "redirect:/dashboard" : "auth/login";
    }

    @GetMapping("/register")
    public String registrationForm(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        addRegistrationOptions(model);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                userAccountService.register(form);
                redirect.addFlashAttribute("registeredName", form.getFullName().trim());
                return "redirect:/login?registered";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        form.setPassword(null);
        form.setConfirmPassword(null);
        addRegistrationOptions(model);
        return "auth/register";
    }

    private void addRegistrationOptions(Model model) {
        model.addAttribute("requestedRoles", List.of(UserRole.CATALOGER, UserRole.ACQUISITION));
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
