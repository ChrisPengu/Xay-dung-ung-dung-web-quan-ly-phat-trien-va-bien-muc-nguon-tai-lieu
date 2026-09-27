package vn.edu.docucatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "authors")
public class Author extends BaseEntity {

    @NotBlank(message = "Tên tác giả không được để trống")
    @Size(max = 150, message = "Tên tác giả tối đa 150 ký tự")
    @Column(nullable = false, length = 150)
    private String name;

    @Min(value = 1000, message = "Năm sinh không hợp lệ")
    @Max(value = 2100, message = "Năm sinh không hợp lệ")
    private Integer birthYear;

    @Size(max = 80, message = "Quốc tịch tối đa 80 ký tự")
    @Column(length = 80)
    private String nationality;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    @Column(length = 500)
    private String note;
}
