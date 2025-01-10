package js.project.account_service.model.dto;

import js.project.account_service.model.Account;
import js.project.account_service.model.enums.AccountStatus;
import js.project.account_service.model.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummaryDto {
    private UUID id;
    private String accountNumber;
    private AccountType accountType;
    private String currency;
    private AccountStatus status;
    private LocalDateTime createdAt;

    public AccountSummaryDto(Account account) {
        this.id = account.getId();
        this.accountNumber = account.getAccountNumber();
        this.accountType = account.getAccountType();
        this.currency = account.getCurrency();
        this.createdAt = account.getCreatedAt();
        this.status = account.getStatus();
    }

}