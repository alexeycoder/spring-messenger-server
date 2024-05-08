package edu.alexey.messengerserver.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.alexey.messengerserver.dto.MessageRequestDto;
import edu.alexey.messengerserver.dto.MessageResponseDto;
import edu.alexey.messengerserver.entities.Message;
import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.services.ClientService;
import edu.alexey.messengerserver.services.MessageService;
import edu.alexey.messengerserver.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Messages", description = "The Messages API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {

	static final String HEADER_KEY_CLIENT_ID = "EduAlexeyMessenger-Client-Id";

	private final UserService userService;
	private final MessageService messageService;
	private final ClientService clientService;

	//	@GetMapping("/{user_uuid}/{limit}")
	//	public ResponseEntity<List<MessageResponseDto>> test(@PathVariable("user_uuid") UUID userUuid,
	//			@PathVariable("limit") int limit) {
	//
	//		System.out.println("REQUESTED MESSAGES OF " + userUuid.toString());
	//
	//		Optional<User> userOpt = userService.getByUserUuid(userUuid);
	//
	//		System.out.println(userOpt);
	//		System.out.println(userOpt.orElse(null));
	//
	//		if (userOpt.isEmpty()) {
	//			return ResponseEntity.notFound().build();
	//		}
	//
	//		List<Message> messages = messageService.findLast(userOpt.get(), limit);
	//		List<MessageResponseDto> result = messages.stream()
	//				.map(m -> new MessageResponseDto(m, null))
	//				.toList();
	//
	//		return ResponseEntity.ok(result);
	//	}

	//	@GetMapping({ "/test" })
	//	public ResponseEntity<String> test2(
	//			@AuthenticationPrincipal User userDetails,
	//			@RequestHeader(name = HEADER_KEY_CLIENT_ID, required = false) UUID clientUuid) {
	//
	//		if (clientUuid != null) {
	//			System.out.println("CLIENTUUID SPECIFIED " + clientUuid);
	//			clientService.unsetHasNewMessages(clientUuid);
	//
	//			return ResponseEntity.ok(clientUuid.toString());
	//		}
	//		return ResponseEntity.ok().build();
	//	}

	//	@GetMapping("/since/{message_uuid}")
	@Operation(summary = "Get last messages since the specified one by message UUID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Messages since the specified message excluding the one", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MessageResponseDto.class))) }),
			@ApiResponse(responseCode = "400", description = "Invalid UUID of 'since message' supplied", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	@GetMapping(params = "since_message")
	public ResponseEntity<List<MessageResponseDto>> findSince(
			@AuthenticationPrincipal User userDetails,
			@RequestHeader(name = HEADER_KEY_CLIENT_ID, required = false) UUID clientUuid,
			@RequestParam(name = "since_message", required = true) UUID sinceMessageUuid) {

		if (clientUuid != null) {
			clientService.unsetHasNewMessages(clientUuid);
		}
		List<Message> messages = messageService.findSince(userDetails, sinceMessageUuid); // messageService.findLast(userDetails.getUserUuid(), limit);
		List<MessageResponseDto> result = messages.stream()
				.map(m -> new MessageResponseDto(m, userDetails))
				.toList();
		return ResponseEntity.ok(result);
	}

	//	@GetMapping("/last/{limit}")
	@Operation(summary = "Get last messages in the amount of 'limit'")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Last N='limin' messages", content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MessageResponseDto.class))) }),
			@ApiResponse(responseCode = "400", description = "Invalid 'limit' number of messages", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
	})
	@GetMapping(params = "limit")
	public ResponseEntity<List<MessageResponseDto>> getLast(
			@AuthenticationPrincipal User userDetails,
			@RequestHeader(name = HEADER_KEY_CLIENT_ID, required = false) UUID clientUuid,
			@RequestParam(name = "limit", required = true) int limit) {

		if (clientUuid != null) {
			clientService.unsetHasNewMessages(clientUuid);
		}
		List<Message> messages = messageService.findLast(userDetails, limit);
		List<MessageResponseDto> result = messages.stream()
				.map(m -> new MessageResponseDto(m, userDetails))
				.toList();
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Send message", description = "Addressee UUID specified in the message must exist")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "The message sent succesfully", content = @Content),
			@ApiResponse(responseCode = "400", description = "Invalid message data supplied", content = @Content),
			@ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
			@ApiResponse(responseCode = "404", description = "Addressee not found", content = @Content)
	})
	@PostMapping("/new")
	public ResponseEntity<Void> send(
			@AuthenticationPrincipal User userDetails,
			@RequestBody MessageRequestDto messageDto) {

		Optional<User> addresseeOpt = userService.getByUserUuid(messageDto.getAddresseeUuid());
		if (addresseeOpt.isEmpty()) {
			log.warn("The addresseeUuid {} of message sent by {} not found.",
					messageDto.getAddresseeUuid(),
					userDetails.getUsername());
			return ResponseEntity.notFound().build();
		}

		log.warn("The userUuid {} send message to addresseeUuid {}.",
				userDetails.getUserUuid(),
				messageDto.getAddresseeUuid());

		User addressee = addresseeOpt.get();
		Message message = new Message();
		message.setSender(userDetails);
		message.setAddressee(addressee);
		message.setContent(messageDto.getContent());
		message.setSentAt(LocalDateTime.now());
		messageService.add(message);

		clientService.notifyUserHasNewMessages(addressee.getUserUuid());
		clientService.notifyUserHasNewMessages(userDetails.getUserUuid());

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

}
