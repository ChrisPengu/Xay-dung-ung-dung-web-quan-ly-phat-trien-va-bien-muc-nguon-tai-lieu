package vn.edu.docucatalog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CopyStatus {
    AVAILABLE("Sẵn sàng", "success"),
    CHECKED_OUT("Đang mượn", "info"),
    LOST("Thất lạc", "danger"),
    WITHDRAWN("Thanh lý", "neutral");

    private final String label;
    private final String tone;
}
