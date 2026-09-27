package vn.edu.docucatalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import vn.edu.docucatalog.domain.CatalogStatus;
import vn.edu.docucatalog.domain.ResourceDocument;

import java.util.List;
import java.util.Optional;

public interface ResourceDocumentRepository extends JpaRepository<ResourceDocument, Long>, JpaSpecificationExecutor<ResourceDocument> {
    boolean existsByCatalogCodeIgnoreCase(String catalogCode);
    boolean existsByCatalogCodeIgnoreCaseAndIdNot(String catalogCode, Long id);
    boolean existsByIsbnAndIdNot(String isbn, Long id);
    boolean existsByIsbn(String isbn);
    long countByStatus(CatalogStatus status);

    @Override
    @EntityGraph(attributePaths = {"category", "publisher", "authors"})
    Optional<ResourceDocument> findById(Long id);

    @Override
    @EntityGraph(attributePaths = {"category", "publisher"})
    Page<ResourceDocument> findAll(Specification<ResourceDocument> specification, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "publisher"})
    List<ResourceDocument> findTop5ByOrderByCreatedAtDesc();

    @Query("select d from ResourceDocument d order by d.title")
    List<ResourceDocument> findAllOrdered();
}
