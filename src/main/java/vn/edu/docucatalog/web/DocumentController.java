package vn.edu.docucatalog.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
import vn.edu.docucatalog.domain.CatalogStatus;
import vn.edu.docucatalog.domain.CopyCondition;
import vn.edu.docucatalog.domain.CopyStatus;
import vn.edu.docucatalog.domain.ResourceDocument;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.DocumentService;
import vn.edu.docucatalog.service.ReferenceDataService;
import vn.edu.docucatalog.web.form.CopyForm;
import vn.edu.docucatalog.web.form.DocumentForm;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentService documentService;
    private final ReferenceDataService referenceDataService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false) CatalogStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        model.addAttribute("documents", documentService.search(keyword, categoryId, status, page, size));
        model.addAttribute("categories", referenceDataService.categories());
        model.addAttribute("statuses", CatalogStatus.values());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        return "documents/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String createForm(Model model) {
        return renderForm(new DocumentForm(), model, false, false);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String create(@Valid @ModelAttribute("documentForm") DocumentForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                ResourceDocument saved = documentService.save(form);
                redirect.addFlashAttribute("success", "Đã tạo biểu ghi “" + saved.getTitle() + "”.");
                return "redirect:/documents/" + saved.getId();
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderForm(form, model, false, false);
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        return renderDetail(id, new CopyForm(), model);
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String editForm(@PathVariable Long id, Model model) {
        return renderForm(documentService.toForm(documentService.get(id)), model, true, false);
    }

    @GetMapping("/{id}/duplicate")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String duplicateForm(@PathVariable Long id, Model model) {
        return renderForm(documentService.duplicateForm(id), model, false, true);
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("documentForm") DocumentForm form,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        form.setId(id);
        if (!result.hasErrors()) {
            try {
                ResourceDocument saved = documentService.save(form);
                redirect.addFlashAttribute("success", "Đã cập nhật biểu ghi “" + saved.getTitle() + "”.");
                return "redirect:/documents/" + saved.getId();
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderForm(form, model, true, false);
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            documentService.delete(id);
            redirect.addFlashAttribute("success", "Đã xóa biểu ghi tài liệu.");
            return "redirect:/documents";
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
            return "redirect:/documents/" + id;
        }
    }

    @PostMapping("/{id}/copies")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String addCopy(@PathVariable Long id,
                          @Valid @ModelAttribute("copyForm") CopyForm form,
                          BindingResult result, Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                documentService.addCopy(id, form);
                redirect.addFlashAttribute("success", "Đã thêm bản ấn phẩm mới.");
                return "redirect:/documents/" + id + "#copies";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderDetail(id, form, model);
    }

    @PostMapping("/{documentId}/copies/{copyId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String updateCopyStatus(@PathVariable Long documentId, @PathVariable Long copyId,
                                   @RequestParam CopyStatus status, RedirectAttributes redirect) {
        try {
            documentService.updateCopyStatus(documentId, copyId, status);
            redirect.addFlashAttribute("success", "Đã cập nhật trạng thái bản ấn phẩm.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/documents/" + documentId + "#copies";
    }

    @GetMapping("/{documentId}/copies/{copyId}/edit")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String editCopyForm(@PathVariable Long documentId, @PathVariable Long copyId, Model model) {
        return renderCopyForm(documentId, documentService.toCopyForm(documentService.getCopy(documentId, copyId)), model);
    }

    @PostMapping("/{documentId}/copies/{copyId}")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String updateCopy(@PathVariable Long documentId, @PathVariable Long copyId,
                             @Valid @ModelAttribute("copyForm") CopyForm form,
                             BindingResult result, Model model, RedirectAttributes redirect) {
        form.setId(copyId);
        if (!result.hasErrors()) {
            try {
                documentService.updateCopy(documentId, copyId, form);
                redirect.addFlashAttribute("success", "Đã cập nhật đầy đủ thông tin bản ấn phẩm.");
                return "redirect:/documents/" + documentId + "#copies";
            } catch (BusinessException ex) {
                result.reject("business", ex.getMessage());
            }
        }
        return renderCopyForm(documentId, form, model);
    }

    @PostMapping("/{documentId}/copies/{copyId}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String deleteCopy(@PathVariable Long documentId, @PathVariable Long copyId, RedirectAttributes redirect) {
        try {
            documentService.deleteCopy(documentId, copyId);
            redirect.addFlashAttribute("success", "Đã xóa bản ấn phẩm.");
        } catch (BusinessException ex) {
            redirect.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/documents/" + documentId + "#copies";
    }

    private String renderForm(DocumentForm form, Model model, boolean editing, boolean copying) {
        model.addAttribute("documentForm", form);
        model.addAttribute("categories", referenceDataService.categories());
        model.addAttribute("publishers", referenceDataService.publishers());
        model.addAttribute("authors", referenceDataService.authors());
        model.addAttribute("statuses", CatalogStatus.values());
        model.addAttribute("editing", editing);
        model.addAttribute("copying", copying);
        return "documents/form";
    }

    private String renderCopyForm(Long documentId, CopyForm copyForm, Model model) {
        model.addAttribute("document", documentService.get(documentId));
        model.addAttribute("copyForm", copyForm);
        model.addAttribute("copyStatuses", CopyStatus.values());
        model.addAttribute("copyConditions", CopyCondition.values());
        return "documents/copy-form";
    }

    private String renderDetail(Long id, CopyForm copyForm, Model model) {
        model.addAttribute("document", documentService.get(id));
        model.addAttribute("copies", documentService.copies(id));
        model.addAttribute("copyForm", copyForm);
        model.addAttribute("copyStatuses", CopyStatus.values());
        model.addAttribute("copyConditions", CopyCondition.values());
        return "documents/detail";
    }
}
