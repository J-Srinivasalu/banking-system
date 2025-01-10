package js.project.account_service.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import js.project.account_service.model.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountApprovalRequest {

    @NotNull(message = "Account approval ID is required")
    private UUID approvalId;
    @NotNull(message = "Account ID is required")
    private UUID accountId;
    @NotNull(message = "Approval status is required")
    private ApprovalStatus status;

    @Size(max = 500, message = "Comments must be less than 500 characters")
    private String comments;
}