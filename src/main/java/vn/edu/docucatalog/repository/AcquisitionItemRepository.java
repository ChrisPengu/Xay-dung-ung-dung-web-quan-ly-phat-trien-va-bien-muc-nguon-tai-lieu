package vn.edu.docucatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.docucatalog.domain.AcquisitionItem;

public interface AcquisitionItemRepository extends JpaRepository<AcquisitionItem, Long> {
    boolean existsByDocumentId(Long documentId);
}
