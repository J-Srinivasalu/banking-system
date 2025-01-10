package js.project.auth_service.model.request;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;

@Data
public class UserLoginRequest {

    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Password cannot be empty")
    private String password;
}