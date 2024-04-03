package edu.alexey.messengerserver.dto;

import java.util.UUID;

public record UserResponseDto(UUID userUuid, String displayName) {

	public UserResponseDto {
		if (userUuid == null) {
			throw new IllegalArgumentException("userUuid cannot be null");
		}
		if (displayName == null) {
			throw new IllegalArgumentException("displayName cannot be null");
		}
	}
}
