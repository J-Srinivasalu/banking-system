package js.project.account_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import js.project.account_service.model.enums.ApprovalStatus;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "account_service_approval")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountApproval {
    
    @Id
    @UuidGenerator
    @Column(name = "approval_id", columnDefinition = "UUID")
    private UUID id;

    @NotNull(message = "Account ID is required")
    @Column(name = "account_id", columnDefinition = "UUID", nullable = false)
    private UUID accountId;

    @NotNull(message = "Approval status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApprovalStatus status;

    @NotNull(message = "Requested by is required")
    @Column(name = "requested_by", columnDefinition = "UUID", nullable = false)
    private UUID requestedBy;

    @Column(name = "reviewed_by", columnDefinition = "UUID")
    private UUID reviewedBy;

    @Size(max = 500, message = "Comments must be less than 500 characters")
    @Column(name = "comments", length = 500)
    private String comments;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

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