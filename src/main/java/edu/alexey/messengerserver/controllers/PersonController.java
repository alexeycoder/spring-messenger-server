package edu.alexey.messengerserver.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.alexey.messengerserver.entities.Person;
import edu.alexey.messengerserver.services.PersonService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
public class PersonController {

	private final PersonService personService;

	@GetMapping()
	List<Person> all() {
		return personService.getAll();
	}

}
