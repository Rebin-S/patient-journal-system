package backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Observation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false)
    Long patientId;
    String type;
    String value;
    String unit;
    LocalDateTime observedAt;
    Long recordedByPractitionerId;
}
