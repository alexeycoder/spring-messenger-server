package edu.alexey.messengerserver.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.alexey.messengerserver.utils.ControllerUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Home", description = "Just for tests")
@RestController
public class HomeController {

	@Operation(summary = "Does nothing")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Always OK", content = @Content)
	})
	@GetMapping("/hello")
	public ResponseEntity<Void> hello(HttpServletRequest request) {

		log.info("IP {} -- Hello", ControllerUtils.getClientIp(request));
		return ResponseEntity.ok().build();
	}
}
