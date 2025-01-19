package js.project.auth_service.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordRequest {
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Old Password cannot be empty")
    private String oldPassword;

    @NotEmpty(message = "New Password cannot be empty")
    private String newPassword;

}
