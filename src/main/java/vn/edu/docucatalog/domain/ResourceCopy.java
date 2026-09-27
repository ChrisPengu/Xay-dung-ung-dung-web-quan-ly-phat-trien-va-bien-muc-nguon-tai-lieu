package vn.edu.docucatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "resource_copies")
public class ResourceCopy extends BaseEntity {

    @Column(nullable = false, unique = true, length = 60)
    private String accessionNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private ResourceDocument document;

    @Column(nullable = false)
    private LocalDate acquiredDate;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "physical_condition", nullable = false, length = 20)
    private CopyCondition condition = CopyCondition.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Column(length = 500)
    private String note;
}
