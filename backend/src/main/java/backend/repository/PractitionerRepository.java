// backend/repository/PractitionerRepository.java
package backend.repository;

import backend.model.Practitioner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PractitionerRepository extends JpaRepository<Practitioner, Long> {
}
