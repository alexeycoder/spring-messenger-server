package edu.alexey.messengerserver.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.alexey.messengerserver.entities.Message;
import edu.alexey.messengerserver.entities.User;

/**
 * JSON Сообщение, предназначенное пользователю и возвращаемое его клиенту.
 */
public record MessageResponseDto(UUID messageUuid, UUID senderUuid, UUID addresseeUuid, LocalDateTime sentAt,
		String content) {

	public MessageResponseDto {
		if (messageUuid == null) {
			throw new IllegalArgumentException("messageUuid cannot be null");
		}
		//		if (senderUuid == null && addresseeUuid == null) {
		//			throw new IllegalArgumentException("Both senderUuid and addresseeUuid cannot be null simultaneously");
		//		}
		if (sentAt == null) {
			throw new IllegalArgumentException("sentAt cannot be null");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content cannot be null or empty");
		}
	}

	public MessageResponseDto(Message message, User targetUser) {
		this(
				message.getMessageUuid(),
				message.getSender().equals(targetUser) ? null : message.getSender().getUserUuid(),
				message.getAddressee().equals(targetUser) ? null : message.getAddressee().getUserUuid(),
				message.getSentAt(),
				message.getContent());
	}
}
