package js.project.auth_service.service;

import js.project.auth_service.exception.*;
import js.project.auth_service.model.AddressDto;
import js.project.auth_service.model.Role;
import js.project.auth_service.model.User;
import js.project.auth_service.model.request.*;
import js.project.auth_service.model.response.AuthenticationResponse;
import js.project.auth_service.model.response.GeneralResponse;
import js.project.auth_service.repository.UserRepository;
import js.project.model.Address;
import js.project.model.UserCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void registerAdmin_validInput_success() {
        // Given
        String fName = "john";
        String lName = "dove";
        String email = "admin@example.com";
        String password = "password123";
        AdminRegisterRequest request = new AdminRegisterRequest(fName, lName, email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        //when
        GeneralResponse response = authenticationService.registerAdmin(request);

        //then
        assertThat(response.message()).isEqualTo("Admin registered successfully");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void registerAdmin_emailAlreadyTaken_throwsEmailAlreadyTakenException() {
        // Arrange
        String existingEmail = "admin@example.com";
        AdminRegisterRequest request = new AdminRegisterRequest("john", "doe", existingEmail, "password");

        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authenticationService.registerAdmin(request))
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessage("Email is already taken: " + existingEmail);
    }

    @Test
    void registerAdmin_emailAlreadyTaken_throwsEmailAlreadyTakenExceptionBDD() {
        // Arrange
        String existingEmail = "admin@example.com";
        AdminRegisterRequest request = new AdminRegisterRequest("john", "doe", existingEmail, "password");

        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(new User()));

        // When (Act)
        Throwable thrown = catchThrowable(() -> authenticationService.registerAdmin(request));

        // Then (Assert)
        assertThat(thrown)
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessage("Email is already taken: " + existingEmail);
    }

    @Test
    void registerAdmin_dataIntegrityViolation_throwsUserRegistrationException() {
        // Given
        String email = "admin@example.com";
        String password = "password123";
        AdminRegisterRequest request = new AdminRegisterRequest("john", "doe", email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);

        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Simulated database error"));

        // When (Act) & Then (Assert)
        assertThatThrownBy(() -> authenticationService.registerAdmin(request))
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Error registering admin: Simulated database error");

//        //when
//        Throwable thrown = catchThrowable(() -> authenticationService.registerAdmin(request));
//        //then
//        assertThat(thrown)
//                .isInstanceOf(UserRegistrationException.class)
//                .hasMessage("Error registering admin: Simulated database error");
    }

    @Test
    void registerUser_validInput_success() {
        // Given (Arrange)
        String email = "test@example.com";
        String password = "password";
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email(email)
                .password(password)
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .dateOfBirth("10-10-1990")
                .address(new AddressDto("street", "apartmentSuite", "city", "state", "zipCode", "country"))
                .nationality("test nationality")
                .occupation("test occupation")
                .nationalId("test national id")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        User savedUser = User.builder().id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000")).email(email).password("encodedPassword").role(Role.USER).build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);


        // When (Act)
        GeneralResponse response = authenticationService.registerUser(request);

        // Then (Assert)
        assertThat(response.message()).isEqualTo("User registered successfully");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());
