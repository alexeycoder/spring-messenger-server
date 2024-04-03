package edu.alexey.messengerserver.services;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.data.domain.Limit;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.javafaker.Faker;

import edu.alexey.messengerserver.dto.UserSignupDto;
import edu.alexey.messengerserver.entities.Message;
import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.repositories.MessageRepository;
import edu.alexey.messengerserver.repositories.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final MessageRepository messageRepository;

	@PostConstruct
	private void init() {
		if (userRepository.count() > 0)
			return;
		var users = populateUsers();
		populateMessages(users);
	}

	public Optional<User> getByUserUuid(UUID userUuid) {
		return userRepository.getByUserUuid(userUuid);
	}

	public boolean exists(UUID userUuid) {
		return userRepository.existsByUserUuid(userUuid);
	}

	public User add(UserSignupDto userSignupDto) {
		String username = userSignupDto.getUsername();
		String encodedPassword = passwordEncoder.encode(userSignupDto.getPassword());
		String displayName = userSignupDto.getDisplayName();

		User user = new User();
		// user.setRegisteredAt(LocalDateTime.now());
		// user.setUserUuid(UUID.randomUUID());
		user.setUsername(username);
		user.setPassword(encodedPassword);
		user.setDisplayName(displayName);
		return userRepository.saveAndFlush(user);
	}

	public List<User> findByDisplayNameLimited(String displayNamePattern) {
		return userRepository.findTop10ByDisplayNameIgnoringCaseContaining(displayNamePattern);
	}

	public List<User> findByUserUuidLimited(String userUuidPattern) {
		return userRepository.findByUserUuidPattern(userUuidPattern.toLowerCase(), Limit.of(10));
	}

	public List<User> populateUsers() {

		Faker faker = new Faker();

		List<User> users = Stream.<User>generate(() -> {

			User user = new User();
			// user.setRegisteredAt(LocalDateTime.now());
			// user.setUserUuid(UUID.randomUUID());
			user.setUsername(faker.name().username());
			String password = faker.internet().password();
			user.setPassword(passwordEncoder.encode(password));
			user.setDisplayName(faker.name().fullName());
			log.info("TEST USER: " + user + " password=" + password);
			return user;

		}).limit(4).toList();

		var user = new User();
		user.setUsername("user.user");
		user.setPassword(passwordEncoder.encode("1234"));
		user.setDisplayName("User User");

		userRepository.save(user);
		users.forEach(userRepository::save);

		userRepository.flush();

		return users;
	}

	public void populateMessages(List<User> users) {

		Faker faker = new Faker();

		ArrayList<Message> messages = new ArrayList<Message>();
		final int maxMessagesSent = 50;

		for (User user : users) {

			List<User> others = users.stream().filter(u -> u != user).toList();
			for (User addressee : others) {
				int rndCount = ThreadLocalRandom.current().nextInt(maxMessagesSent + 1);
				for (int i = 0; i < rndCount; ++i) {

					Message message = new Message();
					message.setSender(user);
					message.setAddressee(addressee);
					message.setSentAt(faker.date()
							.past(10, TimeUnit.DAYS)
							.toInstant().atZone(ZoneId.systemDefault())
							.toLocalDateTime());
					message.setContent(faker.lorem().sentence(ThreadLocalRandom.current().nextInt(5, 20)));
					messages.add(message);
				}
			}

		}

		//		messageRepository.saveAllAndFlush(messages);
		messageRepository.saveAll(messages);
	}

}
