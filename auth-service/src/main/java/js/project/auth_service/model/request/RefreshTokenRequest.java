package js.project.auth_service.model.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {

    private String refreshToken;
}