package backend.controller;

import backend.auth.SessionManager;
import backend.dto.UserDto;
import backend.model.Role;
import backend.model.User;
import backend.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * Controller for managing user accounts in the system.
 * Provides endpoints for retrieving, creating, and deleting users.
 * Cross-origin requests are allowed from http://localhost:5173 for development purposes.
 */
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository repo;

    /**
     * Constructor for UserController with dependency injection.
     *
     * @param repo UserRepository for accessing user data
     */
    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    /**
     * Retrieves all users in the system.
     * Returns a list of all users as UserDto objects.
     *
     * @return List of UserDto objects representing all users
     */
    @GetMapping
    public List<UserDto> all() {
        return repo.findAll().stream().map(UserController::toDto).toList();
    }

    /**
     * Retrieves a specific user by their ID.
     *
     * @param id The ID of the user to retrieve
     * @return ResponseEntity with UserDto if found, or 404 status if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> get(@PathVariable Long id) {
        return repo.findById(id)
                .map(UserController::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Creates a new user in the system.
     * Validates that the username is not already taken.
     *
     * @param req CreateUserRequest containing username and password for the new user
     * @return ResponseEntity with the created UserDto and location header,
     *         or bad request if username already exists
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserRequest req) {
        if (repo.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User u = new User();
        u.setUsername(req.username());

        User saved = repo.save(u);

        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();

        return ResponseEntity.created(location).body(toDto(saved));
    }

    /**
     * Deletes a user from the system by their ID.
     *
     * @param id The ID of the user to delete
     * @return ResponseEntity with 204 status if successfully deleted,
     *         or 404 status if user not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // --- DTO mapping ---

    /**
     * Converts a User entity to a UserDto data transfer object.
     *
     * @param u The User entity to convert
     * @return UserDto containing the user's ID, username, and role
     */
    private static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getUsername(),
                u.getRole() == null ? null : u.getRole().name());
    }

    // --- Request model ---

    /**
     * Record representing the data required to create a new user.
     */
    public record CreateUserRequest(
            @NotBlank String username,
            String password
    ) {}

    /**
     * Validates the authentication token and retrieves the corresponding user.
     * This is a helper method for authorization checks.
     *
     * @param token Authentication token from the X-Auth header
     * @return User entity if token is valid
     * @throws RuntimeException with "401 Unauthorized" message if token is invalid or user not found
     */
    private User requireUser(String token) {
        Long uid = SessionManager.resolveUserId(token);
        if (uid == null) throw new RuntimeException("401 Unauthorized");
        return repo.findById(uid).orElseThrow(() -> new RuntimeException("401 Unauthorized"));
    }

    /**
     * Validates that the user has either DOCTOR or STAFF role.
     * This is a helper method for authorization checks.
     *
     * @param u The user to check
     * @throws RuntimeException with "403 Forbidden" message if user doesn't have required role
     */
    private void requireDoctorOrStaff(User u) {
        if (u.getRole() != Role.DOCTOR && u.getRole() != Role.STAFF)
            throw new RuntimeException("403 Forbidden");
    }
}