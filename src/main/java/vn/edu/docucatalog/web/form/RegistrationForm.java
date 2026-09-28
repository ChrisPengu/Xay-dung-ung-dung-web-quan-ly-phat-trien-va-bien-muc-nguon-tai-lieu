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
public class RegistrationForm {
    @NotBlank(message = "Bạn hãy nhập họ và tên")
    @Size(max = 120, message = "Họ và tên không được dài quá 120 ký tự")
    private String fullName;

    @NotBlank(message = "Bạn hãy chọn tên đăng nhập")
    @Size(min = 4, max = 50, message = "Tên đăng nhập cần từ 4 đến 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Tên đăng nhập chỉ gồm chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang")
    private String username;

    @NotBlank(message = "Bạn hãy nhập email")
    @Email(message = "Email chưa đúng định dạng")
    @Size(max = 150, message = "Email không được dài quá 150 ký tự")
    private String email;

    @Size(max = 100, message = "Tên bộ phận không được dài quá 100 ký tự")
    private String department;

    @NotNull(message = "Bạn hãy chọn công việc phụ trách")
    private UserRole requestedRole;

    @NotBlank(message = "Bạn hãy tạo mật khẩu")
    @Size(min = 8, max = 72, message = "Mật khẩu cần từ 8 đến 72 ký tự")
    private String password;

    @NotBlank(message = "Bạn hãy nhập lại mật khẩu")
    @Size(max = 72, message = "Mật khẩu không được dài quá 72 ký tự")
    private String confirmPassword;
}
