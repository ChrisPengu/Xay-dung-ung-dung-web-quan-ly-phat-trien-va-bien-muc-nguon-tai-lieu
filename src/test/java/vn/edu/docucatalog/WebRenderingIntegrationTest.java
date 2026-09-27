package vn.edu.docucatalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.docucatalog.domain.*;
import vn.edu.docucatalog.repository.*;

import java.math.BigDecimal;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebRenderingIntegrationTest {
    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"");

    @Value("${local.server.port}") private int port;
    @Autowired private UserAccountRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private SupplierRepository supplierRepository;
    @Autowired private ResourceDocumentRepository documentRepository;
    @Autowired private ResourceCopyRepository copyRepository;
    @Autowired private AcquisitionRequestRepository acquisitionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private HttpClient client;
    private ResourceDocument document;
    private ResourceCopy copy;
    private AcquisitionRequest acquisition;

    @BeforeEach
    void setUp() throws Exception {
        if (userRepository.findByUsernameIgnoreCase("web-admin").isEmpty()) {
            UserAccount user = new UserAccount();
            user.setUsername("web-admin");
            user.setPassword(passwordEncoder.encode("test-password"));
            user.setFullName("Quản trị kiểm thử web");
            user.setRole(UserRole.ADMIN);
            userRepository.save(user);
        }

        Category category = new Category();
        category.setCode("WEB-" + System.nanoTime());
        category.setName("Kiểm thử giao diện");
        category = categoryRepository.save(category);

        document = new ResourceDocument();
        document.setCatalogCode("WEB-DOC-" + System.nanoTime());
        document.setTitle("Biểu ghi kiểm thử giao diện");
        document.setCategory(category);
        document.setStatus(CatalogStatus.PUBLISHED);
        document = documentRepository.save(document);

        copy = new ResourceCopy();
        copy.setAccessionNumber("WEB-COPY-" + System.nanoTime());
        copy.setDocument(document);
        copy.setAcquiredDate(LocalDate.now());
        copy.setLocation("Kho kiểm thử");
        copy = copyRepository.save(copy);

        Supplier supplier = new Supplier();
        supplier.setName("Nhà cung cấp web " + System.nanoTime());
        supplier = supplierRepository.save(supplier);

        acquisition = new AcquisitionRequest();
        acquisition.setRequestCode("WEB-ACQ-" + System.nanoTime());
        acquisition.setTitle("Đề xuất kiểm thử giao diện");
        acquisition.setRequesterName("Người kiểm thử");
        acquisition.setRequestDate(LocalDate.now());
        acquisition.setStatus(AcquisitionStatus.DRAFT);
        acquisition.setSupplier(supplier);
        AcquisitionItem item = new AcquisitionItem();
        item.setDocument(document);
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("99000"));
        acquisition.addItem(item);
        acquisition = acquisitionRepository.save(acquisition);

        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        client = HttpClient.newBuilder().cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER).build();
        login();
    }

    @Test
    void authenticatedPagesRenderWithoutTemplateErrors() throws Exception {
        assertPage("/dashboard", "Tổng quan thư viện");
        assertPage("/documents", "Biên mục tài liệu");
        assertPage("/documents/new", "Biên mục tài liệu mới");
        assertPage("/documents/" + document.getId(), "Biểu ghi kiểm thử giao diện");
        assertPage("/documents/" + document.getId() + "/edit", "Chỉnh sửa biểu ghi");
        assertPage("/documents/" + document.getId() + "/duplicate", "Sao chép để biên mục");
        assertPage("/documents/" + document.getId() + "/copies/" + copy.getId() + "/edit", "Chỉnh sửa bản ấn phẩm");
        assertPage("/acquisitions", "Phát triển nguồn");
        assertPage("/acquisitions/new", "Tạo đề xuất mới");
        assertPage("/acquisitions/" + acquisition.getId(), "Đề xuất kiểm thử giao diện");
        assertPage("/acquisitions/" + acquisition.getId() + "/edit", "Chỉnh sửa đề xuất");
        assertPage("/references", "Dữ liệu danh mục");
        assertPage("/references?tab=authors", "Tác giả");
        assertPage("/references?tab=publishers", "Nhà xuất bản");
        assertPage("/references?tab=suppliers", "Nhà cung cấp");
        assertPage("/users", "Quản lý người dùng");
        assertPage("/users/new", "Thêm tài khoản");
        assertPage("/account/password", "Đổi mật khẩu");
    }

    @Test
    void stylesheetsAreServedLocallyWithCssContentType() throws Exception {
        assertStylesheet("/css/app.css", ":root");
        assertStylesheet("/webjars/bootstrap-icons/1.13.1/font/bootstrap-icons.min.css", "bootstrap-icons");
    }

    private void login() throws Exception {
        HttpResponse<String> loginPage = client.send(HttpRequest.newBuilder(uri("/login")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(loginPage.statusCode()).isEqualTo(200);
        Matcher matcher = CSRF.matcher(loginPage.body());
        assertThat(matcher.find()).as("CSRF token exists in login form").isTrue();
        String body = "username=web-admin&password=test-password&_csrf="
                + URLEncoder.encode(matcher.group(1), StandardCharsets.UTF_8);
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/login"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("location")).hasValue("http://localhost:" + port + "/dashboard");
    }

    private void assertPage(String path, String expectedText) throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(path).isEqualTo(200);
        assertThat(response.body()).as(path)
                .contains(expectedText)
                .contains("<head>")
                .contains("/webjars/bootstrap-icons/1.13.1/font/bootstrap-icons.min.css")
                .contains("/css/app.css")
                .contains("</head>")
                .doesNotContain("TemplateInputException");
    }

    private void assertStylesheet(String path, String expectedText) throws Exception {
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(path).isEqualTo(200);
        assertThat(response.headers().firstValue("content-type")).hasValueSatisfying(
                value -> assertThat(value).startsWith("text/css"));
        assertThat(response.body()).contains(expectedText);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
