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
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/client")
public class ClientController {

	private final ClientService clientService;

	@GetMapping()
	public ResponseEntity<Void> checkAuth(
			HttpServletRequest request,
			@AuthenticationPrincipal User userDetails) {

		log.info("Hello from {}, username = {}, userUuid = {}",
				ControllerUtils.getClientIp(request),
				userDetails.getUsername(),
				userDetails.getUserUuid());

		return ResponseEntity.ok().build();
	}

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
				UriComponentsBuilder.fromPath("/{client_uuid}")
						.buildAndExpand(clientUuid)
						.toUri())
				.build();
	}

	@GetMapping("/{client_uuid}")
	public ResponseEntity<Integer> checkUpdates(
			HttpServletRequest request,
			@AuthenticationPrincipal User userDetails,
			@PathVariable("client_uuid") UUID clientUuid) {

		int result = clientService.hasIncomingMessages(clientUuid) ? 1 : 0;

		log.info("Ckeck state from {}, username = {}, clientUuid = {}, result is {}",
				ControllerUtils.getClientIp(request),
				userDetails.getUsername(),
				clientUuid,
				result);

		return ResponseEntity.ok(result);
	}

}
