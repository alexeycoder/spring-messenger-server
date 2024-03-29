package edu.alexey.messengerserver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserSignupDto {

	@NotBlank(message = "The username is required")
	private String username;

	@NotBlank(message = "The password is required")
	private String password;

	@NotBlank(message = "The displayName is required")
	private String displayName;
}
