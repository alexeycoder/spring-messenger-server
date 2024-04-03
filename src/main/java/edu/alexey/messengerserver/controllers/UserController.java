package edu.alexey.messengerserver.controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import edu.alexey.messengerserver.dto.UserResponseDto;
import edu.alexey.messengerserver.dto.UserSignupDto;
import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.services.UserService;
import edu.alexey.messengerserver.utils.ControllerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	@GetMapping("/{user_uuid}")
	public ResponseEntity<String> getDisplayName(HttpServletRequest request, @PathVariable("user_uuid") UUID userUuid) {

		log.info("IP {} -- Requested displayName of user {}", ControllerUtils.getClientIp(request), userUuid);

		Optional<User> userOpt = userService.getByUserUuid(userUuid);
		if (userOpt.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}

		return ResponseEntity.ok(userOpt.get().getDisplayName());
	}

	@PostMapping("/signup")
	public ResponseEntity<Void> signUp(@Valid @RequestBody UserSignupDto userSignupDto) {

		User addedUser = userService.add(userSignupDto);
		log.info("New user \"{}\" signed up", addedUser.getUsername());
		return ResponseEntity.created(
				UriComponentsBuilder.fromPath("/user/{uuid}")
						.buildAndExpand(addedUser.getUserUuid())
						.toUri())
				.build();
	}

	@GetMapping(params = "uuid")
	public ResponseEntity<List<UserResponseDto>> findByUuid(
			@RequestParam(name = "uuid", required = true) String userUuidPattern) {

		List<UserResponseDto> userDtos = userService.findByUserUuidLimited(userUuidPattern)
				.stream()
				.map(u -> new UserResponseDto(u.getUserUuid(), u.getDisplayName()))
				.toList();

		return ResponseEntity.ok(userDtos);
	}

	@GetMapping(params = "display_name")
	public ResponseEntity<List<UserResponseDto>> findByDisplayName(
			@RequestParam(name = "display_name", required = true) String displayNamePattern) {

		List<UserResponseDto> userDtos = userService.findByDisplayNameLimited(displayNamePattern)
				.stream()
				.map(u -> new UserResponseDto(u.getUserUuid(), u.getDisplayName()))
				.toList();

		return ResponseEntity.ok(userDtos);
	}

}
