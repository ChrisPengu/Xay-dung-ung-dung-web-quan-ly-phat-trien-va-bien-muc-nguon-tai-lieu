package vn.edu.docucatalog.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import vn.edu.docucatalog.domain.CatalogStatus;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
public class DocumentForm {
    private Long id;

    @NotBlank(message = "Mã biên mục không được để trống")
    @Size(max = 40, message = "Mã biên mục tối đa 40 ký tự")
    @Pattern(regexp = "[A-Za-z0-9._/-]+", message = "Mã chỉ được chứa chữ, số, dấu chấm, gạch ngang, gạch dưới hoặc gạch chéo")
    private String catalogCode;

    @NotBlank(message = "Nhan đề không được để trống")
    @Size(max = 255, message = "Nhan đề tối đa 255 ký tự")
    private String title;

    @Size(max = 255, message = "Nhan đề phụ tối đa 255 ký tự")
    private String subtitle;

    @Pattern(regexp = "^$|^(97[89])?\\d{9}[\\dXx]$", message = "ISBN phải gồm 10 hoặc 13 ký tự số")
    private String isbn;

    @NotBlank(message = "Ngôn ngữ không được để trống")
    @Size(max = 50, message = "Ngôn ngữ tối đa 50 ký tự")
    private String language = "Tiếng Việt";

    @Min(value = 1000, message = "Năm xuất bản không hợp lệ")
    @Max(value = 2100, message = "Năm xuất bản không hợp lệ")
    private Integer publicationYear;

    @Size(max = 80, message = "Lần xuất bản tối đa 80 ký tự")
    private String edition;

    @Size(max = 50, message = "Số phân loại tối đa 50 ký tự")
    private String classificationNumber;

    @Size(max = 50, message = "Ký hiệu xếp giá tối đa 50 ký tự")
    private String callNumber;

    @Size(max = 500, message = "Từ khóa tối đa 500 ký tự")
    private String keywords;

    @Size(max = 5000, message = "Tóm tắt tối đa 5000 ký tự")
    private String summary;

    @Size(max = 255, message = "Mô tả vật lý tối đa 255 ký tự")
    private String physicalDescription;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private CatalogStatus status = CatalogStatus.DRAFT;

    @NotNull(message = "Vui lòng chọn thể loại")
    private Long categoryId;

    private Long publisherId;
    private Set<Long> authorIds = new LinkedHashSet<>();
}
