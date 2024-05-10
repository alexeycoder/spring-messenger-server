package edu.alexey.messengerserver.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebClient
class HomeControllerTest {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void helloResultsOkAndDoesNotRequiresAuth() {

		webTestClient.get().uri("/hello")
				.header(HttpHeaders.AUTHORIZATION, "")
				.exchange()
				.expectStatus().isOk();
	}

}
