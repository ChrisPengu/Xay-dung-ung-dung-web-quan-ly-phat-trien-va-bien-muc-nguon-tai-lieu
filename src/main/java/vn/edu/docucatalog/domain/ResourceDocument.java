package vn.edu.docucatalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "resource_documents")
public class ResourceDocument extends BaseEntity {

    @Column(nullable = false, unique = true, length = 40)
    private String catalogCode;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 50)
    private String language = "Tiếng Việt";

    private Integer publicationYear;

    @Column(length = 80)
    private String edition;

    @Column(length = 50)
    private String classificationNumber;

    @Column(length = 50)
    private String callNumber;

    @Column(length = 500)
    private String keywords;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(length = 255)
    private String physicalDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CatalogStatus status = CatalogStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id")
    private Publisher publisher;

    @ManyToMany
    @JoinTable(name = "document_authors",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id"))
    private Set<Author> authors = new LinkedHashSet<>();
}
