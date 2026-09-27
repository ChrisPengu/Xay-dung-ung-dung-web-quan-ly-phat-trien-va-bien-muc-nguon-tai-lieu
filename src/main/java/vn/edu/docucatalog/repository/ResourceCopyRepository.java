package vn.edu.docucatalog.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.docucatalog.domain.CopyStatus;
import vn.edu.docucatalog.domain.ResourceCopy;

import java.util.List;

public interface ResourceCopyRepository extends JpaRepository<ResourceCopy, Long> {
    @EntityGraph(attributePaths = "document")
    List<ResourceCopy> findByDocumentIdOrderByAccessionNumberAsc(Long documentId);
    long countByDocumentId(Long documentId);
    long countByStatus(CopyStatus status);
    boolean existsByAccessionNumberIgnoreCase(String accessionNumber);
    boolean existsByAccessionNumberIgnoreCaseAndIdNot(String accessionNumber, Long id);
}
