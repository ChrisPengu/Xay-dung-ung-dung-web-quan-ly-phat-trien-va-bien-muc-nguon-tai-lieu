package vn.edu.docucatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    @NotBlank(message = "Mã thể loại không được để trống")
    @Size(max = 30, message = "Mã thể loại tối đa 30 ký tự")
    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @NotBlank(message = "Tên thể loại không được để trống")
    @Size(max = 120, message = "Tên thể loại tối đa 120 ký tự")
    @Column(nullable = false, length = 120)
    private String name;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    @Column(length = 500)
    private String description;
}
