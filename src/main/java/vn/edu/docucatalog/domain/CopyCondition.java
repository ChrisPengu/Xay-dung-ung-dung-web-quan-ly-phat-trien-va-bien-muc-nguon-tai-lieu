package vn.edu.docucatalog.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CopyCondition {
    NEW("Mới"),
    GOOD("Tốt"),
    WORN("Cũ"),
    DAMAGED("Hư hỏng");

    private final String label;
}
