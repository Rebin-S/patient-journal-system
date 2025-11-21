package backend.controller;

import backend.auth.SessionManager;
import backend.model.*;
import backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing patient medical records, encounters, and conditions.
 * Provides endpoints for healthcare professionals to create patient notes and diagnoses,
 * and for patients to access their own medical records.
 * Cross-origin requests are allowed from http://localhost:5173 for development purposes.
 */
@RestController
@RequestMapping("/api/patients")
@CrossOrigin(origins = "http://localhost:5173") // byt port om din frontend kör annat
public class PatientRecordController {

    private final PatientRepository patients;
    private final EncounterRepository encounters;
    private final ConditionRepository conditions;
    private final UserRepository users;
    private final PractitionerRepository practitioners;

    /**
     * Constructor for PatientRecordController with dependency injection.
     *
     * @param patients PatientRepository for accessing patient data
     * @param encounters EncounterRepository for accessing encounter/note data
     * @param conditions ConditionRepository for accessing diagnosis data
     * @param users UserRepository for accessing user data
     * @param practitioners PractitionerRepository for accessing practitioner data
     */
    public PatientRecordController(
            PatientRepository patients,
            EncounterRepository encounters,
            ConditionRepository conditions,
            UserRepository users,
            PractitionerRepository practitioners
    ) {
        this.patients = patients;
        this.encounters = encounters;
        this.conditions = conditions;
        this.users = users;
        this.practitioners = practitioners;
    }

    // ==== helpers ====

    private User requireUser(String token) {
        Long uid = SessionManager.resolveUserId(token);
        if (uid == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }
        return users.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid session"));
    }

    private void requireDoctorOrStaff(User u) {
        if (u.getRole() != Role.DOCTOR && u.getRole() != Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only doctor/staff may do this");
        }
    }

    // =========================================================
    // 1) Create patient note via patient ID
    // =========================================================

    /**
     * Record representing the data required to create a new patient note.
     */
    public record CreateNoteRequest(String noteText) {}

    /**
     * Creates a new medical note/encounter for a specific patient by patient ID.
     * Restricted to doctors and staff members only.
     *
     * @param token Authentication token from the X-Auth header
     * @param patientId The ID of the patient to add the note for
     * @param req CreateNoteRequest containing the note text
     * @return ResponseEntity with encounter details including ID, patientId, notes, and startTime
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 403 status if user is not doctor/staff
     * @throws ResponseStatusException with 404 status if patient is not found
     */
    @PostMapping("/{patientId}/notes")
    public ResponseEntity<?> createNote(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @PathVariable Long patientId,
            @RequestBody CreateNoteRequest req
    ) {
        User user = requireUser(token);
        requireDoctorOrStaff(user);

        Patient patient = patients.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        Encounter enc = new Encounter();
        enc.setPatientId(patient.getId());
        enc.setPractitionerId(user.getPractitionerId());
        enc.setStartTime(LocalDateTime.now());
        enc.setNotes(req.noteText());

        Encounter saved = encounters.save(enc);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("patientId", saved.getPatientId());
        resp.put("notes", saved.getNotes());
        resp.put("startTime", saved.getStartTime());
        return ResponseEntity.ok(resp);
    }

    // =======================================================
    // 2) Create diagnosis (Condition)
    // =======================================================

    /**
     * Record representing the data required to create a new diagnosis.
     */
    public record CreateDiagnosisRequest(String code, String display, String onsetDate) {}

