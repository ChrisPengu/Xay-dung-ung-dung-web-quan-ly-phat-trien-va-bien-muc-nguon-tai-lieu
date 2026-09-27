package vn.edu.docucatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.docucatalog.domain.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByOrderByNameAsc();
    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);
    boolean existsByCodeIgnoreCase(String code);
}
