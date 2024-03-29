package edu.alexey.messengerserver.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import edu.alexey.messengerserver.entities.Message;

/**
 * JSON Сообщение, предназначенное пользователю и возвращаемое его клиенту.
 */
public record MessageResponseDto(UUID messageUuid, UUID senderUuid, LocalDateTime sentAt, String content) {

	public MessageResponseDto {
		if (messageUuid == null) {
			throw new IllegalArgumentException("messageUuid cannot be null");
		}
		if (senderUuid == null) {
			throw new IllegalArgumentException("senderUuid cannot be null");
		}
		if (sentAt == null) {
			throw new IllegalArgumentException("sentAt cannot be null");
		}
		if (content == null || content.isBlank()) {
			throw new IllegalArgumentException("content cannot be null or empty");
		}
	}

	public MessageResponseDto(Message message) {
		this(
				message.getMessageUuid(),
				message.getSender().getUserUuid(),
				message.getSentAt(),
				message.getContent());
	}
}
