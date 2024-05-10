package edu.alexey.messengerserver.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;

import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import edu.alexey.messengerserver.dto.UserResponseDto;
import edu.alexey.messengerserver.dto.UserSignupDto;
import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.repositories.MessageRepository;
import edu.alexey.messengerserver.repositories.UserRepository;
import edu.alexey.messengerserver.services.ClientService;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
@Testcontainers
class UserControllerTest {

	private static final String ENDPOINT_BASE = "/users";

	@Container
	public static PostgreSQLContainer<?> pgSqlContainer = new PostgreSQLContainer<>("postgres")
			.withDatabaseName("postgres")
			.withUsername("postgres")
			.withPassword("postgres");

	@DynamicPropertySource
	public static void properties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", pgSqlContainer::getJdbcUrl);
		registry.add("spring.datasource.username", pgSqlContainer::getUsername);
		registry.add("spring.datasource.password", pgSqlContainer::getPassword);
	}

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private ClientService clientService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	MessageRepository messageRepository;

	private static User badUser;
	private static String badUserBasicAuthHeader;

	@BeforeAll
	static void setUpBeforeClass(@Autowired PasswordEncoder passwordEncoder) throws Exception {

		String username = "bad@user";
		String password = "secret";
		String displayName = "Display Name";

		badUser = new User();
		badUser.setUsername(username);
		badUser.setPassword(passwordEncoder.encode(password));
		badUser.setDisplayName(displayName);

		badUserBasicAuthHeader = "Basic "
				+ Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}

	private User validUser;
	private String validUserBasicAuthHeader;

	@BeforeEach
	void setUp(@Autowired PasswordEncoder passwordEncoder) throws Exception {

		messageRepository.deleteAll();
		userRepository.deleteAll();

		String username = "some@validuser";
		String password = "password";
		String displayName = "Lora Palmer";

		User user = new User();
		user.setUsername(username);
		user.setPassword(passwordEncoder.encode(password));
		user.setDisplayName(displayName);
		// UUID пользователя генерируется СУБД самостоятельно при INSERT user'а
		validUser = userRepository.save(user);

		validUserBasicAuthHeader = "Basic "
				+ Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}

	@Test
	void getDisplayNameResults401ForIllegalUser() {

		webTestClient.get().uri(ENDPOINT_BASE + "/" + UUID.randomUUID())
				.header(HttpHeaders.AUTHORIZATION, badUserBasicAuthHeader)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void getDisplayNameResults200WithUserDisplayNameForValidUser() {

		webTestClient.get().uri(ENDPOINT_BASE + "/" + validUser.getUserUuid())
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class).isEqualTo(validUser.getDisplayName());
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcde", "123456", "~@0.123" })
	void getDisplayNameResults400IfInvalidUserUuidFormat(String userUuidStr) {

		webTestClient.get().uri(ENDPOINT_BASE + "/" + userUuidStr)
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void getDisplayNameResults404IfNoSuchUser() {

		webTestClient.get().uri(ENDPOINT_BASE + "/" + UUID.randomUUID())
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isNotFound();
	}

	@Test
	void signUpNameResults200AndReturnsLocationOfCreatedUser() {

		UserSignupDto userSignupDto = new UserSignupDto();
		userSignupDto.setDisplayName("Ivan Ivanov");
		userSignupDto.setUsername("ivan@ivanov");
		userSignupDto.setPassword("ivanivanov");

		webTestClient.post().uri(ENDPOINT_BASE + "/signup")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(userSignupDto)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().valueMatches(HttpHeaders.LOCATION, ENDPOINT_BASE + "/[-\\w]+");

		userRepository.findByUsername(userSignupDto.getUsername());
	}

	@Test
	void signUpNameResults400AndIneligibleSignupData() {

		UserSignupDto userSignupDto = new UserSignupDto();
		userSignupDto.setDisplayName("Ivan Ivanov");
		userSignupDto.setUsername("");
		userSignupDto.setPassword("ivanivanov");

		webTestClient.post().uri(ENDPOINT_BASE + "/signup")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(userSignupDto)
				.exchange()
				.expectStatus().isBadRequest();

		userSignupDto = new UserSignupDto();
		userSignupDto.setDisplayName("Ivan Ivanov");
		userSignupDto.setUsername("ivan@ivanov");
		userSignupDto.setPassword("");

		webTestClient.post().uri(ENDPOINT_BASE + "/signup")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(userSignupDto)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void findByUuidResults401ForIllegalUser() {

		webTestClient.get().uri(uriBuilder -> uriBuilder
				.path(ENDPOINT_BASE)
				.queryParam("uuid", UUID.randomUUID())
				.build())
				.header(HttpHeaders.AUTHORIZATION, badUserBasicAuthHeader)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void findByDisplayNameResults401ForIllegalUser() {

		webTestClient.get().uri(uriBuilder -> uriBuilder
				.path(ENDPOINT_BASE)
				.queryParam("display_name", "abc")
				.build())
				.header(HttpHeaders.AUTHORIZATION, badUserBasicAuthHeader)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void findByUuidResults200WithListOfFoundUsers() {

		var responseBody = webTestClient.get().uri(uriBuilder -> uriBuilder
				.path(ENDPOINT_BASE)
				.queryParam("uuid", validUser.getUserUuid())
				.build())
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(UserResponseDto.class)
				.returnResult().getResponseBody();

		assertThatList(responseBody).hasSize(1);
		assertThat(responseBody.getFirst()).isNotNull()
				.extracting(UserResponseDto::displayName).isEqualTo(validUser.getDisplayName());
	}

	@Test
	void findByDisplayNameResults200WithListOfFoundUsers() {

		var responseBody = webTestClient.get().uri(uriBuilder -> uriBuilder
				.path(ENDPOINT_BASE)
				.queryParam("display_name", validUser.getDisplayName())
				.build())
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isOk()
				.expectBodyList(UserResponseDto.class)
				.returnResult().getResponseBody();

		assertThatList(responseBody).hasSize(1);
		assertThat(responseBody.getFirst()).isNotNull()
				.extracting(UserResponseDto::userUuid).isEqualTo(validUser.getUserUuid());
	}

}
