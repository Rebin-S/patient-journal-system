package backend.controller;

import backend.auth.SessionManager;
import backend.model.Message;
import backend.model.Role;
import backend.model.User;
import backend.repository.MessageRepository;
import backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for handling message operations between users in the system.
 * Provides endpoints for managing contacts, message threads, and sending messages.
 * Supports communication between patients and medical staff (doctors and staff members).
 * Cross-origin requests are allowed from http://localhost:5173 for development purposes.
 */
@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:5173")
public class MessageController {

    private final MessageRepository messages;
    private final UserRepository users;

    /**
     * Constructor for MessageController with dependency injection.
     *
     * @param messages MessageRepository for accessing message data
     * @param users UserRepository for accessing user data
     */
    public MessageController(MessageRepository messages, UserRepository users) {
        this.messages = messages;
        this.users = users;
    }

    // ===== helpers =====

    private User requireUser(String token) {
        Long uid = SessionManager.resolveUserId(token);
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return users.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session"));
    }

    /**
     * Data transfer object representing a contact user.
     */
    private record ContactDto(Long id, String username, String role) {}

    /**
     * Data transfer object representing a message with sender and receiver information.
     */
    private record MessageDto(
            Long id,
            Long senderId,
            Long receiverId,
            String senderName,
            String receiverName,
            String content,
            LocalDateTime sentAt,
            boolean read
    ) {}

    private MessageDto toDto(Message m, User sender, User receiver) {
        return new MessageDto(
                m.getId(),
                m.getSenderUserId(),
                m.getReceiverUserId(),
                sender.getUsername(),
                receiver.getUsername(),
                m.getContent(),
                m.getSentAt(),
                m.isRead()
        );
    }

    // ===========================
    // 1) Get contacts to message with
    // ===========================

    /**
     * Retrieves a list of contacts that the current user can message with.
     * The available contacts depend on the user's role:
     * - Patients can message with all doctors and staff members
     * - Doctors and staff can message with all patients
     *
     * @param token Authentication token from the X-Auth header
     * @return ResponseEntity with a list of ContactDto objects representing available contacts
     * @throws ResponseStatusException with 401 status if user is not authenticated
     */
    @GetMapping("/contacts")
    public ResponseEntity<?> getContacts(
            @RequestHeader(value = "X-Auth", required = false) String token) {

        User me = requireUser(token);

        List<User> contacts;
        if (me.getRole() == Role.PATIENT) {
            // patient -> alla läkare + personal
            var doctors = users.findByRole(Role.DOCTOR);
            var staff   = users.findByRole(Role.STAFF);
            doctors.addAll(staff);
            contacts = doctors;
        } else {
            // läkare/personal -> alla patienter
            contacts = users.findByRole(Role.PATIENT);
        }

        var list = contacts.stream()
                .filter(u -> !u.getId().equals(me.getId()))
                .map(u -> new ContactDto(u.getId(), u.getUsername(), u.getRole().name()))
                .toList();

        return ResponseEntity.ok(list);
    }

    /**
     * Retrieves the complete message thread between the current user and another user.
     * Returns all messages exchanged between the two users, sorted by timestamp in ascending order.
     *
     * @param token Authentication token from the X-Auth header
     * @param otherId The ID of the other user in the conversation
     * @return ResponseEntity with a list of MessageDto objects representing the message thread
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 404 status if the other user is not found
     */
    @GetMapping("/thread/{otherId}")
    public ResponseEntity<?> getThread(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @PathVariable Long otherId) {

        User me = requireUser(token);

        User other = users.findById(otherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // 1) alla jag -> andra
        var m1 = messages.findBySenderUserIdAndReceiverUserIdOrderBySentAtAsc(
                me.getId(), otherId);

        // 2) alla andra -> jag
        var m2 = messages.findBySenderUserIdAndReceiverUserIdOrderBySentAtAsc(
                otherId, me.getId());

        // 3) slå ihop och sortera efter tid
        var merged = java.util.stream.Stream.concat(m1.stream(), m2.stream())
                .sorted(java.util.Comparator.comparing(Message::getSentAt))
                .map(m -> {
                    User sender   = m.getSenderUserId().equals(me.getId()) ? me : other;
                    User receiver = m.getReceiverUserId().equals(me.getId()) ? me : other;
                    return toDto(m, sender, receiver);
                })
                .toList();

        return ResponseEntity.ok(merged);
    }

    // ===========================
    // 3) Send message
    // ===========================

    /**
     * Record representing the data required to send a new message.
     */
    public record SendMessageRequest(Long receiverId, String content) {}

    /**
     * Sends a new message from the current user to another user.
     * Validates that the receiver exists and that the message content is not empty.
     * Prevents users from sending messages to themselves.
     *
     * @param token Authentication token from the X-Auth header
     * @param req SendMessageRequest containing receiverId and message content
     * @return ResponseEntity with the sent message as MessageDto
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 400 status for invalid request (empty content, self-message, etc.)
     * @throws ResponseStatusException with 404 status if the receiver user is not found
     */
    @PostMapping
    public ResponseEntity<?> send(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @RequestBody SendMessageRequest req) {

        User me = requireUser(token);

        if (req.receiverId() == null || req.content() == null || req.content().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "receiverId och content krävs");
        }
        if (req.receiverId().equals(me.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Kan inte skicka till dig själv");
        }

        User receiver = users.findById(req.receiverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver not found"));

        Message m = new Message();
        m.setSenderUserId(me.getId());
        m.setReceiverUserId(receiver.getId());
        m.setContent(req.content().trim());
        m.setSentAt(LocalDateTime.now());
        m.setRead(false);

        Message saved = messages.save(m);

        return ResponseEntity.ok(toDto(saved, me, receiver));
    }
}