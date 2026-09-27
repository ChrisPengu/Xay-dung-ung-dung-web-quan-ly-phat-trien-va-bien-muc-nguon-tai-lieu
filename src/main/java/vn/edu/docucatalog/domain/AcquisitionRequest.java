package vn.edu.docucatalog.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "acquisition_requests")
public class AcquisitionRequest extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String requestCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 120)
    private String requesterName;

    @Column(nullable = false)
    private LocalDate requestDate;

    private LocalDate expectedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcquisitionStatus status = AcquisitionStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(length = 1000)
    private String note;

    @Column(length = 120)
    private String approvedBy;

    private LocalDateTime approvedAt;

    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<AcquisitionItem> items = new ArrayList<>();

    public void addItem(AcquisitionItem item) {
        items.add(item);
        item.setRequest(this);
    }

    public void clearItems() {
        items.forEach(item -> item.setRequest(null));
        items.clear();
    }

    @Transient
    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(AcquisitionItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transient
    public int getTotalQuantity() {
        return items.stream().mapToInt(AcquisitionItem::getQuantity).sum();
    }
}
