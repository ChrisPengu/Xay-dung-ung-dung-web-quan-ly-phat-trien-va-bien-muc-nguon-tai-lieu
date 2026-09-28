package vn.edu.docucatalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.docucatalog.domain.UserAccount;
import vn.edu.docucatalog.domain.UserRole;
import vn.edu.docucatalog.repository.UserAccountRepository;
import vn.edu.docucatalog.web.form.PasswordChangeForm;
import vn.edu.docucatalog.web.form.RegistrationForm;
import vn.edu.docucatalog.web.form.UserAccountForm;
import vn.edu.docucatalog.web.form.UserProfileForm;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccountService {
    public static final List<String> PROFILE_THEMES = List.of("TEAL", "BLUE", "VIOLET", "AMBER", "ROSE");

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AvatarStorageService avatarStorageService;

    public List<UserAccount> findAll() {
        return repository.findAllByOrderByFullNameAsc();
    }

    public UserAccount get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy tài khoản"));
    }

    public UserAccountForm toForm(UserAccount account) {
        UserAccountForm form = new UserAccountForm();
        form.setId(account.getId());
        form.setUsername(account.getUsername());
        form.setFullName(account.getFullName());
        form.setEmail(account.getEmail());
        form.setRole(account.getRole());
        form.setActive(account.isActive());
        return form;
    }

    @Transactional
    public UserAccount save(UserAccountForm form, String actingUsername) {
        validateUnique(form.getUsername(), form.getEmail(), form.getId());
        boolean creating = form.getId() == null;
        UserAccount account = creating ? new UserAccount() : get(form.getId());

        if (creating && (form.getPassword() == null || form.getPassword().isBlank())) {
            throw new BusinessException("Mật khẩu là bắt buộc khi tạo tài khoản");
        }
        if (!creating && account.getUsername().equalsIgnoreCase(actingUsername)
                && (account.getRole() != form.getRole() || !form.isActive())) {
            throw new BusinessException("Bạn không thể tự đổi vai trò hoặc tạm khóa tài khoản đang đăng nhập");
        }
        ensureAdminStillAvailable(account, form.getRole(), form.isActive());

        account.setUsername(form.getUsername().trim().toLowerCase(Locale.ROOT));
        account.setFullName(form.getFullName().trim());
        account.setEmail(normalizeEmail(form.getEmail()));
        account.setRole(form.getRole());
        account.setActive(form.isActive());
        if (creating || form.isActive()) {
            account.setApproved(true);
        }
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            account.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        return repository.save(account);
    }

    @Transactional
    public void setActive(Long id, boolean active, String actingUsername) {
        UserAccount account = get(id);
        if (account.getUsername().equalsIgnoreCase(actingUsername)) {
            throw new BusinessException("Bạn không thể tự tạm khóa tài khoản đang đăng nhập");
        }
        ensureAdminStillAvailable(account, account.getRole(), active);
        account.setActive(active);
        if (active) {
            account.setApproved(true);
        }
    }

    @Transactional
    public UserAccount register(RegistrationForm form) {
        if (form.getRequestedRole() == UserRole.ADMIN) {
            throw new BusinessException("Tài khoản quản trị cần được tạo bởi một quản trị viên đang hoạt động");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Hai lần nhập mật khẩu chưa khớp nhau");
        }
        validateUnique(form.getUsername(), form.getEmail(), null);

        UserAccount account = new UserAccount();
        account.setUsername(form.getUsername().trim().toLowerCase(Locale.ROOT));
        account.setPassword(passwordEncoder.encode(form.getPassword()));
        account.setFullName(form.getFullName().trim());
        account.setEmail(normalizeEmail(form.getEmail()));
        account.setDepartment(trimToNull(form.getDepartment()));
        account.setRole(form.getRequestedRole());
        account.setAvatarTheme(form.getRequestedRole() == UserRole.CATALOGER ? "TEAL" : "BLUE");
        account.setApproved(false);
        account.setActive(false);
        return repository.save(account);
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeForm form) {
        UserAccount account = get(userId);
        if (!passwordEncoder.matches(form.getCurrentPassword(), account.getPassword())) {
            throw new BusinessException("Mật khẩu hiện tại chưa chính xác");
        }
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            throw new BusinessException("Xác nhận mật khẩu mới không khớp");
        }
        if (passwordEncoder.matches(form.getNewPassword(), account.getPassword())) {
            throw new BusinessException("Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        account.setPassword(passwordEncoder.encode(form.getNewPassword()));
    }

    public UserProfileForm toProfileForm(UserAccount account) {
        UserProfileForm form = new UserProfileForm();
        form.setFullName(account.getFullName());
        form.setEmail(account.getEmail());
        form.setDepartment(account.getDepartment());
        form.setPhone(account.getPhone());
        form.setBio(account.getBio());
        form.setAvatarTheme(account.getAvatarTheme());
        return form;
    }

    @Transactional
    public UserAccount updateProfile(Long userId, UserProfileForm form) {
        UserAccount account = get(userId);
        String email = normalizeEmail(form.getEmail());
        if (email != null && repository.existsByEmailIgnoreCaseAndIdNot(email, userId)) {
            throw new BusinessException("Email đã được tài khoản khác sử dụng");
        }
        if (!PROFILE_THEMES.contains(form.getAvatarTheme())) {
            throw new BusinessException("Màu đại diện không hợp lệ");
        }
        account.setFullName(form.getFullName().trim());
        account.setEmail(email);
        account.setDepartment(trimToNull(form.getDepartment()));
        account.setPhone(trimToNull(form.getPhone()));
        account.setBio(trimToNull(form.getBio()));
        account.setAvatarTheme(form.getAvatarTheme());
        return account;
    }

    @Transactional
    public UserAccount updateAvatar(Long userId, MultipartFile avatar) {
        UserAccount account = get(userId);
        account.setAvatarFileName(avatarStorageService.store(userId, avatar));
        return account;
    }

    private void validateUnique(String usernameValue, String emailValue, Long id) {
        String username = usernameValue.trim();
        boolean usernameExists = id == null
                ? repository.existsByUsernameIgnoreCase(username)
                : repository.existsByUsernameIgnoreCaseAndIdNot(username, id);
        if (usernameExists) throw new BusinessException("Tên đăng nhập đã tồn tại");

        String email = normalizeEmail(emailValue);
        if (email != null) {
            boolean emailExists = id == null
                    ? repository.existsByEmailIgnoreCase(email)
                    : repository.existsByEmailIgnoreCaseAndIdNot(email, id);
            if (emailExists) throw new BusinessException("Email đã được tài khoản khác sử dụng");
        }
    }

    private void ensureAdminStillAvailable(UserAccount current, UserRole newRole, boolean newActive) {
        if (current.getId() != null && current.getRole() == UserRole.ADMIN && current.isActive()
                && (newRole != UserRole.ADMIN || !newActive)
                && repository.countByRoleAndActiveTrue(UserRole.ADMIN) <= 1) {
            throw new BusinessException("Hệ thống phải luôn còn ít nhất một quản trị viên hoạt động");
        }
    }

    private String normalizeEmail(String value) {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
