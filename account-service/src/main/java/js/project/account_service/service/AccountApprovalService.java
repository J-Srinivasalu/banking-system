package js.project.account_service.service;


import jakarta.persistence.criteria.Predicate;
import js.project.account_service.model.AccountApproval;
import js.project.account_service.model.enums.ApprovalStatus;
import js.project.account_service.repository.AccountApprovalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountApprovalService {

    private final AccountApprovalRepository accountApprovalRepository;

    @Transactional(readOnly = true)
    public Page<AccountApproval> getAccountApprovals(List<ApprovalStatus> statuses, Pageable pageable) {
        log.info("Fetching account approvals with statuses: {}, pageable: {}", statuses, pageable);

        Specification<AccountApproval> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<AccountApproval> approvalsPage = accountApprovalRepository.findAll(spec, pageable);

        log.info("Found {} account approvals matching the criteria on page {} of {} (total {} account approvals).",
                approvalsPage.getNumberOfElements(),
                approvalsPage.getNumber(),
                approvalsPage.getTotalPages(),
                approvalsPage.getTotalElements());

        return approvalsPage;
    }
}