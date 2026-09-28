package vn.edu.docucatalog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AvatarStorageService {
    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024;
    private static final int AVATAR_SIZE = 256;
    private static final long MAX_PIXELS = 40_000_000L;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");
    private static final Pattern SAFE_FILE_NAME = Pattern.compile("avatar-[0-9]+\\.jpg");

    private final Path storageDirectory;

    public AvatarStorageService(@Value("${app.avatar-storage-dir:./data/uploads/avatars}") String storageDirectory) {
        this.storageDirectory = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public String store(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Bạn hãy chọn một ảnh trước khi lưu");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("Ảnh đại diện cần nhỏ hơn hoặc bằng 2 MB");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Ảnh đại diện chỉ nhận tệp PNG hoặc JPEG");
        }

        BufferedImage source;
        try (InputStream input = file.getInputStream()) {
            source = ImageIO.read(input);
        } catch (IOException ex) {
            throw new BusinessException("Không đọc được tệp ảnh. Bạn hãy thử chọn ảnh khác");
        }
        if (source == null || source.getWidth() <= 0 || source.getHeight() <= 0
                || (long) source.getWidth() * source.getHeight() > MAX_PIXELS) {
            throw new BusinessException("Tệp ảnh không hợp lệ hoặc có kích thước điểm ảnh quá lớn");
        }

        BufferedImage avatar = cropAndResize(source);
        String fileName = "avatar-" + userId + ".jpg";
        Path temporaryFile = null;
        try {
            Files.createDirectories(storageDirectory);
            temporaryFile = Files.createTempFile(storageDirectory, "avatar-", ".tmp");
            if (!ImageIO.write(avatar, "jpg", temporaryFile.toFile())) {
                throw new IOException("Không có bộ mã hóa JPEG");
            }
            moveSafely(temporaryFile, storageDirectory.resolve(fileName));
            return fileName;
        } catch (IOException ex) {
            if (temporaryFile != null) {
                try {
                    Files.deleteIfExists(temporaryFile);
                } catch (IOException ignored) {
                    // Tệp tạm sẽ được hệ điều hành dọn sau nếu đang bị khóa.
                }
            }
            throw new BusinessException("Chưa thể lưu ảnh đại diện. Bạn hãy thử lại sau");
        }
    }

    public Resource load(String fileName) {
        if (fileName == null || !SAFE_FILE_NAME.matcher(fileName).matches()) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh đại diện");
        }
        Path file = storageDirectory.resolve(fileName).normalize();
        if (!file.startsWith(storageDirectory) || !Files.isRegularFile(file)) {
            throw new ResourceNotFoundException("Không tìm thấy ảnh đại diện");
        }
        return new FileSystemResource(file);
    }

    private BufferedImage cropAndResize(BufferedImage source) {
        int side = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - side) / 2;
        int y = (source.getHeight() - side) / 2;
        BufferedImage result = new BufferedImage(AVATAR_SIZE, AVATAR_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = result.createGraphics();
        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, AVATAR_SIZE, AVATAR_SIZE);
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.drawImage(source, 0, 0, AVATAR_SIZE, AVATAR_SIZE, x, y, x + side, y + side, null);
        } finally {
            graphics.dispose();
        }
        return result;
    }

    private void moveSafely(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
