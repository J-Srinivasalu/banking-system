package js.project.auth_service.model.request;

import jakarta.validation.constraints.NotEmpty;
import js.project.auth_service.validation.ValidPassword;
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
    @ValidPassword(message = "Password must be 8-20 characters long and contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String newPassword;

}
