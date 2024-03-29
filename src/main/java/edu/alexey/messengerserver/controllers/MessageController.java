package edu.alexey.messengerserver.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.alexey.messengerserver.dto.MessageRequestDto;
import edu.alexey.messengerserver.dto.MessageResponseDto;
import edu.alexey.messengerserver.entities.Message;
import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.services.ClientService;
import edu.alexey.messengerserver.services.MessageService;
import edu.alexey.messengerserver.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/messages")
public class MessageController {

	private final UserService userService;
	private final MessageService messageService;
	private final ClientService clientService;

	@GetMapping("/{user_uuid}/{limit}")
	public ResponseEntity<List<MessageResponseDto>> test(@PathVariable("user_uuid") UUID userUuid,
			@PathVariable("limit") int limit) {

		System.out.println("REQUESTED MESSAGES OF " + userUuid.toString());

		Optional<User> userOpt = userService.getByUserUuid(userUuid);

		System.out.println(userOpt);
		System.out.println(userOpt.orElse(null));

		if (userOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		List<Message> messages = messageService.findLast(userOpt.get(), limit);
		List<MessageResponseDto> result = messages.stream().map(MessageResponseDto::new).toList();

		return ResponseEntity.ok(result);
	}

	@GetMapping({ "/since/{message_uuid}", "/since/{message_uuid}/client/{client_uuid}" })
	public ResponseEntity<List<MessageResponseDto>> findSince(
			@AuthenticationPrincipal User userDetails,
			@PathVariable("message_uuid") UUID sinceMessageUuid,
			@PathVariable(name = "client_uuid", required = false) UUID clientUuid) {

		if (clientUuid != null) {
			clientService.unsetHasIncomingMessages(clientUuid);
			System.out.println("CLIENTUUID SPECIFIED " + clientUuid);
		}
		List<Message> messages = messageService.findSince(userDetails, sinceMessageUuid); // messageService.findLast(userDetails.getUserUuid(), limit);
		List<MessageResponseDto> result = messages.stream().map(MessageResponseDto::new).toList();
		return ResponseEntity.ok(result);
	}

	@GetMapping({ "/last/{count}", "/last/{count}/client/{client_uuid}" })
	public ResponseEntity<List<MessageResponseDto>> getLast(
			@AuthenticationPrincipal User userDetails,
			@PathVariable("count") int count,
			@PathVariable(name = "client_uuid", required = false) UUID clientUuid) {

		if (clientUuid != null) {
			clientService.unsetHasIncomingMessages(clientUuid);
		}
		List<Message> messages = messageService.findLast(userDetails, count);
		List<MessageResponseDto> result = messages.stream().map(MessageResponseDto::new).toList();
		return ResponseEntity.ok(result);
	}

	@PostMapping("/send")
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

		User addressee = addresseeOpt.get();
		Message message = new Message();
		message.setSender(userDetails);
		message.setAddressee(addressee);
		message.setContent(messageDto.getContent());
		message.setSentAt(LocalDateTime.now());
		messageService.add(message);

		clientService.notifyUserHasIncomingMessages(addressee.getUserUuid());

		return ResponseEntity.ok().build();
	}

}
