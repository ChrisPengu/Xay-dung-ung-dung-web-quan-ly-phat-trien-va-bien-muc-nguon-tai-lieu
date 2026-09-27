package vn.edu.docucatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.docucatalog.domain.Publisher;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    List<Publisher> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByNameIgnoreCase(String name);
}
