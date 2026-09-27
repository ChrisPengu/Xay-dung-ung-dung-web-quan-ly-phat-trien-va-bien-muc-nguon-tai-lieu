package vn.edu.docucatalog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("Quản trị viên"),
    CATALOGER("Nhân viên biên mục"),
    ACQUISITION("Nhân viên bổ sung");

    private final String label;
}
