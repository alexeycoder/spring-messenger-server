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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Users", description = "The Users API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	@Operation(summary = "Get user's Display Name", description = "User UUID must exist")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "The user's Display Name", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "400", description = "Invalid user UUID supplied", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "User not found", content = @Content)
	})
	@GetMapping("/{user_uuid}")
	public ResponseEntity<String> getDisplayName(HttpServletRequest request, @PathVariable("user_uuid") UUID userUuid) {

		log.info("IP {} -- Requested displayName of user {}", ControllerUtils.getClientIp(request), userUuid);

		Optional<User> userOpt = userService.getByUserUuid(userUuid);
		if (userOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(userOpt.get().getDisplayName());
	}

	@Operation(summary = "Create user", description = "User sign-up data must be eligible. Username must not be occupied")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "The created user URL in Location field", content = @Content),
			@ApiResponse(responseCode = "400", description = "Invalid user sign-up data supplied", content = @Content)
	})
	@PostMapping("/signup")
	public ResponseEntity<Void> signUp(@Valid @RequestBody UserSignupDto userSignupDto) {

		User addedUser = userService.add(userSignupDto);
		log.info("New user \"{}\" signed up", addedUser.getUsername());
		return ResponseEntity.created(
				UriComponentsBuilder.fromPath("/users/{uuid}")
						.buildAndExpand(addedUser.getUserUuid())
						.toUri())
				.build();
	}

	@Operation(summary = "Find by user UUID pattern", description = "Partial match is allowed")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All found users", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))) }),
			@ApiResponse(responseCode = "400", description = "Invalid user UUID pattern supplied", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	@GetMapping(params = "uuid")
	public ResponseEntity<List<UserResponseDto>> findByUuid(
			@RequestParam(name = "uuid", required = true) String userUuidPattern) {

		List<UserResponseDto> userDtos = userService.findByUserUuidLimited(userUuidPattern)
				.stream()
				.map(u -> new UserResponseDto(u.getUserUuid(), u.getDisplayName()))
				.toList();

		return ResponseEntity.ok(userDtos);
	}

	@Operation(summary = "Find by Display Name pattern", description = "Partial match is allowed")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "All found users", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserResponseDto.class))) }),
			@ApiResponse(responseCode = "400", description = "Invalid Display Name pattern supplied", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
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