//        verify(userRepository).save(userCaptor.capture()); //other way
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo(email);
        assertThat(capturedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(capturedUser.getRole()).isEqualTo(Role.USER);

        ArgumentCaptor<UserCreatedEvent> eventCaptor = ArgumentCaptor.forClass(UserCreatedEvent.class);
        verify(kafkaTemplate).send(eq("user-created"), eventCaptor.capture());
        UserCreatedEvent sentEvent = eventCaptor.getValue();
        assertThat(sentEvent.getEmail()).isEqualTo(email);
        assertThat(sentEvent.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(sentEvent.getLastName()).isEqualTo(request.getLastName());
        assertThat(sentEvent.getPhoneNumber()).isEqualTo(request.getPhoneNumber());
        assertThat(sentEvent.getDateOfBirth()).isEqualTo(LocalDate.parse(request.getDateOfBirth(), DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        assertThat(sentEvent.getNationality()).isEqualTo(request.getNationality());
        assertThat(sentEvent.getOccupation()).isEqualTo(request.getOccupation());
        assertThat(sentEvent.getNationalId()).isEqualTo(request.getNationalId());
        assertThat(sentEvent.getUserId()).isEqualTo(savedUser.getId());
        assertThat(sentEvent.getAddress()).usingRecursiveComparison().isEqualTo(Address.builder()
                .street("street")
                .city("city")
                .state("state")
                .zipCode("zipCode")
                .country("country")
                .apartmentSuite("apartmentSuite")
                .build());
    }

    @Test
    void registerUser_emailAlreadyTaken_throwsEmailAlreadyTakenException() {
        //given
        String existingEmail = "test@example.com";
        String password = "password";
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email(existingEmail)
                .password(password)
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .dateOfBirth("10-10-1990")
                .address(new AddressDto("street", "city", "state", "zipCode", "country", "apartmentSuite"))
                .nationality("test nationality")
                .occupation("test occupation")
                .nationalId("test national id")
                .build();

        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(new User()));

        //when
        Throwable thrown = catchThrowable(() -> authenticationService.registerUser(request));

        //then
        assertThat(thrown)
                .isInstanceOf(EmailAlreadyTakenException.class)
                .hasMessage("Email is already taken: " + existingEmail);

    }

    @Test
    void registerUser_dataIntegrityViolation_throwsUserRegistrationException() {
        //given
        String email = "test@example.com";
        String password = "password";
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email(email)
                .password(password)
                .firstName("Test")
                .lastName("User")
                .phoneNumber("1234567890")
                .dateOfBirth("10-10-1990")
                .address(new AddressDto("street", "apartmentSuite", "city", "state", "zipCode", "country"))
                .nationality("test nationality")
                .occupation("test occupation")
                .nationalId("test national id")
                .build();

        given(userRepository.findByEmail(email)).willReturn(Optional.empty()); // BDD way
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Simulated database error"));

        //when
        Throwable thrown = catchThrowable(() -> authenticationService.registerUser(request));

        assertThat(thrown)
                .isInstanceOf(UserRegistrationException.class)
                .hasMessage("Error registering user: Simulated database error");
    }

    @Test
    void loginUser_validCredentials_returnsAuthenticationResponse() {
        // Given (Arrange)
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        UserLoginRequest request = new UserLoginRequest(email, password);
        User user = User.builder().email(email).password(encodedPassword).role(Role.USER).build();
        String accessToken = "testAccessToken";
        String refreshToken = "testRefreshToken";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(accessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(refreshToken);

        // When (Act)
        AuthenticationResponse response = authenticationService.loginUser(request);

        // Then (Assert)
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
    }

    @Test
    void loginUser_userNotFound_throwsUserNotFoundException() {
        // Given (Arrange)
        String email = "test@example.com";
        String password = "password";
        UserLoginRequest request = new UserLoginRequest(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when
        Throwable thrown = catchThrowable(() -> authenticationService.loginUser(request));

        //then
        assertThat(thrown)
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: " + email);

    }

    @Test
    void loginUser_invalidPassword_throwsInvalidCredentialsException() {
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        UserLoginRequest request = new UserLoginRequest(email, password);
        User user = User.builder().email(email).password(encodedPassword).role(Role.USER).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        //when
        Throwable thrown = catchThrowable(() -> authenticationService.loginUser(request));

        //then
        assertThat(thrown)
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");
    }

    @Test
    void refreshToken_validRefreshToken_returnsAuthenticationResponse() {
        //given
        String refreshToken = "refreshToken";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        String email = "mail@mail.com";
        User user = User.builder().email(email).password("encodedPassword").role(Role.USER).build();
        String newAccessToken = "testAccessToken";
        String newRefreshToken = "testRefreshToken";

        when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(newAccessToken);
        when(jwtService.generateRefreshToken(user)).thenReturn(newRefreshToken);

        //when
        AuthenticationResponse response = authenticationService.refreshToken(request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(response.getRefreshToken()).isEqualTo(newRefreshToken);
    }

    @Test
    void refreshToken_invalidRefreshToken_throwsAuthenticationException() { // Given
        String refreshToken = "invalidRefreshToken";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        when(jwtService.extractUsername(refreshToken)).thenReturn(null); // Simulate invalid token

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("Invalid token");
    }

    @Test
    void refreshToken_userNotFound_throwsUserNotFoundException() {
        // Given
        String refreshToken = "validRefreshToken";
        String email = "nonexistent@example.com";
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

        when(jwtService.extractUsername(refreshToken)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.refreshToken(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: " + email);
    }

    @Test
    void resetPassword_validInput_success() {
        //given
        String email = "mail@mail.com";
        String oldPassword = "oldPassword";
        String encodedOldPassword = "encodedOldPassword";
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";
        ResetPasswordRequest request = new ResetPasswordRequest(email, oldPassword, newPassword);
        User user = User.builder().email(email).password(encodedOldPassword).role(Role.USER).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword,encodedOldPassword)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        //when
        GeneralResponse response = authenticationService.resetPassword(request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.message()).isEqualTo("Password reset successful");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        // then(userRepository).should().save(userCaptor.capture());
        verify(userRepository).save(userCaptor.capture());

        User updatedUser = userCaptor.getValue();
        assertThat(updatedUser.getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    void resetPassword_invalidOldPassword_throwsInvalidCredentialsException() {
        //given
        String email = "mail@mail.com";
        String oldPassword = "oldPassword";
        String encodedOldPassword = "encodedOldPassword";
        String newPassword = "newPassword";
        ResetPasswordRequest request = new ResetPasswordRequest(email, oldPassword, newPassword);
        User user = User.builder().email(email).password(encodedOldPassword).role(Role.USER).build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword,encodedOldPassword)).thenReturn(false);

        //when & then
        assertThatThrownBy(() -> authenticationService.resetPassword(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid old password");
    }

    @Test
    void resetPassword_userNotFound_throwsUserNotFoundException() {
        //given
        String email = "mail@mail.com";
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        ResetPasswordRequest request = new ResetPasswordRequest(email, oldPassword, newPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when & then
        assertThatThrownBy(() -> authenticationService.resetPassword(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: " + email);
    }
}