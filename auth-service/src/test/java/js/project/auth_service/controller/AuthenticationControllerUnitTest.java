package js.project.auth_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import js.project.auth_service.exception.AuthenticationException;
import js.project.auth_service.exception.InvalidCredentialsException;
import js.project.auth_service.model.AddressDto;
import js.project.auth_service.model.User;
import js.project.auth_service.model.request.*;
import js.project.auth_service.model.response.AuthenticationResponse;
import js.project.auth_service.model.response.GeneralResponse;
import js.project.auth_service.repository.UserRepository;
import js.project.auth_service.security.SecurityConfig;
import js.project.auth_service.service.AuthenticationService;
import js.project.auth_service.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
public class AuthenticationControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @TestConfiguration
    static class TestConfig {

        @Bean
        UserRepository userRepository() {
            return Mockito.mock(UserRepository.class);
        }
    }

    @Test
    public void registerUser_validInput_returnsCreated() throws Exception {
        // Given
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("StrongPassword123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+1234567890")
                .dateOfBirth("01-01-1990")
                .address(AddressDto.builder()
                        .street("123 Main St")
                        .apartmentSuite("Apt 101")
                        .city("Anytown")
                        .state("CA")
                        .zipCode("12345")
                        .country("USA")
                        .build())
                .nationality("American")
                .nationalId("1234567890")
                .occupation("Software Engineer")
                .build();
        GeneralResponse mockResponse = new GeneralResponse("User registered successfully");
        when(authenticationService.registerUser(any(UserRegisterRequest.class))).thenReturn(mockResponse);

        String jsonRequest = objectMapper.writeValueAsString(request);

        //log.info("object mapper: {}", jsonRequest);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(content().json("{\"message\":\"User registered successfully\"}"));
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));
    }

    @Test
    public void registerUser_invalidInput_returnsBadRequest() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test")
                .password("rd123!")
                .firstName("Test")
                .lastName("User")
                .phoneNumber("+123456")
                .dateOfBirth("01-01-1990")
//                .address(AddressDto.builder()
//                        .street("123 Main St")
//                        .apartmentSuite("Apt 101")
//                        .city("Anytown")
//                        .state("CA")
//                        .zipCode("12345")
//                        .country("USA")
//                        .build())
                .nationality("American")
                .nationalId("1234567890")
                .occupation("Software Engineer")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(result -> {
                    log.info("Response: {}", result.getResponse().getContentAsString());
                })
                .andExpect(status().isBadRequest());

    }

    // Test methods for registerAdmin (similar to registerUser)
    @Test
    public void registerAdmin_validInput_returnsCreated() throws Exception {
        AdminRegisterRequest request = AdminRegisterRequest.builder()
                .firstName("fname")
                .lastName("lname")
                .email("somemail@mail.com")
                .password("StrongPassword@123")
                .build();
        String jwt = "accessToken";
        GeneralResponse mockResponse = new GeneralResponse("Admin registered successfully");
        when(authenticationService.registerAdmin(request)).thenReturn(mockResponse);
        when(jwtService.extractUsername(jwt)).thenReturn("admin@mail.com");
        when(userDetailsService.loadUserByUsername("admin@mail.com")).thenReturn(new User());
        when(jwtService.extractClaim(any(), any())).thenReturn(Optional.of("ADMIN"));


        mockMvc.perform(post("/api/v1/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

    }

    @Test
    public void registerAdmin_invalidInput_returnsBadRequest() throws Exception {
        AdminRegisterRequest request = AdminRegisterRequest.builder()
                .firstName("fname")
                .lastName("lname")
                .email("somemail@mail.com")
                .password("Str")
                .build();
        String jwt = "accessToken";
//        GeneralResponse mockResponse = new GeneralResponse("Admin registered successfully");
//        when(authenticationService.registerAdmin(request)).thenReturn(mockResponse);
        when(jwtService.extractUsername(jwt)).thenReturn("admin@mail.com");
        when(userDetailsService.loadUserByUsername("admin@mail.com")).thenReturn(new User());
        when(jwtService.extractClaim(any(), any())).thenReturn(Optional.of("ADMIN"));


        mockMvc.perform(post("/api/v1/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // Test methods for loginUser
    @Test
    public void loginUser_validCredentials_returnsOk() throws Exception {
        UserLoginRequest request = new UserLoginRequest("somemail@mail.com", "somePassword");

        AuthenticationResponse mockResponse = new AuthenticationResponse("accessToken", "refreshToken");
        when(authenticationService.loginUser(request)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

    }

    @Test
    public void loginUser_invalidCredentials_returnsUnauthorized() throws Exception {
        UserLoginRequest request = new UserLoginRequest("somemail@mail.com", "somePassword");

        when(authenticationService.loginUser(request)).thenThrow(new InvalidCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // Test methods for refreshToken
    @Test
    public void refreshToken_validToken_returnsOk() throws Exception {
        String jwt = "refreshToken";
        RefreshTokenRequest request = new RefreshTokenRequest(jwt);

        AuthenticationResponse mockResponse = new AuthenticationResponse("accessToken", "refreshToken");

        when(jwtService.extractUsername(jwt)).thenReturn("user@mail.com");
        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(new User());
        when(jwtService.extractClaim(any(), any())).thenReturn(Optional.of("USER"));

        when(authenticationService.refreshToken(request)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));

    }

    @Test
    public void refreshToken_invalidToken_returnsUnauthorized() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("refreshToken");

        String jwt = "accessToken";
        when(jwtService.extractUsername(jwt)).thenReturn("user@mail.com");
        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(new User());
        when(jwtService.extractClaim(any(), any())).thenReturn(Optional.of("USER"));

        when(authenticationService.refreshToken(request)).thenThrow(new AuthenticationException("invalid token"));


        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // Test methods for resetPassword
    @Test
    public void resetPassword_validInput_returnsOk() throws Exception {
        String jwt = "refreshToken";
        ResetPasswordRequest request = new ResetPasswordRequest("user@mail.com", "oldPass", "newStrongPass@123");

        GeneralResponse mockResponse = new GeneralResponse("Password reset successful");

        when(jwtService.extractUsername(jwt)).thenReturn("user@mail.com");
        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(new User());
        when(jwtService.extractClaim(any(), any())).thenReturn(Optional.of("USER"));

        when(authenticationService.resetPassword(request)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/v1/auth/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper.writeValueAsString(mockResponse)));
    }

    @Test
    public void resetPassword_invalidInput_returnsBadRequest() throws Exception {
        String jwt = "refreshToken";
        ResetPasswordRequest request = new ResetPasswordRequest("user@mail.com", "oldPass", "newWeakPassword");

        when(jwtService.extractUsername(jwt)).thenReturn("user@mail.com");
        when(userDetailsService.loadUserByUsername("user@mail.com")).thenReturn(new User());
        when(jwtService.extractClaim(any(), any())).thenReturn(Optional.of("USER"));

        mockMvc.perform(post("/api/v1/auth/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}