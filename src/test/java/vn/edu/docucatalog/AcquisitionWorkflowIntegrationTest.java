package vn.edu.docucatalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.docucatalog.domain.AcquisitionRequest;
import vn.edu.docucatalog.domain.AcquisitionStatus;
import vn.edu.docucatalog.domain.CatalogStatus;
import vn.edu.docucatalog.domain.Category;
import vn.edu.docucatalog.domain.ResourceDocument;
import vn.edu.docucatalog.domain.Supplier;
import vn.edu.docucatalog.repository.CategoryRepository;
import vn.edu.docucatalog.repository.ResourceCopyRepository;
import vn.edu.docucatalog.repository.ResourceDocumentRepository;
import vn.edu.docucatalog.repository.SupplierRepository;
import vn.edu.docucatalog.service.AcquisitionService;
import vn.edu.docucatalog.web.form.AcquisitionForm;
import vn.edu.docucatalog.web.form.AcquisitionItemForm;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AcquisitionWorkflowIntegrationTest {
    @Autowired private AcquisitionService acquisitionService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ResourceDocumentRepository documentRepository;
    @Autowired private ResourceCopyRepository copyRepository;

    @Test
    void fullWorkflowCreatesExpectedInventoryCopies() {
        Category category = new Category();
        category.setCode("TEST");
        category.setName("Thể loại kiểm thử");
        category = categoryRepository.save(category);

        Supplier supplier = new Supplier();
        supplier.setName("Nhà cung cấp kiểm thử");
        supplier = supplierRepository.save(supplier);

        ResourceDocument document = new ResourceDocument();
        document.setCatalogCode("TEST-DOC-001");
        document.setTitle("Tài liệu kiểm thử workflow");
        document.setCategory(category);
        document.setStatus(CatalogStatus.PUBLISHED);
        document = documentRepository.save(document);

        AcquisitionForm form = new AcquisitionForm();
        form.setTitle("Đề xuất tích hợp");
        form.setSupplierId(supplier.getId());
        form.setExpectedDate(LocalDate.now().plusDays(7));
        form.getItems().clear();
        AcquisitionItemForm item = new AcquisitionItemForm();
        item.setDocumentId(document.getId());
        item.setQuantity(3);
        item.setUnitPrice(new BigDecimal("125000"));
        form.getItems().add(item);

        AcquisitionRequest request = acquisitionService.create(form, "Người kiểm thử");
        acquisitionService.submit(request.getId());
        acquisitionService.approve(request.getId(), "Quản trị kiểm thử");
        int createdCopies = acquisitionService.complete(request.getId());

        AcquisitionRequest completed = acquisitionService.get(request.getId());
        assertThat(createdCopies).isEqualTo(3);
        assertThat(completed.getStatus()).isEqualTo(AcquisitionStatus.COMPLETED);
        assertThat(completed.getCompletedAt()).isNotNull();
        assertThat(copyRepository.findByDocumentIdOrderByAccessionNumberAsc(document.getId()))
                .extracting("accessionNumber")
                .containsExactly("TEST-DOC-001-001", "TEST-DOC-001-002", "TEST-DOC-001-003");
    }
}
