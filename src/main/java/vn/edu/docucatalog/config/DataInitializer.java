package vn.edu.docucatalog.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.docucatalog.domain.*;
import vn.edu.docucatalog.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class DataInitializer implements CommandLineRunner {
    private final UserAccountRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final SupplierRepository supplierRepository;
    private final ResourceDocumentRepository documentRepository;
    private final ResourceCopyRepository copyRepository;
    private final AcquisitionRequestRepository acquisitionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedUsers();
        if (documentRepository.count() > 0) return;

        Category it = category("CNTT", "Công nghệ thông tin", "Lập trình, dữ liệu, hệ thống thông tin");
        Category economics = category("KT", "Kinh tế", "Quản trị, kinh tế học và tài chính");
        Category literature = category("VH", "Văn học", "Văn học Việt Nam và thế giới");
        Category science = category("KHTN", "Khoa học tự nhiên", "Toán học, vật lý và khoa học cơ bản");

        Author joshua = author("Joshua Bloch", 1961, "Hoa Kỳ");
        Author robert = author("Robert C. Martin", 1952, "Hoa Kỳ");
        Author namCao = author("Nam Cao", 1915, "Việt Nam");
        Author nguyen = author("Nguyễn Văn Tuấn", 1966, "Việt Nam");

        Publisher tre = publisher("Nhà xuất bản Trẻ", "TP. Hồ Chí Minh", "contact@nxbtre.com.vn", "028 3931 6289");
        Publisher education = publisher("Nhà xuất bản Giáo dục Việt Nam", "Hà Nội", "info@nxbgd.vn", "024 3822 0801");
        Publisher pearson = publisher("Pearson Education", "London, United Kingdom", "support@pearson.com", null);

        Supplier fahasa = supplier("Công ty FAHASA", "Phòng khách hàng thư viện", "sales@fahasa.com", "1900 636467", "TP. Hồ Chí Minh");
        Supplier alphabooks = supplier("Alpha Books", "Bộ phận phân phối", "distribution@alphabooks.vn", "024 3722 6234", "Hà Nội");

        ResourceDocument effective = document("TL-CNTT-001", "Effective Java", "9780134685991", 2018,
                "005.133 BLO", it, pearson, CatalogStatus.PUBLISHED, joshua);
        effective.setEdition("3rd edition");
        effective.setLanguage("English");
        effective.setPhysicalDescription("416 trang; 24 cm");
        effective.setSummary("Các thực hành tốt nhất để viết mã Java rõ ràng, đúng đắn và hiệu quả.");
        effective.setKeywords("Java, lập trình, best practices");

        ResourceDocument cleanCode = document("TL-CNTT-002", "Clean Code", "9780132350884", 2008,
                "005.1 MAR", it, pearson, CatalogStatus.PUBLISHED, robert);
        cleanCode.setLanguage("English");
        cleanCode.setPhysicalDescription("464 trang; 23 cm");
        cleanCode.setSummary("Cẩm nang thực hành về cách viết mã nguồn dễ đọc và dễ bảo trì.");

        ResourceDocument chiPheo = document("TL-VH-001", "Chí Phèo", null, 2020,
                "895.922 NAM", literature, tre, CatalogStatus.PUBLISHED, namCao);
        chiPheo.setPhysicalDescription("184 trang; 20 cm");
        chiPheo.setKeywords("truyện ngắn, văn học hiện thực, Nam Cao");

        ResourceDocument data = document("TL-KHTN-001", "Phân tích dữ liệu với R", "9786040000001", 2023,
                "519.5 NGU", science, education, CatalogStatus.DRAFT, nguyen);
        data.setPhysicalDescription("328 trang; minh họa; 24 cm");
        data.setSummary("Nhập môn phân tích và trực quan hóa dữ liệu bằng ngôn ngữ R.");

        ResourceDocument management = document("TL-KT-001", "Quản trị học hiện đại", "9786040000002", 2022,
                "658.0 NGU", economics, education, CatalogStatus.DRAFT, nguyen);
        management.setPhysicalDescription("290 trang; 24 cm");

        copy(effective, "TL-CNTT-001-001", new BigDecimal("425000"), "Kệ CNTT-A1");
        copy(effective, "TL-CNTT-001-002", new BigDecimal("425000"), "Kệ CNTT-A1");
        copy(cleanCode, "TL-CNTT-002-001", new BigDecimal("390000"), "Kệ CNTT-A1");
        copy(chiPheo, "TL-VH-001-001", new BigDecimal("72000"), "Kệ Văn học-B2");

        AcquisitionRequest pending = request("BS-DEMO-001", "Bổ sung tài liệu học kỳ I", "Nhân viên bổ sung",
                AcquisitionStatus.PENDING, fahasa, LocalDate.now().plusDays(14), "Ưu tiên tài liệu phục vụ môn học mới.");
        item(pending, data, 5, new BigDecimal("165000"));
        item(pending, management, 3, new BigDecimal("142000"));
        acquisitionRepository.save(pending);

        AcquisitionRequest completed = request("BS-DEMO-002", "Bổ sung sách kỹ năng lập trình", "Quản trị hệ thống",
                AcquisitionStatus.COMPLETED, alphabooks, LocalDate.now().minusDays(5), "Đã nhập kho đầy đủ.");
        completed.setApprovedBy("Quản trị hệ thống");
        completed.setApprovedAt(LocalDateTime.now().minusDays(10));
        completed.setCompletedAt(LocalDateTime.now().minusDays(3));
        item(completed, cleanCode, 1, new BigDecimal("390000"));
        acquisitionRepository.save(completed);
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;
        user("admin", "admin123", "Quản trị hệ thống", "admin@docucatalog.local", UserRole.ADMIN);
        user("cataloger", "catalog123", "Nguyễn Minh Anh", "cataloger@docucatalog.local", UserRole.CATALOGER);
        user("acquisition", "acq123", "Trần Thu Hà", "acquisition@docucatalog.local", UserRole.ACQUISITION);
    }

    private void user(String username, String rawPassword, String fullName, String email, UserRole role) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(rawPassword));
        account.setFullName(fullName);
        account.setEmail(email);
        account.setRole(role);
        account.setDepartment(switch (role) {
            case ADMIN -> "Quản trị hệ thống";
            case CATALOGER -> "Phòng Biên mục";
            case ACQUISITION -> "Phòng Phát triển nguồn";
        });
        account.setAvatarTheme(switch (role) {
            case ADMIN -> "VIOLET";
            case CATALOGER -> "TEAL";
            case ACQUISITION -> "AMBER";
        });
        userRepository.save(account);
    }

    private Category category(String code, String name, String description) {
        Category entity = new Category(); entity.setCode(code); entity.setName(name); entity.setDescription(description);
        return categoryRepository.save(entity);
    }

    private Author author(String name, Integer birthYear, String nationality) {
        Author entity = new Author(); entity.setName(name); entity.setBirthYear(birthYear); entity.setNationality(nationality);
        return authorRepository.save(entity);
    }

    private Publisher publisher(String name, String address, String email, String phone) {
        Publisher entity = new Publisher(); entity.setName(name); entity.setAddress(address); entity.setEmail(email); entity.setPhone(phone);
        return publisherRepository.save(entity);
    }

    private Supplier supplier(String name, String contact, String email, String phone, String address) {
        Supplier entity = new Supplier(); entity.setName(name); entity.setContactPerson(contact); entity.setEmail(email); entity.setPhone(phone); entity.setAddress(address);
        return supplierRepository.save(entity);
    }

    private ResourceDocument document(String code, String title, String isbn, int year, String callNumber,
                                      Category category, Publisher publisher, CatalogStatus status, Author... authors) {
        ResourceDocument entity = new ResourceDocument();
        entity.setCatalogCode(code); entity.setTitle(title); entity.setIsbn(isbn); entity.setPublicationYear(year);
        entity.setCallNumber(callNumber); entity.setClassificationNumber(callNumber.split(" ")[0]);
        entity.setCategory(category); entity.setPublisher(publisher); entity.setStatus(status);
        entity.setAuthors(new LinkedHashSet<>(java.util.List.of(authors)));
        return documentRepository.save(entity);
    }

    private void copy(ResourceDocument document, String accession, BigDecimal price, String location) {
        ResourceCopy entity = new ResourceCopy(); entity.setDocument(document); entity.setAccessionNumber(accession);
        entity.setAcquiredDate(LocalDate.now().minusMonths(2)); entity.setPrice(price); entity.setLocation(location);
        entity.setCondition(CopyCondition.GOOD); entity.setStatus(CopyStatus.AVAILABLE); copyRepository.save(entity);
    }

    private AcquisitionRequest request(String code, String title, String requester, AcquisitionStatus status,
                                       Supplier supplier, LocalDate expected, String note) {
        AcquisitionRequest entity = new AcquisitionRequest(); entity.setRequestCode(code); entity.setTitle(title);
        entity.setRequesterName(requester); entity.setRequestDate(LocalDate.now().minusDays(12)); entity.setExpectedDate(expected);
        entity.setStatus(status); entity.setSupplier(supplier); entity.setNote(note); return entity;
    }

    private void item(AcquisitionRequest request, ResourceDocument document, int quantity, BigDecimal price) {
        AcquisitionItem item = new AcquisitionItem(); item.setDocument(document); item.setQuantity(quantity); item.setUnitPrice(price); request.addItem(item);
    }
}
