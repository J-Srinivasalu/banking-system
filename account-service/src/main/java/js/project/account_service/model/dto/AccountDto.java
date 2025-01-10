package js.project.account_service.model.dto;

import js.project.account_service.model.enums.AccountStatus;
import js.project.account_service.model.enums.AccountType;
import js.project.account_service.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private UUID id;
    private String accountNumber;
    private UUID userId;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public AccountDto(Account account) {
        this.id = account.getId();
        this.userId = account.getUserId();
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType();
        this.balance = account.getBalance();
        this.currency = account.getCurrency();
        this.createdAt = account.getCreatedAt();
        this.updatedAt = account.getUpdatedAt();
        this.status = account.getStatus();
    }
}