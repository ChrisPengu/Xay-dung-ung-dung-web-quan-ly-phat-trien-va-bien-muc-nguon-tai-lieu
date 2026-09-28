package vn.edu.docucatalog.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.docucatalog.domain.UserAccount;
import vn.edu.docucatalog.domain.UserRole;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.UserAccountService;
import vn.edu.docucatalog.web.form.UserAccountForm;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserAccountController {
    private final UserAccountService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", service.findAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        return renderForm(new UserAccountForm(), model, false);
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserAccountForm form,
                         BindingResult result, Authentication authentication,
                         Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                UserAccount saved = service.save(form, authentication.getName());
                redirect.addFlashAttribute("success", "Đã tạo tài khoản “" + saved.getUsername() + "”.");
                return "redirect:/users";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderForm(form, model, false);
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        return renderForm(service.toForm(service.get(id)), model, true);
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("userForm") UserAccountForm form,
                         BindingResult result, Authentication authentication,
                         Model model, RedirectAttributes redirect) {
        form.setId(id);
        if (!result.hasErrors()) {
            try {
                UserAccount saved = service.save(form, authentication.getName());
                redirect.addFlashAttribute("success", "Đã cập nhật tài khoản “" + saved.getUsername() + "”.");
                return "redirect:/users";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderForm(form, model, true);
    }

    @PostMapping("/{id}/active")
    public String setActive(@PathVariable Long id, @RequestParam boolean active,
                            Authentication authentication, RedirectAttributes redirect) {
        try {
            boolean pending = !service.get(id).isApproved();
            service.setActive(id, active, authentication.getName());
            redirect.addFlashAttribute("success", active
                    ? (pending ? "Đã duyệt tài khoản. Người dùng có thể đăng nhập ngay." : "Tài khoản đã được mở lại.")
                    : "Tài khoản đã được tạm khóa.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/users";
    }

    private String renderForm(UserAccountForm form, Model model, boolean editing) {
        model.addAttribute("userForm", form);
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("editing", editing);
        model.addAttribute("pendingAccount", editing && form.getId() != null && !service.get(form.getId()).isApproved());
        return "users/form";
    }
}