    /**
     * Creates a new medical diagnosis/condition for a specific patient by patient ID.
     * Restricted to doctors and staff members only.
     *
     * @param token Authentication token from the X-Auth header
     * @param patientId The ID of the patient to add the diagnosis for
     * @param req CreateDiagnosisRequest containing diagnosis code, display name, and onset date
     * @return ResponseEntity with condition details including ID, patientId, code, display, and onsetDate
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 403 status if user is not doctor/staff
     * @throws ResponseStatusException with 404 status if patient is not found
     */
    @PostMapping("/{patientId}/conditions")
    public ResponseEntity<?> createDiagnosis(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @PathVariable Long patientId,
            @RequestBody CreateDiagnosisRequest req
    ) {
        User user = requireUser(token);
        requireDoctorOrStaff(user);

        Patient patient = patients.findById(patientId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        Condition cond = new Condition();
        cond.setPatient(patient);        // ManyToOne till Patient
        cond.setCode(req.code());
        cond.setDisplay(req.display());
        if (req.onsetDate() != null && !req.onsetDate().isBlank()) {
            cond.setOnsetDate(LocalDate.parse(req.onsetDate())); // "2025-11-09"
        }
        if (user.getPractitionerId() != null) {
            practitioners.findById(user.getPractitionerId())
                    .ifPresent(cond::setAssertedByPractitioner);
        }

        Condition saved = conditions.save(cond);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("patientId", saved.getPatient().getId());
        resp.put("code", saved.getCode());
        resp.put("display", saved.getDisplay());
        resp.put("onsetDate", saved.getOnsetDate());
        return ResponseEntity.ok(resp);
    }

    // =========================================================
    // 3) Create note via patient name
    // =========================================================

    /**
     * Record representing the data required to create a new patient note using patient name.
     */
    public record CreateNoteByNameRequest(String patientName, String noteText) {}

    /**
     * Creates a new medical note/encounter for a patient by patient name instead of ID.
     * Restricted to doctors and staff members only.
     *
     * @param token Authentication token from the X-Auth header
     * @param req CreateNoteByNameRequest containing patient name and note text
     * @return ResponseEntity with encounter details including ID, patientId, notes, and startTime
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 403 status if user is not doctor/staff
     * @throws ResponseStatusException with 404 status if patient is not found
     */
    @PostMapping("/notes/by-name")
    public ResponseEntity<?> createNoteByName(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @RequestBody CreateNoteByNameRequest req
    ) {
        User user = requireUser(token);
        requireDoctorOrStaff(user);

        Patient patient = patients.findByName(req.patientName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        Encounter enc = new Encounter();
        enc.setPatientId(patient.getId());
        enc.setPractitionerId(user.getPractitionerId());
        enc.setStartTime(LocalDateTime.now());
        enc.setNotes(req.noteText());

        Encounter saved = encounters.save(enc);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("patientId", saved.getPatientId());
        resp.put("notes", saved.getNotes());
        resp.put("startTime", saved.getStartTime());
        return ResponseEntity.ok(resp);
    }

    // =========================================================
    // 4) Create diagnosis via patient name
    // =========================================================

    /**
     * Record representing the data required to create a new diagnosis using patient name.
     */
    public record CreateDiagnosisByNameRequest(String patientName, String code, String display, String onsetDate) {}

    /**
     * Creates a new medical diagnosis/condition for a patient by patient name instead of ID.
     * Restricted to doctors and staff members only.
     *
     * @param token Authentication token from the X-Auth header
     * @param req CreateDiagnosisByNameRequest containing patient name, diagnosis code, display name, and onset date
     * @return ResponseEntity with condition details including ID, patientId, code, display, and onsetDate
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 403 status if user is not doctor/staff
     * @throws ResponseStatusException with 404 status if patient is not found
     */
    @PostMapping("/conditions/by-name")
    public ResponseEntity<?> createDiagnosisByName(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @RequestBody CreateDiagnosisByNameRequest req
    ) {
        User user = requireUser(token);
        requireDoctorOrStaff(user);

        Patient patient = patients.findByName(req.patientName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        Condition cond = new Condition();
        cond.setPatient(patient);
        cond.setCode(req.code());
        cond.setDisplay(req.display());
        if (req.onsetDate() != null && !req.onsetDate().isBlank()) {
            cond.setOnsetDate(LocalDate.parse(req.onsetDate()));
        }
        if (user.getPractitionerId() != null) {
            practitioners.findById(user.getPractitionerId())
                    .ifPresent(cond::setAssertedByPractitioner);
        }

        Condition saved = conditions.save(cond);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", saved.getId());
        resp.put("patientId", saved.getPatient().getId());
        resp.put("code", saved.getCode());
        resp.put("display", saved.getDisplay());
        resp.put("onsetDate", saved.getOnsetDate());
        return ResponseEntity.ok(resp);
    }

    /**
     * Record representing a complete patient summary including patient details, notes, and conditions.
     */
    public record PatientSummaryResponse(
            Patient patient,
            java.util.List<Encounter> notes,
            java.util.List<Condition> conditions
    ) {}

    /**
     * Retrieves the complete medical record for a specific patient by patient name.
     * Includes patient details, all medical notes/encounters, and all diagnoses/conditions.
     * Restricted to doctors and staff members only.
     *
     * @param token Authentication token from the X-Auth header
     * @param patientName The name of the patient to retrieve the record for
     * @return ResponseEntity with PatientSummaryResponse containing patient, notes, and conditions
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 403 status if user is not doctor/staff
     * @throws ResponseStatusException with 404 status if patient is not found
     */
    @GetMapping("/{patientName}/full")
    public ResponseEntity<?> getFullRecordForDoctor(
            @RequestHeader(value = "X-Auth", required = false) String token,
            @PathVariable String patientName
    ) {
        User user = requireUser(token);
        requireDoctorOrStaff(user);

        Patient patient = patients.findByName(patientName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        var notes = encounters.findByPatientId(patient.getId());
        var conds = conditions.findByPatient(patient);

        return ResponseEntity.ok(new PatientSummaryResponse(patient, notes, conds));
    }

    /**
     * Retrieves the current user's own complete medical record.
     * Available only to patients and returns their own patient data, notes, and conditions.
     *
     * @param token Authentication token from the X-Auth header
     * @return ResponseEntity with PatientSummaryResponse containing the patient's own medical record
     * @throws ResponseStatusException with 401 status if user is not authenticated
     * @throws ResponseStatusException with 403 status if user is not a patient
     * @throws ResponseStatusException with 400 status if no patient is linked to the user
     * @throws ResponseStatusException with 404 status if patient record is not found
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMyRecord(
            @RequestHeader(value = "X-Auth", required = false) String token
    ) {
        User user = requireUser(token);
        if (user.getRole() != Role.PATIENT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients can use this");
        }
        if (user.getPatientId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No patient linked to this user");
        }

        Patient patient = patients.findById(user.getPatientId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patient not found"));

        var notes = encounters.findByPatientId(patient.getId());
        var conds = conditions.findByPatient(patient);

        return ResponseEntity.ok(new PatientSummaryResponse(patient, notes, conds));
    }
}