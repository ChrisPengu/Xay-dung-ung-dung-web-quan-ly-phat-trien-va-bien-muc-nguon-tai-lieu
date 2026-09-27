package vn.edu.docucatalog.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.docucatalog.domain.AcquisitionRequest;
import vn.edu.docucatalog.domain.AcquisitionStatus;

import java.math.BigDecimal;
import java.util.Optional;

public interface AcquisitionRequestRepository extends JpaRepository<AcquisitionRequest, Long> {
    @EntityGraph(attributePaths = {"supplier", "items", "items.document"})
    @Query("select distinct r from AcquisitionRequest r where r.id = :id")
    Optional<AcquisitionRequest> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = "supplier")
    Page<AcquisitionRequest> findByStatus(AcquisitionStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "supplier")
    Page<AcquisitionRequest> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "supplier")
    Page<AcquisitionRequest> findByTitleContainingIgnoreCaseOrRequestCodeContainingIgnoreCase(
            String title, String requestCode, Pageable pageable);

    @EntityGraph(attributePaths = "supplier")
    @Query("select r from AcquisitionRequest r where " +
            "(:keyword = '' or lower(r.title) like lower(concat('%', :keyword, '%')) " +
            "or lower(r.requestCode) like lower(concat('%', :keyword, '%'))) " +
            "and (:status is null or r.status = :status)")
    Page<AcquisitionRequest> search(@Param("keyword") String keyword,
                                    @Param("status") AcquisitionStatus status,
                                    Pageable pageable);

    long countByStatus(AcquisitionStatus status);

    @EntityGraph(attributePaths = "supplier")
    java.util.List<AcquisitionRequest> findTop5ByOrderByCreatedAtDesc();

    @Query("select coalesce(sum(i.unitPrice * i.quantity), 0) from AcquisitionItem i where i.request.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") AcquisitionStatus status);
}
