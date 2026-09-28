package vn.edu.docucatalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.edu.docucatalog.domain.UserAccount;
import vn.edu.docucatalog.domain.UserRole;
import vn.edu.docucatalog.repository.UserAccountRepository;
import vn.edu.docucatalog.service.AvatarStorageService;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountAccessIntegrationTest {
    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"");

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private UserAccountRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @TempDir
    Path temporaryDirectory;

    @Test
    void publicRegistrationCreatesAPendingNonAdminAccount() throws Exception {
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient client = HttpClient.newBuilder().cookieHandler(cookies)
                .followRedirects(HttpClient.Redirect.NEVER).build();

        HttpResponse<String> page = client.send(HttpRequest.newBuilder(uri("/register")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        assertThat(page.statusCode()).isEqualTo(200);
        assertThat(page.body()).contains("Tạo tài khoản", "Tài khoản sẽ chờ duyệt");
        Matcher matcher = CSRF.matcher(page.body());
        assertThat(matcher.find()).as("CSRF token exists in registration form").isTrue();

        String username = "new-reader-" + System.nanoTime();
        String body = "fullName=" + encode("Nguyễn Minh Anh")
                + "&username=" + encode(username)
                + "&email=" + encode(username + "@example.test")
                + "&department=" + encode("Phòng Biên mục")
                + "&requestedRole=CATALOGER"
                + "&password=" + encode("MatKhau!234")
                + "&confirmPassword=" + encode("MatKhau!234")
                + "&_csrf=" + encode(matcher.group(1));
        HttpResponse<String> response = client.send(HttpRequest.newBuilder(uri("/register"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build(),
                HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(302);
        assertThat(response.headers().firstValue("location"))
                .hasValue("http://localhost:" + port + "/login?registered");
        UserAccount account = repository.findByUsernameIgnoreCase(username).orElseThrow();
        assertThat(account.isApproved()).isFalse();
        assertThat(account.isActive()).isFalse();
        assertThat(account.getRole()).isEqualTo(UserRole.CATALOGER);
        assertThat(passwordEncoder.matches("MatKhau!234", account.getPassword())).isTrue();
    }

    @Test
    void avatarUploadIsValidatedCroppedAndResized() throws Exception {
        AvatarStorageService storage = new AvatarStorageService(temporaryDirectory.toString());
        BufferedImage source = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = source.createGraphics();
        graphics.setColor(new Color(28, 122, 112));
        graphics.fillRect(0, 0, source.getWidth(), source.getHeight());
        graphics.dispose();
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(source, "png", bytes);

        String fileName = storage.store(42L,
                new MockMultipartFile("avatar", "portrait.png", "image/png", bytes.toByteArray()));
        BufferedImage saved = ImageIO.read(storage.load(fileName).getInputStream());

        assertThat(fileName).isEqualTo("avatar-42.jpg");
        assertThat(saved.getWidth()).isEqualTo(256);
        assertThat(saved.getHeight()).isEqualTo(256);
        assertThatThrownBy(() -> storage.store(42L,
                new MockMultipartFile("avatar", "note.txt", "text/plain", "not an image".getBytes(StandardCharsets.UTF_8))))
                .hasMessageContaining("PNG hoặc JPEG");
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
