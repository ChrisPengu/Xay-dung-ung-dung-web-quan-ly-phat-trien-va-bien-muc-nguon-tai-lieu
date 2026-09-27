package vn.edu.docucatalog.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import vn.edu.docucatalog.domain.CopyCondition;
import vn.edu.docucatalog.domain.CopyStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CopyForm {
    private Long id;

    @NotBlank(message = "Số đăng ký cá biệt không được để trống")
    @Size(max = 60, message = "Số đăng ký tối đa 60 ký tự")
    private String accessionNumber;

    @NotNull(message = "Vui lòng nhập ngày bổ sung")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate acquiredDate = LocalDate.now();

    @DecimalMin(value = "0", message = "Giá không được âm")
    private BigDecimal price;

    @NotBlank(message = "Vị trí không được để trống")
    @Size(max = 100, message = "Vị trí tối đa 100 ký tự")
    private String location = "Kho chính";

    @NotNull(message = "Vui lòng chọn tình trạng")
    private CopyCondition condition = CopyCondition.NEW;

    @NotNull(message = "Vui lòng chọn trạng thái")
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
