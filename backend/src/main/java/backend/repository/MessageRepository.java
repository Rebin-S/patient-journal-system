package backend.repository;

import backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // enkel, “spring-data-style” metod
    List<Message> findBySenderUserIdAndReceiverUserIdOrderBySentAtAsc(
            Long senderId,
            Long receiverId
    );
}
