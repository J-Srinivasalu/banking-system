package js.project.account_service.controller;

import js.project.account_service.model.Account;
import js.project.account_service.model.AccountApproval;
import js.project.account_service.model.dto.AccountDto;
import js.project.account_service.model.dto.AccountSummaryDto;
import js.project.account_service.model.enums.AccountStatus;
import js.project.account_service.model.enums.AccountType;
import js.project.account_service.model.enums.ApprovalStatus;
import js.project.account_service.model.request.AccountApprovalRequest;
import js.project.account_service.model.request.CreateAccountRequest;
import js.project.account_service.model.request.StatusUpdateRequest;
import js.project.account_service.model.response.GeneralResponse;
import js.project.account_service.service.AccountApprovalService;
import js.project.account_service.service.AccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/accounts")
@AllArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private final AccountApprovalService accountApprovalService;

    @PostMapping
    public ResponseEntity<AccountSummaryDto> createAccount(@RequestHeader("X-User-Id") UUID userId, @Valid @RequestBody CreateAccountRequest request) {
        log.info("Received request to create account for user: {}", userId);
        AccountSummaryDto createdAccount = accountService.createAccount(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @GetMapping
    public ResponseEntity<Page<AccountDto>> getMyAccounts(@RequestHeader("X-User-Id") UUID userId,
                                                           @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
                                                                   Pageable pageable) {
        log.info("Received request to get accounts for user: {}", userId);
        Page<AccountDto> accounts = accountService.getAccountsByUserId(userId, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getMyAccount(@RequestHeader("X-User-Id") UUID userId, @PathVariable UUID accountId) {
        log.info("Received request to get account {} for user: {}", accountId, userId);
        AccountDto account = accountService.getAccountDetails(accountId);
        return ResponseEntity.ok(account);
    }


    // Admin endpoints
    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<AccountDto>> getAccountsByUserId(@PathVariable UUID userId,
                                                                       @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
                                                                               Pageable pageable) {
        log.info("Received request to get all accounts for user {} (admin)", userId);
        Page<AccountDto> accounts = accountService.getAccountsByUserId(userId, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable UUID accountId) {
        log.info("Received request to get account with id: {} (admin)", accountId);
        AccountDto account = accountService.getAccountDetails(accountId);
        return ResponseEntity.ok(account);
    }

    //Todo
//    @PutMapping("/{accountId}")
//    public ResponseEntity<GeneralResponse> updateAccount(@PathVariable UUID accountId, @Valid @RequestBody UpdateAccountRequest request) {
//        log.info("Received request to update account with id: {} (admin)", accountId);
//        GeneralResponse response = accountService.updateAccount(accountId, request);
//        return ResponseEntity.ok(response);
//    }
//
//    @PatchMapping("/{accountId}")
//    public ResponseEntity<GeneralResponse> patchAccount(@PathVariable UUID accountId, @RequestBody Map<String, Object> request) {
//        log.info("Received request to patch account with id: {} (admin)", accountId);
//        GeneralResponse response = accountService.patchAccount(accountId, request);
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<AccountDto>> get(@PathVariable UUID userId,
                                                                @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
                                                                Pageable pageable) {
        log.info("Received request to get all accounts for user {} (admin)", userId);
        Page<AccountDto> accounts = accountService.getAccountsByUserId(userId, pageable);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping
    public ResponseEntity<Page<AccountApproval>> getAccountApprovals(
            @RequestParam(value = "status", required = false) String statusParam, // Comma-separated statuses
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("Received request to get account approvals with status filter: {}, pageable: {}", statusParam, pageable);

        List<ApprovalStatus> statuses = null;
        if (statusParam != null && !statusParam.isEmpty()) {
            try {
                statuses = Arrays.stream(statusParam.split(","))
                        .map(String::trim)
                        .map(ApprovalStatus::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ex) {
                log.warn("Invalid approval status provided: {}", statusParam);
                return ResponseEntity.badRequest().build();
            }
        }


        Page<AccountApproval> approvals = accountApprovalService.getAccountApprovals(statuses, pageable);

        log.info("Found {} account approvals matching the criteria on page {} of {} (total {} approvals).",
                approvals.getNumberOfElements(),
                approvals.getNumber(),
                approvals.getTotalPages(),
                approvals.getTotalElements());

        return ResponseEntity.ok(approvals);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<AccountDto>> searchAccounts(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) AccountType type,
            @RequestParam(required = false) AccountStatus status,
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {

        log.info("Received request to search accounts with filters: " +
                        "username={}, status={}, pageable={}",
                username, status, pageable);

        Page<Account> accountsPage = accountService.searchAccounts(username,type, status, pageable);

        log.info("Found {} accounts matching the search criteria on page {} of {} (total {} accounts).",
                accountsPage.getNumberOfElements(),
                accountsPage.getNumber(),
                accountsPage.getTotalPages(),
                accountsPage.getTotalElements());

        return ResponseEntity.ok(accountsPage.map(AccountDto::new));
    }

    @PutMapping("/{accountId}/approve")
    public ResponseEntity<GeneralResponse> approveAccount(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID accountId,
                                                          @Valid @RequestBody AccountApprovalRequest request) {
        log.info("Received request to approve account with id: {} (admin)", accountId);
        request.setAccountId(accountId);
        GeneralResponse response = accountService.approveAccount(adminId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/freeze")
    public ResponseEntity<GeneralResponse> freezeAccount(@RequestHeader("X-Admin-Id") UUID adminId, @PathVariable UUID accountId,
                                                         @Valid @RequestBody StatusUpdateRequest request) {
        log.info("Received request to freeze account: {}", accountId);
        request.setAccountId(accountId);
        GeneralResponse response = accountService.freezeAccount(adminId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/unfreeze")
    public ResponseEntity<GeneralResponse> unfreezeAccount(@RequestHeader("X-Admin-Id") UUID adminId,
                                                           @Valid @RequestBody StatusUpdateRequest request) {
        log.info("Received request to unfreeze account: {}", request.getAccountId());
        request.setAccountId(request.getAccountId());
        GeneralResponse response = accountService.unfreezeAccount(adminId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{accountId}/close")
    public ResponseEntity<GeneralResponse> closeAccount(@RequestHeader("X-Admin-Id") UUID adminId,
                                                        @Valid @RequestBody StatusUpdateRequest request) {
        log.info("Received request to close account: {}", request.getAccountId());
        request.setAccountId(request.getAccountId());
        GeneralResponse response = accountService.closeAccount(adminId, request);
        return ResponseEntity.ok(response);
    }


}