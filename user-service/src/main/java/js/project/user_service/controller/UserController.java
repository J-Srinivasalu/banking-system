package js.project.user_service.controller;

import jakarta.validation.Valid;
import js.project.user_service.model.request.UpdateUserRequest;
import js.project.user_service.model.response.GeneralResponse;
import js.project.user_service.model.dto.UserDto;
import js.project.user_service.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    // Endpoints for the currently authenticated user (/me)
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@RequestHeader("X-User-Id") UUID userId) {
        log.info("Received request to get current user");
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<GeneralResponse> updateCurrentUser(@RequestHeader("X-User-Id") UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        log.info("Received request to update current user");
        GeneralResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<GeneralResponse> patchCurrentUser(@RequestHeader("X-User-Id") UUID userId, @RequestBody UpdateUserRequest request) {
        log.info("Received request to patch current user");
        GeneralResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    public ResponseEntity<GeneralResponse> deleteCurrentUser(@RequestHeader("X-User-Id") UUID userId) {
        log.info("Received request to delete current user");
        GeneralResponse response = userService.deleteUser(userId);
        return ResponseEntity.ok(response);
    }

    // Admin-only endpoints. No @PreAuthorize needed as API Gateway handles authorization
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        log.info("Received request to get all users");
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUser(@PathVariable UUID userId) {
        log.info("Received request to get user with id: {}", userId);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<GeneralResponse> updateUser(@PathVariable UUID userId, @Valid @RequestBody UpdateUserRequest request) {
        log.info("Received request to update user with id: {}", userId);
        GeneralResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<GeneralResponse> patchUser(@PathVariable UUID userId, @RequestBody Map<String, Object> request) {
        log.info("Received request to patch user with id: {}", userId);
        GeneralResponse response = userService.patchUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<GeneralResponse> deleteUser(@PathVariable UUID userId) {
        log.info("Received request to delete user with id: {}", userId);
        GeneralResponse response = userService.deleteUser(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email, // Additional filter
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Received request to search users with filters: " +
                        "firstName={}, lastName={}, email={}, pageable={}",
                firstName, lastName, email, pageable);

        Page<UserDto> users = userService.searchUsers(firstName, lastName, email, pageable);

        return ResponseEntity.ok(users);
    }
}