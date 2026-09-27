package vn.edu.docucatalog.web.form;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AcquisitionItemForm {
    @NotNull(message = "Vui lòng chọn tài liệu")
    private Long documentId;

    @Min(value = 1, message = "Số lượng tối thiểu là 1")
    private int quantity = 1;

    @NotNull(message = "Vui lòng nhập đơn giá")
    @DecimalMin(value = "0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
    private BigDecimal unitPrice;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    private String note;
}
