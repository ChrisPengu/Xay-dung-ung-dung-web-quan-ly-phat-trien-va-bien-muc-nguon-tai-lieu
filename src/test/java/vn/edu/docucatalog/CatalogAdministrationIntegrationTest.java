package vn.edu.docucatalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.docucatalog.domain.CatalogStatus;
import vn.edu.docucatalog.domain.Category;
import vn.edu.docucatalog.domain.CopyCondition;
import vn.edu.docucatalog.domain.CopyStatus;
import vn.edu.docucatalog.domain.ResourceCopy;
import vn.edu.docucatalog.domain.ResourceDocument;
import vn.edu.docucatalog.domain.UserAccount;
import vn.edu.docucatalog.domain.UserRole;
import vn.edu.docucatalog.repository.CategoryRepository;
import vn.edu.docucatalog.repository.ResourceDocumentRepository;
import vn.edu.docucatalog.repository.UserAccountRepository;
import vn.edu.docucatalog.service.BusinessException;
import vn.edu.docucatalog.service.DocumentService;
import vn.edu.docucatalog.service.UserAccountService;
import vn.edu.docucatalog.web.form.CopyForm;
import vn.edu.docucatalog.web.form.DocumentForm;
import vn.edu.docucatalog.web.form.PasswordChangeForm;
import vn.edu.docucatalog.web.form.UserAccountForm;
import vn.edu.docucatalog.web.form.UserProfileForm;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CatalogAdministrationIntegrationTest {
    @Autowired private DocumentService documentService;
    @Autowired private UserAccountService userAccountService;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ResourceDocumentRepository documentRepository;
    @Autowired private UserAccountRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void catalogCrudSupportsCopyingInventoryEditingSearchAndSafeDeletion() {
        Category category = new Category();
        category.setCode("CAT-" + System.nanoTime());
        category.setName("Nghiệp vụ biên mục");
        category = categoryRepository.save(category);

        ResourceDocument document = new ResourceDocument();
        document.setCatalogCode("CATALOG-001");
        document.setTitle("Tài liệu nghiệp vụ thực tế");
        document.setCategory(category);
        document.setStatus(CatalogStatus.PUBLISHED);
        document = documentRepository.save(document);

        DocumentForm duplicate = documentService.duplicateForm(document.getId());
        assertThat(duplicate.getId()).isNull();
        assertThat(duplicate.getCatalogCode()).isEqualTo("CATALOG-001-COPY");
        assertThat(duplicate.getStatus()).isEqualTo(CatalogStatus.DRAFT);

        CopyForm createCopy = new CopyForm();
        createCopy.setAccessionNumber("DKCB-TEST-001");
        createCopy.setAcquiredDate(LocalDate.now());
        createCopy.setPrice(new BigDecimal("125000"));
        createCopy.setLocation("Kho A - Kệ 03");
        createCopy.setCondition(CopyCondition.NEW);
        createCopy.setStatus(CopyStatus.AVAILABLE);
        ResourceCopy copy = documentService.addCopy(document.getId(), createCopy);
        Long documentId = document.getId();
        Long copyId = copy.getId();

        CopyForm editCopy = documentService.toCopyForm(copy);
        editCopy.setLocation("Kho B - Kệ 08");
        editCopy.setCondition(CopyCondition.GOOD);
        documentService.updateCopy(documentId, copyId, editCopy);

        assertThat(documentService.search("dkcb-test-001", null, null, 0, 10).getContent())
                .extracting(ResourceDocument::getId).contains(documentId);
        assertThat(documentService.getCopy(documentId, copyId).getLocation())
                .isEqualTo("Kho B - Kệ 08");

        documentService.updateCopyStatus(documentId, copyId, CopyStatus.CHECKED_OUT);
        assertThatThrownBy(() -> documentService.deleteCopy(documentId, copyId))
                .isInstanceOf(BusinessException.class).hasMessageContaining("đang được mượn");
        assertThatThrownBy(() -> documentService.delete(documentId))
                .isInstanceOf(BusinessException.class).hasMessageContaining("đã có bản ấn phẩm");

        documentService.updateCopyStatus(documentId, copyId, CopyStatus.AVAILABLE);
        documentService.deleteCopy(documentId, copyId);
        documentService.delete(documentId);
        assertThat(documentRepository.findById(documentId)).isEmpty();
    }

    @Test
    void administratorsCanManageAccountsWithoutOverwritingBlankPasswords() {
        UserAccountForm create = new UserAccountForm();
        create.setUsername("catalog-tester");
        create.setFullName("Nhân viên biên mục kiểm thử");
        create.setEmail("catalog-tester@example.test");
        create.setRole(UserRole.CATALOGER);
        create.setActive(true);
        create.setPassword("initial-password");

        UserAccount account = userAccountService.save(create, "admin");
        assertThat(passwordEncoder.matches("initial-password", account.getPassword())).isTrue();
        String originalHash = account.getPassword();

        UserAccountForm edit = userAccountService.toForm(account);
        edit.setFullName("Biên mục viên đã cập nhật");
        edit.setPassword("");
        userAccountService.save(edit, "admin");
        assertThat(userRepository.findById(account.getId()).orElseThrow().getPassword()).isEqualTo(originalHash);

        UserProfileForm profile = userAccountService.toProfileForm(account);
        profile.setFullName("Biên mục viên hồ sơ mới");
        profile.setDepartment("Trung tâm học liệu");
        profile.setPhone("090 123 4567");
        profile.setBio("Phụ trách chuẩn hóa dữ liệu thư mục.");
        profile.setAvatarTheme("BLUE");
        userAccountService.updateProfile(account.getId(), profile);
        assertThat(account.getDepartment()).isEqualTo("Trung tâm học liệu");
        assertThat(account.getPhone()).isEqualTo("090 123 4567");
        assertThat(account.getAvatarTheme()).isEqualTo("BLUE");

        assertThatThrownBy(() -> userAccountService.setActive(account.getId(), false, "catalog-tester"))
                .isInstanceOf(BusinessException.class).hasMessageContaining("đang đăng nhập");

        PasswordChangeForm password = new PasswordChangeForm();
        password.setCurrentPassword("initial-password");
        password.setNewPassword("updated-password");
        password.setConfirmPassword("updated-password");
        userAccountService.changePassword(account.getId(), password);
        assertThat(passwordEncoder.matches("updated-password", account.getPassword())).isTrue();
    }
}
