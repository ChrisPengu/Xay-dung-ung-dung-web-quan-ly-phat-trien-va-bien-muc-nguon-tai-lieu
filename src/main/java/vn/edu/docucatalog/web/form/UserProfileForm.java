package vn.edu.docucatalog.web.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileForm {
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
    private String fullName;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    private String email;

    @Size(max = 100, message = "Đơn vị công tác tối đa 100 ký tự")
    private String department;

    @Pattern(regexp = "^$|^[0-9+().\\-\\s]{7,20}$", message = "Số điện thoại chỉ gồm 7-20 chữ số và ký hiệu hợp lệ")
    private String phone;

    @Size(max = 500, message = "Giới thiệu tối đa 500 ký tự")
    private String bio;

    @NotBlank(message = "Vui lòng chọn màu đại diện")
    @Pattern(regexp = "TEAL|BLUE|VIOLET|AMBER|ROSE", message = "Màu đại diện không hợp lệ")
    private String avatarTheme = "TEAL";
}
