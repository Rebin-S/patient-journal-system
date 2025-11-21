package backend.model;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(unique = true, nullable = false)
    String personnummer;
    String name;
    String contactInfo;
    public long getId()
    {
        return id;
    }

    public void setName(String username) {
        this.name = username;
    }

    public void setPersonnummer(String personnummer) {
        this.personnummer = personnummer;
    }
}
