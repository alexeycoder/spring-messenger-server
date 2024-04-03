package edu.alexey.messengerserver.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerserver.entities.Message;
import edu.alexey.messengerserver.entities.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

	Optional<Message> findByMessageUuid(UUID messageUuid);

	List<Message> findByAddresseeOrSender(User addressee, User sender, Sort sort, Limit limit);

	@Query("select m from Message m where ( m.addressee = ?1 or m.sender = ?1 ) and m.sentAt >= ?2 order by m.sentAt, m.messageId")
	Stream<Message> streamAllLaterByUser(User user, LocalDateTime sentAt);

	// Stream<Message> streamByAddresseeAndSentAtGreaterThanEqual(User addressee, LocalDateTime sentAt, Sort sort);
	// Stream<Message> streamByAddresseeOrSenderAndSentAtGreaterThanEqual(User addressee, User sender, LocalDateTime sentAt, Sort sort);
	// List<Message> findByAddresseeOrderBySentAtAsc(User addressee, Limit limit);
}
