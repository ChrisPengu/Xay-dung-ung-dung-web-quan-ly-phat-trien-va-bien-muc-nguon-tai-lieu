package vn.edu.docucatalog.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.edu.docucatalog.domain.UserRole;

@Getter
@Setter
public class UserAccountForm {
    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Tên đăng nhập chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang")
    private String username;

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @NotNull(message = "Vui lòng chọn vai trò")
    private UserRole role;

    private boolean active = true;

    @Pattern(regexp = "^$|.{8,72}$", message = "Mật khẩu phải từ 8 đến 72 ký tự")
    private String password;
}
