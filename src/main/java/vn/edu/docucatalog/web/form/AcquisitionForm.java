package vn.edu.docucatalog.web.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AcquisitionForm {
    private Long id;

    @NotBlank(message = "Tên đề xuất không được để trống")
    @Size(max = 200, message = "Tên đề xuất tối đa 200 ký tự")
    private String title;

    private Long supplierId;

    @FutureOrPresent(message = "Ngày dự kiến không được ở quá khứ")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedDate;

    @Size(max = 1000, message = "Ghi chú tối đa 1000 ký tự")
    private String note;

    @Valid
    private List<AcquisitionItemForm> items = new ArrayList<>();

    public AcquisitionForm() {
        items.add(new AcquisitionItemForm());
    }
}
