package vn.edu.docucatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "publishers")
public class Publisher extends BaseEntity {

    @NotBlank(message = "Tên nhà xuất bản không được để trống")
    @Size(max = 150, message = "Tên nhà xuất bản tối đa 150 ký tự")
    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String address;

    @Email(message = "Email không đúng định dạng")
    @Size(max = 150, message = "Email tối đa 150 ký tự")
    @Column(length = 150)
    private String email;

    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    @Column(length = 20)
    private String phone;
}
