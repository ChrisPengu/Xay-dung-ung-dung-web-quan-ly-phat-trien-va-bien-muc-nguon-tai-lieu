package vn.edu.docucatalog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AcquisitionStatus {
    DRAFT("Bản nháp", "neutral"),
    PENDING("Chờ duyệt", "warning"),
    APPROVED("Đã duyệt", "info"),
    REJECTED("Từ chối", "danger"),
    COMPLETED("Hoàn tất", "success");

    private final String label;
    private final String tone;
}
