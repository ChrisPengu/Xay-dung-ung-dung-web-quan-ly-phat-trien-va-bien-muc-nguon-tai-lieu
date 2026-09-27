package vn.edu.docucatalog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.docucatalog.domain.AcquisitionRequest;
import vn.edu.docucatalog.domain.AcquisitionStatus;
import vn.edu.docucatalog.domain.CatalogStatus;
import vn.edu.docucatalog.domain.CopyStatus;
import vn.edu.docucatalog.domain.ResourceDocument;
import vn.edu.docucatalog.repository.AcquisitionRequestRepository;
import vn.edu.docucatalog.repository.ResourceCopyRepository;
import vn.edu.docucatalog.repository.ResourceDocumentRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {
    private final ResourceDocumentRepository documentRepository;
    private final ResourceCopyRepository copyRepository;
    private final AcquisitionRequestRepository acquisitionRepository;

    public DashboardData getData() {
        return new DashboardData(
                documentRepository.count(),
                documentRepository.countByStatus(CatalogStatus.PUBLISHED),
                copyRepository.count(),
                copyRepository.countByStatus(CopyStatus.AVAILABLE),
                acquisitionRepository.countByStatus(AcquisitionStatus.PENDING),
                acquisitionRepository.sumAmountByStatus(AcquisitionStatus.COMPLETED),
                documentRepository.findTop5ByOrderByCreatedAtDesc(),
                acquisitionRepository.findTop5ByOrderByCreatedAtDesc());
    }

    public record DashboardData(long totalDocuments, long publishedDocuments, long totalCopies,
                                long availableCopies, long pendingRequests, BigDecimal completedBudget,
                                List<ResourceDocument> recentDocuments,
                                List<AcquisitionRequest> recentRequests) { }
}
