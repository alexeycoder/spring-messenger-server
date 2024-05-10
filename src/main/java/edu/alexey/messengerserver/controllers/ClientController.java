package edu.alexey.messengerserver.controllers;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.services.ClientService;
import edu.alexey.messengerserver.utils.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Client", description = "The Client API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/client")
public class ClientController {

	private final ClientService clientService;

	@Operation(summary = "Check user authorization")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "UUID of authorized user", content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = UUID.class)) }),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	@GetMapping()
	public ResponseEntity<UUID> checkAuth(
			HttpServletRequest request,
			@AuthenticationPrincipal User userDetails) {

		log.info("Hello from {}, username = {}, userUuid = {}",
				ControllerUtils.getClientIp(request),
				userDetails.getUsername(),
				userDetails.getUserUuid());

		return ResponseEntity.ok(userDetails.getUserUuid());
	}

	@Operation(summary = "Register client application")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "The URL in Location field to check incoming messages for the registered client", content = @Content),
			@ApiResponse(responseCode = "400", description = "Invalid client UUID supplied", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	@PostMapping("/{client_uuid}")
	public ResponseEntity<Void> registerClient(
			HttpServletRequest request,
			@AuthenticationPrincipal User userDetails,
			@PathVariable("client_uuid") UUID clientUuid) {

		clientService.registerClient(clientUuid, userDetails.getUserUuid());

		log.info("Registered Client from {}, username = {}, clientUuid = {}",
				ControllerUtils.getClientIp(request),
				userDetails.getUsername(),
				clientUuid);

		return ResponseEntity.created(
				UriComponentsBuilder.fromPath("/client/{client_uuid}")
						.buildAndExpand(clientUuid)
						.toUri())
				.build();
	}

	@Operation(summary = "Check new messages for client")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "0 - if no new messages, 1 - otherwise", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Integer.class))),
			@ApiResponse(responseCode = "400", description = "Invalid client UUID supplied", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	@GetMapping("/{client_uuid}")
	public ResponseEntity<Integer> checkUpdates(
			HttpServletRequest request,
			@AuthenticationPrincipal User userDetails,
			@PathVariable("client_uuid") UUID clientUuid) {

		int result = clientService.hasNewMessages(clientUuid) ? 1 : 0;

		log.info("Check state from {}, username = {}, clientUuid = {}, result is {}",
				ControllerUtils.getClientIp(request),
				userDetails.getUsername(),
				clientUuid,
				result);

		return ResponseEntity.ok(result);
	}

}
