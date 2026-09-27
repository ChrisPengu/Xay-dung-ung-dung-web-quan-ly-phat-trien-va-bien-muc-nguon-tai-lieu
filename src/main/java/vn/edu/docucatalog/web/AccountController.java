package vn.edu.docucatalog.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.docucatalog.security.CustomUserDetails;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.UserAccountService;
import vn.edu.docucatalog.web.form.PasswordChangeForm;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final UserAccountService service;

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("passwordForm", new PasswordChangeForm());
        return "account/password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordChangeForm form,
                                 BindingResult result, Authentication authentication,
                                 HttpServletRequest request, Model model) {
        if (!result.hasErrors()) {
            try {
                CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
                service.changePassword(details.getId(), form);
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                return "redirect:/login?passwordChanged";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        model.addAttribute("passwordForm", form);
        return "account/password";
    }
}
