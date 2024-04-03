package edu.alexey.messengerserver.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.alexey.messengerserver.utils.ControllerUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class HomeController {

	@GetMapping("/hello")
	public ResponseEntity<Void> hello(HttpServletRequest request) {

		log.info("IP {} -- Hello", ControllerUtils.getClientIp(request));
		return ResponseEntity.ok().build();
	}
}
