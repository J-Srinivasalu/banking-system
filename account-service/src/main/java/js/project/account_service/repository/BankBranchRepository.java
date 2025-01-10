package js.project.account_service.repository;

import js.project.account_service.model.BankBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankBranchRepository extends JpaRepository<BankBranch, UUID> {
    Optional<BankBranch> findByBranchCode(String branchCode);
}