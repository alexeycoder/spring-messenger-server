package edu.alexey.messengerserver.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import edu.alexey.messengerserver.entities.Message;
import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.repositories.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class MessageService {

	private final MessageRepository messageRepository;

	@Transactional
	public List<Message> findSince(User user, UUID sinceMessageUuid) {

		Optional<Message> sinceMessageOpt = messageRepository.findByAddresseeAndMessageUuid(user, sinceMessageUuid);

		if (sinceMessageOpt.isEmpty()) {
			log.error("Вероятная ошибка: не найдено сообщение с идентификатором {} для пользователя {} ({})",
					sinceMessageUuid,
					user.getUsername(),
					user.getUserUuid());
			return List.of();
		}

		var sinceDateTime = sinceMessageOpt.get().getSentAt();
		Stream<Message> result = messageRepository.streamByAddresseeAndSentAtGreaterThanEqual(
				user,
				sinceDateTime,
				Sort.by("sentAt", "messageId").ascending());

		return result.filter(m -> !m.getMessageUuid().equals(sinceMessageUuid)).toList();
	}

	public List<Message> findLast(User user, int limit) {

		// List<Message> result = messageRepository.findByAddresseeOrderBySentAtAsc(user, new Limit(0, limit));
		List<Message> result = messageRepository.findByAddressee(
				user,
				Sort.by("sentAt", "messageId").descending(),
				Limit.of(limit));
		return result.reversed();
	}

	public Message add(Message message) {

		return messageRepository.save(message);
	}

}
