package js.project.auth_service.controller;

import jakarta.validation.Valid;
import js.project.auth_service.model.ObjectTest;
import js.project.auth_service.model.request.*;
import js.project.auth_service.model.response.GeneralResponse;
import js.project.auth_service.model.response.AuthenticationResponse;
import js.project.auth_service.service.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Validated
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, ObjectTest> objectKafkaTemplate;

    @PostMapping("/register")
    public ResponseEntity<GeneralResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        log.info("Received user registration request for email: {}", request.getEmail());
        GeneralResponse response = authenticationService.registerUser(request);
        log.info("User registered successfully with email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<GeneralResponse> registerAdmin(@Valid @RequestBody AdminRegisterRequest request) {
        log.info("Received admin registration request for email: {}", request.getEmail());
        GeneralResponse response = authenticationService.registerAdmin(request);
        log.info("Admin registered successfully with email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@Valid @RequestBody UserLoginRequest request) {
        log.info("Received login request for email: {}", request.getEmail());
        AuthenticationResponse response = authenticationService.loginUser(request);
        log.info("User logged in successfully: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received refresh token request");
        AuthenticationResponse response = authenticationService.refreshToken(request);
        log.info("Token refreshed successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/reset")
    public ResponseEntity<GeneralResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        log.info("Received password reset request for email: {}", request.getEmail());
        GeneralResponse response = authenticationService.resetPassword(request);
        log.info("Password reset successful for email: {}", request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/kafka-string-test")
    public ResponseEntity<GeneralResponse> kafkaStringTest(@RequestBody  RefreshTokenRequest request) {
        log.info("producer kafka message: {}", request.getRefreshToken());
        stringKafkaTemplate.send("string-event",request.getRefreshToken());
        log.info("producer kafka message: {} sent", request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/kafka-object-test")
    public ResponseEntity<GeneralResponse> kafkaObjectTest(@RequestBody ObjectTest request) {
        log.info("producer kafka object: {}", request);
        objectKafkaTemplate.send("object-event",request);
        log.info("producer kafka object: {} sent", request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
