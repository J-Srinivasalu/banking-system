package js.project.account_service.model.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import js.project.account_service.model.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotBlank(message = "Branch code is required")
    @Size(min = 4, max = 4, message = "Branch code must be 4 characters")
    private String branchCode;

    @NotNull(message = "Balance is required")
    @PositiveOrZero(message = "Balance cannot be negative")
    private BigDecimal balance;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currency;
}