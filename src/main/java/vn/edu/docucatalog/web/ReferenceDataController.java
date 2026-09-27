package vn.edu.docucatalog.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.docucatalog.domain.Author;
import vn.edu.docucatalog.domain.Category;
import vn.edu.docucatalog.domain.Publisher;
import vn.edu.docucatalog.domain.Supplier;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.ReferenceDataService;

@Controller
@RequestMapping("/references")
@RequiredArgsConstructor
public class ReferenceDataController {
    private final ReferenceDataService service;

    @GetMapping
    public String index(@RequestParam(defaultValue = "categories") String tab,
                        @RequestParam(required = false) Long editCategory,
                        @RequestParam(required = false) Long editAuthor,
                        @RequestParam(required = false) Long editPublisher,
                        @RequestParam(required = false) Long editSupplier,
                        Model model) {
        model.addAttribute("categoryForm", editCategory == null ? new Category() : service.category(editCategory));
        model.addAttribute("authorForm", editAuthor == null ? new Author() : service.author(editAuthor));
        model.addAttribute("publisherForm", editPublisher == null ? new Publisher() : service.publisher(editPublisher));
        model.addAttribute("supplierForm", editSupplier == null ? new Supplier() : service.supplier(editSupplier));
        return render(model, tab);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String saveCategory(@Valid @ModelAttribute("categoryForm") Category form, BindingResult result,
                               Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.saveCategory(form);
                redirect.addFlashAttribute("success", "Đã lưu thể loại.");
                return "redirect:/references?tab=categories";
            } catch (BusinessException ex) { result.reject("business", ex.getMessage()); }
        }
        addEmptyForms(model, "categoryForm");
        return render(model, "categories");
    }

    @PostMapping("/authors")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String saveAuthor(@Valid @ModelAttribute("authorForm") Author form, BindingResult result,
                             Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.saveAuthor(form);
                redirect.addFlashAttribute("success", "Đã lưu tác giả.");
                return "redirect:/references?tab=authors";
            } catch (BusinessException ex) { result.reject("business", ex.getMessage()); }
        }
        addEmptyForms(model, "authorForm");
        return render(model, "authors");
    }

    @PostMapping("/publishers")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String savePublisher(@Valid @ModelAttribute("publisherForm") Publisher form, BindingResult result,
                                Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.savePublisher(form);
                redirect.addFlashAttribute("success", "Đã lưu nhà xuất bản.");
                return "redirect:/references?tab=publishers";
            } catch (BusinessException ex) { result.reject("business", ex.getMessage()); }
        }
        addEmptyForms(model, "publisherForm");
        return render(model, "publishers");
    }

    @PostMapping("/suppliers")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String saveSupplier(@Valid @ModelAttribute("supplierForm") Supplier form, BindingResult result,
                               Model model, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                service.saveSupplier(form);
                redirect.addFlashAttribute("success", "Đã lưu nhà cung cấp.");
                return "redirect:/references?tab=suppliers";
            } catch (BusinessException ex) { result.reject("business", ex.getMessage()); }
        }
        addEmptyForms(model, "supplierForm");
        return render(model, "suppliers");
    }

    @PostMapping("/{type}/{id}/delete")
    @PreAuthorize("hasAnyRole('ADMIN','CATALOGER')")
    public String delete(@PathVariable String type, @PathVariable Long id, RedirectAttributes redirect) {
        String tab = switch (type) {
            case "categories" -> { service.deleteCategory(id); yield "categories"; }
            case "authors" -> { service.deleteAuthor(id); yield "authors"; }
            case "publishers" -> { service.deletePublisher(id); yield "publishers"; }
            case "suppliers" -> { service.deleteSupplier(id); yield "suppliers"; }
            default -> throw new BusinessException("Loại danh mục không hợp lệ");
        };
        redirect.addFlashAttribute("success", "Đã xóa dữ liệu danh mục.");
        return "redirect:/references?tab=" + tab;
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, RedirectAttributes redirect) {
        redirect.addFlashAttribute("error", ex.getMessage());
        return "redirect:/references";
    }

    private String render(Model model, String tab) {
        model.addAttribute("activeTab", allowedTab(tab));
        model.addAttribute("categories", service.categories());
        model.addAttribute("authors", service.authors());
        model.addAttribute("publishers", service.publishers());
        model.addAttribute("suppliers", service.suppliers());
        return "references/index";
    }

    private void addEmptyForms(Model model, String preserved) {
        if (!"categoryForm".equals(preserved)) model.addAttribute("categoryForm", new Category());
        if (!"authorForm".equals(preserved)) model.addAttribute("authorForm", new Author());
        if (!"publisherForm".equals(preserved)) model.addAttribute("publisherForm", new Publisher());
        if (!"supplierForm".equals(preserved)) model.addAttribute("supplierForm", new Supplier());
    }

    private String allowedTab(String tab) {
        return switch (tab) {
            case "authors", "publishers", "suppliers" -> tab;
            default -> "categories";
        };
    }
}
