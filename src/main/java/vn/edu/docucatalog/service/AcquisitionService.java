package vn.edu.docucatalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.docucatalog.domain.AcquisitionItem;
import vn.edu.docucatalog.domain.AcquisitionRequest;
import vn.edu.docucatalog.domain.AcquisitionStatus;
import vn.edu.docucatalog.domain.CopyCondition;
import vn.edu.docucatalog.domain.CopyStatus;
import vn.edu.docucatalog.domain.ResourceCopy;
import vn.edu.docucatalog.domain.ResourceDocument;
import vn.edu.docucatalog.repository.AcquisitionRequestRepository;
import vn.edu.docucatalog.repository.ResourceCopyRepository;
import vn.edu.docucatalog.repository.ResourceDocumentRepository;
import vn.edu.docucatalog.repository.SupplierRepository;
import vn.edu.docucatalog.web.form.AcquisitionForm;
import vn.edu.docucatalog.web.form.AcquisitionItemForm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcquisitionService {
    private final AcquisitionRequestRepository requestRepository;
    private final ResourceDocumentRepository documentRepository;
    private final ResourceCopyRepository copyRepository;
    private final SupplierRepository supplierRepository;

    public Page<AcquisitionRequest> search(String keyword, AcquisitionStatus status, int page, int size) {
        int safeSize = Math.min(Math.max(size, 5), 50);
        return requestRepository.search(keyword == null ? "" : keyword.trim(), status,
                PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    public AcquisitionRequest get(Long id) {
        return requestRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đề xuất bổ sung"));
    }

    public AcquisitionForm toForm(AcquisitionRequest request) {
        AcquisitionForm form = new AcquisitionForm();
        form.getItems().clear();
        form.setId(request.getId());
        form.setTitle(request.getTitle());
        form.setSupplierId(request.getSupplier() == null ? null : request.getSupplier().getId());
        form.setExpectedDate(request.getExpectedDate());
        form.setNote(request.getNote());
        request.getItems().forEach(item -> {
            AcquisitionItemForm itemForm = new AcquisitionItemForm();
            itemForm.setDocumentId(item.getDocument().getId());
            itemForm.setQuantity(item.getQuantity());
            itemForm.setUnitPrice(item.getUnitPrice());
            itemForm.setNote(item.getNote());
            form.getItems().add(itemForm);
        });
        return form;
    }

    @Transactional
    public AcquisitionRequest create(AcquisitionForm form, String requesterName) {
        AcquisitionRequest request = new AcquisitionRequest();
        request.setRequestCode(generateRequestCode());
        request.setRequesterName(requesterName);
        request.setRequestDate(LocalDate.now());
        mapForm(form, request);
        return requestRepository.save(request);
    }

    @Transactional
    public AcquisitionRequest update(AcquisitionForm form) {
        AcquisitionRequest request = get(form.getId());
        requireStatus(request, AcquisitionStatus.DRAFT, "Chỉ có thể sửa đề xuất ở trạng thái bản nháp");
        mapForm(form, request);
        return requestRepository.save(request);
    }

    @Transactional
    public void submit(Long id) {
        AcquisitionRequest request = get(id);
        requireStatus(request, AcquisitionStatus.DRAFT, "Chỉ bản nháp mới có thể gửi duyệt");
        if (request.getItems().isEmpty()) throw new BusinessException("Đề xuất phải có ít nhất một tài liệu");
        request.setStatus(AcquisitionStatus.PENDING);
    }

    @Transactional
    public void approve(Long id, String approver) {
        AcquisitionRequest request = get(id);
        requireStatus(request, AcquisitionStatus.PENDING, "Chỉ đề xuất đang chờ mới có thể phê duyệt");
        request.setStatus(AcquisitionStatus.APPROVED);
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
    }

    @Transactional
    public void reject(Long id, String approver, String reason) {
        AcquisitionRequest request = get(id);
        requireStatus(request, AcquisitionStatus.PENDING, "Chỉ đề xuất đang chờ mới có thể từ chối");
        request.setStatus(AcquisitionStatus.REJECTED);
        request.setApprovedBy(approver);
        request.setApprovedAt(LocalDateTime.now());
        if (reason != null && !reason.isBlank()) {
            String rejection = "Lý do từ chối: " + reason.trim();
            request.setNote(request.getNote() == null ? rejection : request.getNote() + "\n" + rejection);
        }
    }

    @Transactional
    public int complete(Long id) {
        AcquisitionRequest request = get(id);
        requireStatus(request, AcquisitionStatus.APPROVED, "Chỉ đề xuất đã duyệt mới có thể hoàn tất");
        int created = 0;
        for (AcquisitionItem item : request.getItems()) {
            ResourceDocument document = item.getDocument();
            long next = copyRepository.countByDocumentId(document.getId()) + 1;
            for (int i = 0; i < item.getQuantity(); i++) {
                String accession = nextAccession(document.getCatalogCode(), next++);
                ResourceCopy copy = new ResourceCopy();
                copy.setDocument(document);
                copy.setAccessionNumber(accession);
                copy.setAcquiredDate(LocalDate.now());
                copy.setPrice(item.getUnitPrice());
                copy.setLocation("Kho bổ sung");
                copy.setCondition(CopyCondition.NEW);
                copy.setStatus(CopyStatus.AVAILABLE);
                copy.setNote("Tạo từ đề xuất " + request.getRequestCode());
                copyRepository.save(copy);
                created++;
            }
        }
        request.setStatus(AcquisitionStatus.COMPLETED);
        request.setCompletedAt(LocalDateTime.now());
        return created;
    }

    @Transactional
    public void delete(Long id) {
        AcquisitionRequest request = get(id);
        if (request.getStatus() != AcquisitionStatus.DRAFT && request.getStatus() != AcquisitionStatus.REJECTED) {
            throw new BusinessException("Chỉ có thể xóa đề xuất nháp hoặc đã bị từ chối");
        }
        requestRepository.delete(request);
    }

    private void mapForm(AcquisitionForm form, AcquisitionRequest request) {
        if (form.getItems() == null || form.getItems().isEmpty()) {
            throw new BusinessException("Đề xuất phải có ít nhất một tài liệu");
        }
        Set<Long> documentIds = new HashSet<>();
        for (AcquisitionItemForm item : form.getItems()) {
            if (item.getDocumentId() == null) throw new BusinessException("Vui lòng chọn tài liệu cho mọi dòng");
            if (!documentIds.add(item.getDocumentId())) throw new BusinessException("Mỗi tài liệu chỉ nên xuất hiện một lần trong đề xuất");
        }
        request.setTitle(form.getTitle().trim());
        request.setExpectedDate(form.getExpectedDate());
        request.setNote(trimToNull(form.getNote()));
        request.setSupplier(form.getSupplierId() == null ? null : supplierRepository.findById(form.getSupplierId())
                .orElseThrow(() -> new BusinessException("Nhà cung cấp không hợp lệ")));
        request.clearItems();
        for (AcquisitionItemForm itemForm : form.getItems()) {
            ResourceDocument document = documentRepository.findById(itemForm.getDocumentId())
                    .orElseThrow(() -> new BusinessException("Tài liệu trong đề xuất không hợp lệ"));
            AcquisitionItem item = new AcquisitionItem();
            item.setDocument(document);
            item.setQuantity(itemForm.getQuantity());
            item.setUnitPrice(itemForm.getUnitPrice());
            item.setNote(trimToNull(itemForm.getNote()));
            request.addItem(item);
        }
    }

    private String generateRequestCode() {
        return "BS-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
    }

    private String nextAccession(String catalogCode, long sequence) {
        String candidate;
        do {
            candidate = catalogCode + "-" + String.format("%03d", sequence++);
        } while (copyRepository.existsByAccessionNumberIgnoreCase(candidate));
        return candidate;
    }

    private void requireStatus(AcquisitionRequest request, AcquisitionStatus expected, String message) {
        if (request.getStatus() != expected) throw new BusinessException(message);
    }

    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
