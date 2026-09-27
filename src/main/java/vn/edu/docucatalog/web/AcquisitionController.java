package vn.edu.docucatalog.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.docucatalog.domain.AcquisitionRequest;
import vn.edu.docucatalog.domain.AcquisitionStatus;
import vn.edu.docucatalog.security.CustomUserDetails;
import vn.edu.docucatalog.service.AcquisitionService;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.DocumentService;
import vn.edu.docucatalog.service.ReferenceDataService;
import vn.edu.docucatalog.web.form.AcquisitionForm;

@Controller
@RequestMapping("/acquisitions")
@RequiredArgsConstructor
public class AcquisitionController {
    private final AcquisitionService acquisitionService;
    private final DocumentService documentService;
    private final ReferenceDataService referenceDataService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) AcquisitionStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("requests", acquisitionService.search(keyword, status, page, size));
        model.addAttribute("statuses", AcquisitionStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        return "acquisitions/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','ACQUISITION')")
    public String createForm(Model model) {
        return renderForm(new AcquisitionForm(), model, false);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ACQUISITION')")
    public String create(@Valid @ModelAttribute("acquisitionForm") AcquisitionForm form,
                         BindingResult result, Model model, Authentication authentication,
                         RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                AcquisitionRequest saved = acquisitionService.create(form, displayName(authentication));
                redirect.addFlashAttribute("success", "Đã tạo đề xuất " + saved.getRequestCode() + ".");
                return "redirect:/acquisitions/" + saved.getId();
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderForm(form, model, false);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("request", acquisitionService.get(id));
        return "acquisitions/detail";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','ACQUISITION')")
    public String editForm(@PathVariable Long id, Model model) {
        AcquisitionRequest request = acquisitionService.get(id);
        if (request.getStatus() != AcquisitionStatus.DRAFT) throw new BusinessException("Chỉ có thể sửa đề xuất ở trạng thái bản nháp");
        return renderForm(acquisitionService.toForm(request), model, true);
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ACQUISITION')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("acquisitionForm") AcquisitionForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        form.setId(id);
        if (!result.hasErrors()) {
            try {
                acquisitionService.update(form);
                redirect.addFlashAttribute("success", "Đã cập nhật đề xuất.");
                return "redirect:/acquisitions/" + id;
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderForm(form, model, true);
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('ADMIN','ACQUISITION')")
    public String submit(@PathVariable Long id, RedirectAttributes redirect) {
        return action(id, redirect, () -> acquisitionService.submit(id), "Đã gửi đề xuất để phê duyệt.");
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approve(@PathVariable Long id, Authentication authentication, RedirectAttributes redirect) {
        return action(id, redirect, () -> acquisitionService.approve(id, displayName(authentication)), "Đã phê duyệt đề xuất.");
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String reason,
                         Authentication authentication, RedirectAttributes redirect) {
        return action(id, redirect, () -> acquisitionService.reject(id, displayName(authentication), reason), "Đã từ chối đề xuất.");
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','ACQUISITION')")
    public String complete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            int copies = acquisitionService.complete(id);
            redirect.addFlashAttribute("success", "Đã hoàn tất và tạo " + copies + " bản ấn phẩm.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/acquisitions/" + id;
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','ACQUISITION')")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            acquisitionService.delete(id);
            redirect.addFlashAttribute("success", "Đã xóa đề xuất.");
            return "redirect:/acquisitions";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/acquisitions/" + id;
        }
    }

    private String action(Long id, RedirectAttributes redirect, Runnable operation, String message) {
        try {
            operation.run();
            redirect.addFlashAttribute("success", message);
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/acquisitions/" + id;
    }

    private String renderForm(AcquisitionForm form, Model model, boolean editing) {
        model.addAttribute("acquisitionForm", form);
        model.addAttribute("documents", documentService.allOrdered());
        model.addAttribute("suppliers", referenceDataService.suppliers());
        model.addAttribute("editing", editing);
        return "acquisitions/form";
    }

    private String displayName(Authentication authentication) {
        return authentication.getPrincipal() instanceof CustomUserDetails details ? details.getFullName() : authentication.getName();
    }
}
