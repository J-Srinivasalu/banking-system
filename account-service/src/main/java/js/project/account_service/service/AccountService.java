package js.project.account_service.service;

import jakarta.persistence.criteria.Predicate;
import js.project.account_service.client.UserServiceClient;
import js.project.account_service.exception.*;
import js.project.account_service.exception.InvalidAccountStatusException;
import js.project.account_service.model.Account;
import js.project.account_service.model.AccountApproval;
import js.project.account_service.model.BankBranch;
import js.project.account_service.model.enums.AccountStatus;
import js.project.account_service.model.dto.AccountDto;
import js.project.account_service.model.dto.AccountSummaryDto;
import js.project.account_service.model.enums.AccountType;
import js.project.account_service.model.enums.ApprovalStatus;
import js.project.account_service.model.request.AccountApprovalRequest;
import js.project.account_service.model.request.CreateAccountRequest;
import js.project.account_service.model.request.StatusUpdateRequest;
import js.project.account_service.model.response.GeneralResponse;
import js.project.account_service.repository.AccountApprovalRepository;
import js.project.account_service.repository.AccountRepository;
import js.project.account_service.repository.BankBranchRepository;
import js.project.account_service.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountApprovalRepository accountApprovalRepository;
    private final BankBranchRepository bankBranchRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final UserServiceClient userServiceClient;
//    private final ObjectMapper objectMapper;
    @Transactional
    public AccountSummaryDto createAccount(UUID userId, CreateAccountRequest request) {
        log.info("Creating account for user with ID: {} and branchId: {}", userId, request.getBranchCode());

        BankBranch branch = bankBranchRepository.findByBranchCode(request.getBranchCode())
                .orElseThrow(() -> new BranchNotFoundException("Branch not found with branch code: " + request.getBranchCode()));

        Long sequentialNumber = accountRepository.getNextAccountNumberSequence();
        String accountNumber = accountNumberGenerator.generateAccountNumber(branch.getBranchCode(), sequentialNumber);


        Account account = Account.builder()
                .userId(userId)
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getBalance())
                .currency(request.getCurrency())
                .bankBranch(branch)
                .lastModifiedBy(userId)
                .status(AccountStatus.PENDING_APPROVAL)
                .build();

        accountRepository.save(account);

        AccountApproval accountApproval = AccountApproval.builder()
                .accountId(account.getId())
                .status(ApprovalStatus.PENDING)
                .requestedBy(userId)
                .build();

        accountApprovalRepository.save(accountApproval);
        log.info("Account approval request created: {}", accountApproval);

        return new AccountSummaryDto(account);
    }

    @Transactional
    public GeneralResponse approveAccount(UUID reviewedBy, AccountApprovalRequest request) {
        log.info("Approving account with ID: {}", request.getAccountId());

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + request.getAccountId()));

        AccountApproval accountApproval = accountApprovalRepository.findById(request.getApprovalId())
                .orElseThrow(() ->
                        new AccountApprovalNotFoundException("Account Approval not found for account approval id: " + request.getApprovalId()));

        if(accountApproval.getStatus().equals(ApprovalStatus.APPROVED)
                || accountApproval.getStatus().equals(ApprovalStatus.REJECTED)){
            throw new AccountAlreadyReviewedException("Account already reviewed");
        }

        accountApproval.setStatus(request.getStatus());
        accountApproval.setComments(request.getComments());
        accountApproval.setReviewedBy(reviewedBy);
        accountApproval.setReviewedAt(LocalDateTime.now());
        accountApproval.setUpdatedAt(LocalDateTime.now());
        accountApprovalRepository.save(accountApproval);


        switch (request.getStatus()) {
            case APPROVED -> {
                account.setStatus(AccountStatus.ACTIVE);
                accountRepository.save(account);
                log.info("Account with ID {} activated successfully.", request.getAccountId());
                return new GeneralResponse("Account approved successfully");
            }
            case REJECTED -> {
                account.setStatus(AccountStatus.REJECTED);
                accountRepository.save(account);
                log.info("Account with ID {} rejected successfully.", request.getAccountId());
                return new GeneralResponse("Account rejected.");
            }
            case PENDING -> {
                log.info("Account approval status set back to pending for account id: {}", request.getAccountId());
                return new GeneralResponse("Account approval status set back to pending.");
            }
            default -> {
                log.warn("Invalid approval status provided: {}", request.getStatus());
                throw new InvalidApprovalStatusException("Invalid approval status provided: " + request.getStatus());
            }
        }
    }

    @Transactional
    public GeneralResponse freezeAccount(UUID adminId, StatusUpdateRequest request) {
        log.info("Freezing account: {}", request.getAccountId());
        
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + request.getAccountId()));
        
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new InvalidAccountStatusException("Only active accounts can be frozen");
        }

        account.setStatus(AccountStatus.FROZEN);
        account.setStatusUpdateComment(request.getReason());
        account.setLastModifiedBy(adminId);
        
        Account savedAccount = accountRepository.save(account);
