// backend/repository/ConditionRepository.java
package backend.repository;

import backend.model.Condition;
import backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConditionRepository extends JpaRepository<Condition, Long> {
    List<Condition> findByPatient(Patient patient);
}
