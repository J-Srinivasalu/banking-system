package js.project.auth_service.service;

import jakarta.transaction.Transactional;
import js.project.auth_service.exception.*;
import js.project.auth_service.model.UserCreatedEvent;
import js.project.auth_service.model.request.*;
import js.project.auth_service.model.response.GeneralResponse;
import js.project.auth_service.repository.UserRepository;
import js.project.auth_service.model.Role;
import js.project.auth_service.model.User;
import js.project.auth_service.model.response.AuthenticationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;

    @Transactional
    public GeneralResponse registerAdmin(AdminRegisterRequest request) {
        log.info("Registering admin with email: {}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Email already taken: {}", request.getEmail());
            throw new EmailAlreadyTakenException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .build();

        try {
            userRepository.save(user);
            log.info("Admin registered successfully with email: {}", request.getEmail());
            return new GeneralResponse("Admin registered successfully");
        } catch (DataIntegrityViolationException e) {
            log.error("Error registering admin: {}", e.getMessage(), e);
            throw new UserRegistrationException("Error registering admin: " + e.getMessage());
        }
    }

    @Transactional
    public GeneralResponse registerUser(UserRegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Email already taken: {}", request.getEmail());
            throw new EmailAlreadyTakenException(request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        try {
            user = userRepository.save(user);

            UserCreatedEvent userCreatedEvent = UserCreatedEvent.builder()
                    .userId(user.getId())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .dateOfBirth(LocalDate.parse(request.getDateOfBirth(), DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                    .address(request.getAddress())
                    .nationality(request.getNationality())
                    .occupation(request.getOccupation())
                    .nationalId(request.getNationalId())
                    .build();

//            kafkaTemplate.send("user-create", userCreatedEvent);
            log.info("user: {}", userCreatedEvent.toString());
            log.info("User registered successfully and event sent to Kafka: {}", request.getEmail());
            return new GeneralResponse("User registered successfully");
        } catch (DataIntegrityViolationException e) {
            log.error("Error registering user: {}", e.getMessage(), e);
            throw new UserRegistrationException("Error registering user: " + e.getMessage());
        }
    }

    public AuthenticationResponse loginUser(UserLoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        log.warn("Invalid password for email: {}", request.getEmail());
                        throw new InvalidCredentialsException("Invalid email or password");
                    }
                    String accessToken = jwtService.generateToken(user);
                    String refreshToken = jwtService.generateRefreshToken(user);
                    log.info("User logged in successfully: {}", request.getEmail());
                    return new AuthenticationResponse(accessToken, refreshToken);
                })
                .orElseThrow(() -> {
                    log.warn("User not found: {}", request.getEmail());
                    throw new UserNotFoundException("User not found with email: " + request.getEmail());
                });
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");
        String email = jwtService.extractUsername(request.getRefreshToken());
        if (email == null) {
            log.warn("Invalid refresh token");
            throw new AuthenticationException("Invalid token");
        }
        return userRepository.findByEmail(email)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    String refreshToken = jwtService.generateRefreshToken(user);
                    log.info("Token refreshed for user: {}", email);
                    return new AuthenticationResponse(accessToken, refreshToken);
                })
                .orElseThrow(() -> {
                    log.warn("User not found for refresh token: {}", email);
                    throw new UserNotFoundException("User not found with email: " + email);
                });
    }

    public GeneralResponse resetPassword(ResetPasswordRequest request) {
        log.info("Password reset request for email: {}", request.getEmail());
        return userRepository.findByEmail(request.getEmail())
                .map(user -> {
                    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                        log.warn("Incorrect old password for email: {}", request.getEmail());
                        throw new InvalidCredentialsException("Invalid old password");
                    }
                    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                    userRepository.save(user);
                    log.info("Password reset successful for email: {}", request.getEmail());
                    return new GeneralResponse("Password reset successful");
                })
                .orElseThrow(() -> {
                    log.warn("User not found for password reset: {}", request.getEmail());
                    throw new UserNotFoundException("User not found with email: " + request.getEmail());
                });
    }
}