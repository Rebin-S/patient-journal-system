package backend.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "[condition]") // matchar din SQL
public class Condition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "code", length = 50, nullable = false)
    private String code;

    @Column(name = "display")
    private String display;

    @Column(name = "onset_date")
    private LocalDate onsetDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asserted_by_practitioner_id")
    private Practitioner assertedByPractitioner;

    // --- getters & setters ---

    public Long getId() {
        return id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public LocalDate getOnsetDate() {
        return onsetDate;
    }

    public void setOnsetDate(LocalDate onsetDate) {
        this.onsetDate = onsetDate;
    }

    public Practitioner getAssertedByPractitioner() {
        return assertedByPractitioner;
    }

    public void setAssertedByPractitioner(Practitioner assertedByPractitioner) {
        this.assertedByPractitioner = assertedByPractitioner;
    }
}
