package vn.edu.docucatalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.docucatalog.domain.UserAccount;

import java.util.Optional;
import java.util.List;
import vn.edu.docucatalog.domain.UserRole;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByUsernameIgnoreCase(String username);
    List<UserAccount> findAllByOrderByFullNameAsc();
    boolean existsByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    long countByRoleAndActiveTrue(UserRole role);
}
