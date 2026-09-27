package vn.edu.docucatalog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.edu.docucatalog.repository.AcquisitionRequestRepository;
import vn.edu.docucatalog.repository.ResourceCopyRepository;
import vn.edu.docucatalog.repository.ResourceDocumentRepository;
import vn.edu.docucatalog.repository.UserAccountRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("demo")
class DemoProfileIntegrationTest {

    @Autowired private UserAccountRepository userRepository;
    @Autowired private ResourceDocumentRepository documentRepository;
    @Autowired private ResourceCopyRepository copyRepository;
    @Autowired private AcquisitionRequestRepository acquisitionRepository;

    @Test
    void demoProfileStartsAndLoadsSampleData() {
        assertThat(userRepository.count()).isEqualTo(3);
        assertThat(userRepository.findByUsernameIgnoreCase("admin")).isPresent();
        assertThat(documentRepository.count()).isEqualTo(5);
        assertThat(copyRepository.count()).isEqualTo(4);
        assertThat(acquisitionRepository.count()).isEqualTo(2);
    }
}
