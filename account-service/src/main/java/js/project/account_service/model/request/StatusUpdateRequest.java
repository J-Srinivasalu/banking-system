package js.project.account_service.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "Account type is required")
    UUID accountId;

    @NotNull(message = "Account type is required")
    @Size(max = 500, message = "Comments must be less than 500 characters")
    String reason;

}
