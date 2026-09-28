package vn.edu.docucatalog.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.docucatalog.domain.UserAccount;
import vn.edu.docucatalog.security.CustomUserDetails;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.UserAccountService;
import vn.edu.docucatalog.web.form.PasswordChangeForm;
import vn.edu.docucatalog.web.form.UserProfileForm;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {
    private final UserAccountService service;

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        UserAccount account = currentAccount(authentication);
        model.addAttribute("account", account);
        model.addAttribute("profileForm", service.toProfileForm(account));
        addProfileOptions(account, model);
        return "account/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@Valid @ModelAttribute("profileForm") UserProfileForm form,
                                BindingResult result, Authentication authentication,
                                Model model, RedirectAttributes redirect) {
        UserAccount account = currentAccount(authentication);
        if (!result.hasErrors()) {
            try {
                UserAccount saved = service.updateProfile(account.getId(), form);
                refreshAuthentication(authentication, saved);
                redirect.addFlashAttribute("success", "Hồ sơ của bạn đã được cập nhật.");
                return "redirect:/account/profile";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        addProfileOptions(account, model);
        return "account/profile";
    }

    @PostMapping("/avatar")
    public String updateAvatar(@RequestParam("avatar") MultipartFile avatar,
                               Authentication authentication, RedirectAttributes redirect) {
        try {
            UserAccount account = currentAccount(authentication);
            UserAccount saved = service.updateAvatar(account.getId(), avatar);
            refreshAuthentication(authentication, saved);
            redirect.addFlashAttribute("success", "Ảnh đại diện mới đã được lưu.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/account/profile";
    }

    @GetMapping("/password")
    public String passwordForm(Authentication authentication, Model model) {
        model.addAttribute("passwordForm", new PasswordChangeForm());
        model.addAttribute("account", currentAccount(authentication));
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
        model.addAttribute("account", currentAccount(authentication));
        return "account/password";
    }

    private void addProfileOptions(UserAccount account, Model model) {
        model.addAttribute("account", account);
        model.addAttribute("profileThemes", UserAccountService.PROFILE_THEMES);
        model.addAttribute("profileCompletion", profileCompletion(account));
    }

    private UserAccount currentAccount(Authentication authentication) {
        CustomUserDetails details = (CustomUserDetails) authentication.getPrincipal();
        return service.get(details.getId());
    }

    private void refreshAuthentication(Authentication authentication, UserAccount account) {
        CustomUserDetails principal = new CustomUserDetails(account);
        UsernamePasswordAuthenticationToken refreshed = new UsernamePasswordAuthenticationToken(
                principal, authentication.getCredentials(), principal.getAuthorities());
        refreshed.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(refreshed);
    }

    private int profileCompletion(UserAccount account) {
        int completed = 1;
        if (account.getEmail() != null) completed++;
        if (account.getDepartment() != null) completed++;
        if (account.getPhone() != null) completed++;
        if (account.getBio() != null) completed++;
        return completed * 20;
    }
}
