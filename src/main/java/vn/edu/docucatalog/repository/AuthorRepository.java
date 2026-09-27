package vn.edu.docucatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.docucatalog.domain.Author;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    List<Author> findAllByOrderByNameAsc();
}
