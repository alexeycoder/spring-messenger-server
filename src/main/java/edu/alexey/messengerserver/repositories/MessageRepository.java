package edu.alexey.messengerserver.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerserver.entities.Message;
import edu.alexey.messengerserver.entities.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

	Optional<Message> findByAddresseeAndMessageUuid(User addressee, UUID messageUuid);

	Stream<Message> streamByAddresseeAndSentAtGreaterThanEqual(User addressee, LocalDateTime sentAt, Sort sort);

	List<Message> findByAddressee(User addressee, Sort sort, Limit limit);

	// List<Message> findByAddresseeOrderBySentAtAsc(User addressee, Limit limit);
}
