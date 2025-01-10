package js.project.account_service.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import js.project.account_service.model.enums.AccountStatus;
import js.project.account_service.model.enums.AccountType;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_service_account")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @UuidGenerator
    @Column(name = "account_id", columnDefinition = "UUID")
    private UUID id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", columnDefinition = "UUID", nullable = false)
    private UUID userId;


    @Column(name = "account_number", nullable = false, unique = true, length = 12)
    private String accountNumber;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Valid
    @NotNull(message = "Branch is required")
    private BankBranch bankBranch;

    @NotNull(message = "Balance is required")
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "last_modified_by", columnDefinition = "UUID")
    private UUID lastModifiedBy;

    @Size(max = 500, message = "Comments must be less than 500 characters")
    @Column(name = "status_update_comment", length = 500)
    private String statusUpdateComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}