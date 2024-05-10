package edu.alexey.messengerserver.controllers;

import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import edu.alexey.messengerserver.entities.User;
import edu.alexey.messengerserver.repositories.MessageRepository;
import edu.alexey.messengerserver.repositories.UserRepository;
import edu.alexey.messengerserver.services.ClientService;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
@Testcontainers
class ClientControllerTest {

	private static final String ENDPOINT_BASE = "/client";

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

	@AfterAll
	static void tearDownAfterClass() throws Exception {
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

	@AfterEach
	void tearDown() throws Exception {
	}

	// Check all methods are protected with Basic Authentication:

	@Test
	void checkAuthResults401IfIllegalUser() {

		webTestClient.get().uri(ENDPOINT_BASE)
				.header(HttpHeaders.AUTHORIZATION, badUserBasicAuthHeader)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void registerClientResults401IfIllegalUser() {

		webTestClient.post().uri(ENDPOINT_BASE + "/" + UUID.randomUUID())
				.header(HttpHeaders.AUTHORIZATION, badUserBasicAuthHeader)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	@Test
	void checkUpdatesResults401IfIllegalUser() {

		webTestClient.get().uri(ENDPOINT_BASE + "/" + UUID.randomUUID())
				.header(HttpHeaders.AUTHORIZATION, badUserBasicAuthHeader)
				.exchange()
				.expectStatus().isUnauthorized();
	}

	// 'After Authenticated' Checks:

	@Test
	void checkAuthResults200WithUserUuidForValidUser() {

		webTestClient.get().uri(ENDPOINT_BASE)
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isOk()
				.expectBody(UUID.class).isEqualTo(validUser.getUserUuid());
	}

	@Test
	void registerClientResults201ForAnyValidClientUuid() {

		UUID clientUuid = UUID.randomUUID();

		webTestClient.post().uri(ENDPOINT_BASE + "/" + clientUuid)
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isCreated()
				.expectHeader().valueMatches(HttpHeaders.LOCATION, ENDPOINT_BASE + "/" + clientUuid.toString())
				.expectBody().isEmpty();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcde", "123456", "~@0.123" })
	void registerClientResults400IfInvalidClientUuidFormat(String clientUuidStr) {

		webTestClient.post().uri(ENDPOINT_BASE + "/" + clientUuidStr)
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@ParameterizedTest
	@ValueSource(strings = { "abcde", "123456", "~@0.123" })
	void checkUpdatesResults400IfInvalidClientUuidFormat(String clientUuidStr) {

		webTestClient.get().uri(ENDPOINT_BASE + "/" + clientUuidStr)
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isBadRequest();
	}

	@Test
	void checkUpdatesResult200WithAnswerZeroIfNoMessages() {

		UUID clientUuid = UUID.randomUUID();

		clientService.registerClient(clientUuid, validUser.getUserUuid());
		clientService.unsetHasNewMessages(clientUuid);

		webTestClient.get().uri(ENDPOINT_BASE + "/" + clientUuid.toString())
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Integer.class).isEqualTo(Integer.valueOf(0));
	}

	@Test
	void checkUpdatesResult200WithAnswerOneIfHasMessages() {

		UUID clientUuid = UUID.randomUUID();

		clientService.registerClient(clientUuid, validUser.getUserUuid());
		clientService.notifyUserHasNewMessages(clientUuid);

		webTestClient.get().uri(ENDPOINT_BASE + "/" + clientUuid.toString())
				.header(HttpHeaders.AUTHORIZATION, validUserBasicAuthHeader)
				.exchange()
				.expectStatus().isOk()
				.expectBody(Integer.class).isEqualTo(Integer.valueOf(1));
	}

}
