package vn.edu.docucatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.docucatalog.domain.Supplier;

import java.util.List;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findAllByOrderByNameAsc();
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    boolean existsByNameIgnoreCase(String name);
}