//        notificationService.notifyUserAccountFrozen(savedAccount);

        return new GeneralResponse("Account frozen successfully");
    }

    @Transactional
    public GeneralResponse unfreezeAccount(UUID adminId, StatusUpdateRequest request) {
        log.info("Unfreezing account: {}", request.getAccountId());
        
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + request.getAccountId()));
        
        if (account.getStatus() != AccountStatus.FROZEN) {
            throw new InvalidAccountStatusException("Only frozen accounts can be unfrozen");
        }

        account.setStatus(AccountStatus.ACTIVE);
        account.setStatusUpdateComment(request.getReason());
        account.setLastModifiedBy(adminId);
        
        Account savedAccount = accountRepository.save(account);
//        notificationService.notifyUserAccountUnfrozen(savedAccount);

        return new GeneralResponse("Account unfrozen successfully");
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAccountsByUserId(UUID userId, Pageable pageable) {
        log.info("Fetching accounts summary for user: {}", userId);
        Page<Account> accountsPage = accountRepository.findByUserId(userId, pageable);
        return accountsPage.map(AccountDto::new);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAccountsByStatus(AccountStatus status, Pageable pageable) {
        log.info("Fetching accounts with status: {}", status);
        Page<Account> accountsPage = accountRepository.findByStatus(status, pageable);
        return accountsPage.map(AccountDto::new);
    }
    @Transactional(readOnly = true)
    public AccountDto getAccountDetails(UUID accountId) {
        log.info("Fetching detailed account information for admin: {}", accountId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
        return new AccountDto(account);
    }

    //Todo
//    @Transactional
//    public GeneralResponse updateAccount(UUID accountId, UpdateAccountRequest request, UUID adminId) {
//        log.info("Updating account: {}", accountId);
//
//        Account account = accountRepository.findById(accountId)
//                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + accountId));
//
//        if (account.getStatus() == AccountStatus.CLOSED) {
//            throw new InvalidAccountStatusException("Cannot update closed account");
//        }
//
//        updateAccountFromRequest(account, request, adminId);
//        Account savedAccount = accountRepository.save(account);
//
//        if (request.getStatus() != null && request.getStatus() != account.getStatus()) {
//            notificationService.notifyUserAccountStatusChange(savedAccount);
//        }
//
//        return new GeneralResponse("Account updated successfully");
//    }

//    private void updateAccountFromRequest(Account account, UpdateAccountRequest request, UUID adminId) {
//        if (request.getAccountType() != null) {
//            account.setAccountType(request.getAccountType());
//        }
//        if (request.getCurrency() != null) {
//            // Only allow currency change if balance is zero
//            if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
//                throw new IllegalStateException("Cannot change currency for account with non-zero balance");
//            }
//            account.setCurrency(request.getCurrency());
//        }
//        if (request.getStatus() != null) {
//            account.setStatus(request.getStatus());
//        }
//
//        account.setLastModifiedBy(adminId);
//        account.setUpdatedAt(LocalDateTime.now());
//    }

    @Transactional
    public GeneralResponse closeAccount(UUID adminId, StatusUpdateRequest request) {
        log.info("Closing account: {}", request.getAccountId());
        
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException("Account not found with ID: " + request.getAccountId()));
        
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close account with non-zero balance");
        }

        account.setStatus(AccountStatus.CLOSED);
        account.setStatusUpdateComment(request.getReason());
        account.setLastModifiedBy(adminId);
        
        Account savedAccount = accountRepository.save(account);
//        notificationService.notifyUserAccountClosed(savedAccount);

        return new GeneralResponse("Account closed successfully");
    }

    public Page<Account> searchAccounts(String username, AccountType type, AccountStatus status, Pageable pageable) {

        List<UUID> userIds = getUserIdsFromUserService(username);

        if(userIds.isEmpty()){
            return Page.empty();
        }

        Specification<Account> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(!userIds.isEmpty()){
                predicates.add(root.get("user_id").in(userIds));
            }
            if(type != null){
                predicates.add(criteriaBuilder.equal(root.get("account_type"),type));
            }
            if(status != null){
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return accountRepository.findAll(specification, pageable);

    }

    public List<UUID> getUserIdsFromUserService(String username){
        try {
            List<UUID> userResponses = userServiceClient.searchUsers(username);
            if(userResponses != null){
                return userResponses;
            }
            else {
                log.warn("User service returned null response.");
                return List.of();
            }
        } catch (Exception ex) {
            log.error("Error calling user service:", ex);
            return List.of();
        }
    }

}