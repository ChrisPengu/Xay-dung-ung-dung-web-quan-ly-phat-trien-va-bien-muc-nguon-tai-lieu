package vn.edu.docucatalog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CatalogStatus {
    DRAFT("Bản nháp", "neutral"),
    PUBLISHED("Đã biên mục", "success");

    private final String label;
    private final String tone;
}
