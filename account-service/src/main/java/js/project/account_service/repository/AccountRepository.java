package js.project.account_service.repository;

import js.project.account_service.model.Account;
import js.project.account_service.model.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {
    @Query(value = "SELECT nextval('account_serial_number_sequence')", nativeQuery = true)
    Long getNextAccountNumberSequence();
    Page<Account> findByUserId(UUID userId, Pageable pageable);
    Page<Account> findByStatus(AccountStatus status, Pageable pageable);

}