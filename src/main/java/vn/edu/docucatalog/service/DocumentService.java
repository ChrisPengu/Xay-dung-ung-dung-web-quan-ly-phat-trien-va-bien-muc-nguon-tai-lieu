package vn.edu.docucatalog.service;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.docucatalog.domain.Author;
import vn.edu.docucatalog.domain.CatalogStatus;
import vn.edu.docucatalog.domain.CopyStatus;
import vn.edu.docucatalog.domain.ResourceCopy;
import vn.edu.docucatalog.domain.ResourceDocument;
import vn.edu.docucatalog.repository.AcquisitionItemRepository;
import vn.edu.docucatalog.repository.AuthorRepository;
import vn.edu.docucatalog.repository.CategoryRepository;
import vn.edu.docucatalog.repository.PublisherRepository;
import vn.edu.docucatalog.repository.ResourceCopyRepository;
import vn.edu.docucatalog.repository.ResourceDocumentRepository;
import vn.edu.docucatalog.web.form.CopyForm;
import vn.edu.docucatalog.web.form.DocumentForm;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentService {
    private final ResourceDocumentRepository documentRepository;
    private final ResourceCopyRepository copyRepository;
    private final CategoryRepository categoryRepository;
    private final PublisherRepository publisherRepository;
    private final AuthorRepository authorRepository;
    private final AcquisitionItemRepository acquisitionItemRepository;

    public Page<ResourceDocument> search(String keyword, Long categoryId, CatalogStatus status, int page, int size) {
        Specification<ResourceDocument> spec = Specification.unrestricted();
        if (keyword != null && !keyword.isBlank()) {
            String term = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> {
                query.distinct(true);
                var author = root.join("authors", JoinType.LEFT);
                var copyQuery = query.subquery(Long.class);
                var copy = copyQuery.from(ResourceCopy.class);
                copyQuery.select(copy.get("id")).where(
                        cb.equal(copy.get("document").get("id"), root.get("id")),
                        cb.like(cb.lower(copy.get("accessionNumber")), term));
                return cb.or(cb.like(cb.lower(root.get("title")), term),
                        cb.like(cb.lower(root.get("catalogCode")), term),
                        cb.like(cb.lower(root.get("isbn")), term),
                        cb.like(cb.lower(author.get("name")), term),
                        cb.exists(copyQuery));
            });
        }
        if (categoryId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        int safeSize = Math.min(Math.max(size, 5), 50);
        return documentRepository.findAll(spec,
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "updatedAt")));
    }

    public ResourceDocument get(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài liệu"));
    }

    public List<ResourceDocument> allOrdered() { return documentRepository.findAllOrdered(); }
    public List<ResourceCopy> copies(Long documentId) { return copyRepository.findByDocumentIdOrderByAccessionNumberAsc(documentId); }

    public ResourceCopy getCopy(Long documentId, Long copyId) {
        ResourceCopy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bản ấn phẩm"));
        if (!copy.getDocument().getId().equals(documentId)) {
            throw new BusinessException("Bản ấn phẩm không thuộc tài liệu này");
        }
        return copy;
    }

    public DocumentForm toForm(ResourceDocument document) {
        DocumentForm form = new DocumentForm();
        form.setId(document.getId());
        form.setCatalogCode(document.getCatalogCode());
        form.setTitle(document.getTitle());
        form.setSubtitle(document.getSubtitle());
        form.setIsbn(document.getIsbn());
        form.setLanguage(document.getLanguage());
        form.setPublicationYear(document.getPublicationYear());
        form.setEdition(document.getEdition());
        form.setClassificationNumber(document.getClassificationNumber());
        form.setCallNumber(document.getCallNumber());
        form.setKeywords(document.getKeywords());
        form.setSummary(document.getSummary());
        form.setPhysicalDescription(document.getPhysicalDescription());
        form.setStatus(document.getStatus());
        form.setCategoryId(document.getCategory().getId());
        form.setPublisherId(document.getPublisher() == null ? null : document.getPublisher().getId());
        form.setAuthorIds(document.getAuthors().stream().map(Author::getId)
                .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new)));
        return form;
    }

    public DocumentForm duplicateForm(Long documentId) {
        DocumentForm form = toForm(get(documentId));
        form.setId(null);
        form.setCatalogCode(nextCopyCatalogCode(form.getCatalogCode()));
        form.setIsbn(null);
        form.setStatus(CatalogStatus.DRAFT);
        return form;
    }

    public CopyForm toCopyForm(ResourceCopy copy) {
        CopyForm form = new CopyForm();
        form.setId(copy.getId());
        form.setAccessionNumber(copy.getAccessionNumber());
        form.setAcquiredDate(copy.getAcquiredDate());
        form.setPrice(copy.getPrice());
        form.setLocation(copy.getLocation());
        form.setCondition(copy.getCondition());
        form.setStatus(copy.getStatus());
        form.setNote(copy.getNote());
        return form;
    }

    @Transactional
    public ResourceDocument save(DocumentForm form) {
        validateUnique(form);
        ResourceDocument target = form.getId() == null ? new ResourceDocument() : get(form.getId());
        target.setCatalogCode(form.getCatalogCode().trim().toUpperCase(Locale.ROOT));
        target.setTitle(form.getTitle().trim());
        target.setSubtitle(trimToNull(form.getSubtitle()));
        target.setIsbn(normalizeIsbn(form.getIsbn()));
        target.setLanguage(form.getLanguage().trim());
        target.setPublicationYear(form.getPublicationYear());
        target.setEdition(trimToNull(form.getEdition()));
        target.setClassificationNumber(trimToNull(form.getClassificationNumber()));
        target.setCallNumber(trimToNull(form.getCallNumber()));
        target.setKeywords(trimToNull(form.getKeywords()));
        target.setSummary(trimToNull(form.getSummary()));
        target.setPhysicalDescription(trimToNull(form.getPhysicalDescription()));
        target.setStatus(form.getStatus());
        target.setCategory(categoryRepository.findById(form.getCategoryId())
                .orElseThrow(() -> new BusinessException("Thể loại không hợp lệ")));
        target.setPublisher(form.getPublisherId() == null ? null : publisherRepository.findById(form.getPublisherId())
                .orElseThrow(() -> new BusinessException("Nhà xuất bản không hợp lệ")));
        Set<Long> authorIds = form.getAuthorIds() == null ? Set.of() : form.getAuthorIds();
        List<Author> authors = authorRepository.findAllById(authorIds);
        if (authors.size() != new HashSet<>(authorIds).size()) throw new BusinessException("Danh sách tác giả không hợp lệ");
        target.setAuthors(new java.util.LinkedHashSet<>(authors));
        return documentRepository.save(target);
    }

    @Transactional
    public void delete(Long id) {
        if (copyRepository.countByDocumentId(id) > 0) throw new BusinessException("Không thể xóa tài liệu đã có bản ấn phẩm");
        if (acquisitionItemRepository.existsByDocumentId(id)) throw new BusinessException("Không thể xóa tài liệu đã nằm trong đề xuất bổ sung");
        documentRepository.delete(get(id));
    }

    @Transactional
    public ResourceCopy addCopy(Long documentId, CopyForm form) {
        validateCopyUnique(null, form.getAccessionNumber());
        ResourceCopy copy = new ResourceCopy();
        copy.setDocument(get(documentId));
        applyCopyForm(copy, form);
        return copyRepository.save(copy);
    }

    @Transactional
    public ResourceCopy updateCopy(Long documentId, Long copyId, CopyForm form) {
        ResourceCopy copy = getCopy(documentId, copyId);
        validateCopyUnique(copyId, form.getAccessionNumber());
        applyCopyForm(copy, form);
        return copy;
    }

    private void applyCopyForm(ResourceCopy copy, CopyForm form) {
        copy.setAccessionNumber(form.getAccessionNumber().trim().toUpperCase(Locale.ROOT));
        copy.setAcquiredDate(form.getAcquiredDate());
        copy.setPrice(form.getPrice());
        copy.setLocation(form.getLocation().trim());
        copy.setCondition(form.getCondition());
        copy.setStatus(form.getStatus());
        copy.setNote(trimToNull(form.getNote()));
    }

    @Transactional
    public void updateCopyStatus(Long documentId, Long copyId, CopyStatus status) {
        ResourceCopy copy = getCopy(documentId, copyId);
        copy.setStatus(status);
    }

    @Transactional
    public void deleteCopy(Long documentId, Long copyId) {
        ResourceCopy copy = getCopy(documentId, copyId);
        if (copy.getStatus() == CopyStatus.CHECKED_OUT) {
            throw new BusinessException("Không thể xóa bản ấn phẩm đang được mượn");
        }
        copyRepository.delete(copy);
    }

    private void validateCopyUnique(Long id, String accessionNumber) {
        String normalized = accessionNumber.trim();
        boolean exists = id == null
                ? copyRepository.existsByAccessionNumberIgnoreCase(normalized)
                : copyRepository.existsByAccessionNumberIgnoreCaseAndIdNot(normalized, id);
        if (exists) throw new BusinessException("Số đăng ký cá biệt đã tồn tại");
    }

    private String nextCopyCatalogCode(String originalCode) {
        for (int sequence = 1; sequence <= 999; sequence++) {
            String suffix = sequence == 1 ? "-COPY" : "-COPY" + sequence;
            String prefix = originalCode.substring(0, Math.min(originalCode.length(), 40 - suffix.length()));
            String candidate = prefix + suffix;
            if (!documentRepository.existsByCatalogCodeIgnoreCase(candidate)) return candidate;
        }
        throw new BusinessException("Không thể sinh mã cho biểu ghi sao chép");
    }

    private void validateUnique(DocumentForm form) {
        Long id = form.getId();
        String code = form.getCatalogCode().trim();
        boolean codeExists = id == null ? documentRepository.existsByCatalogCodeIgnoreCase(code)
                : documentRepository.existsByCatalogCodeIgnoreCaseAndIdNot(code, id);
        if (codeExists) throw new BusinessException("Mã biên mục đã tồn tại");
        String isbn = normalizeIsbn(form.getIsbn());
        if (isbn != null) {
            boolean isbnExists = id == null ? documentRepository.existsByIsbn(isbn)
                    : documentRepository.existsByIsbnAndIdNot(isbn, id);
            if (isbnExists) throw new BusinessException("ISBN đã tồn tại");
        }
    }

    private String normalizeIsbn(String value) {
        return value == null || value.isBlank() ? null : value.replace("-", "").replace(" ", "").trim();
    }

    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
