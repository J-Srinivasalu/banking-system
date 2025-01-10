package js.project.account_service.repository;

import js.project.account_service.model.AccountApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountApprovalRepository extends JpaRepository<AccountApproval, UUID>, JpaSpecificationExecutor<AccountApproval> {

}