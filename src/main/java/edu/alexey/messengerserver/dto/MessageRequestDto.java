package edu.alexey.messengerserver.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * JSON Сообщение, полученное от клиента для отправки другому адресату.
 */
@Data
public class MessageRequestDto {

	@NotNull(message = "The addresseeUuid is required")
	private UUID addresseeUuid;

	@NotBlank(message = "The content is required")
	private String content;
}
