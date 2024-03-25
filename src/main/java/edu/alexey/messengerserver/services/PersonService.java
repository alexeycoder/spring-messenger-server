package edu.alexey.messengerserver.services;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.github.javafaker.Faker;

import edu.alexey.messengerserver.entities.Person;

@Service
public class PersonService {

	public List<Person> getAll() {

		Faker faker = new Faker();

		return Stream.<Person>generate(() -> new Person(
				faker.name().fullName(),
				faker.phoneNumber().phoneNumber(),
				faker.address().fullAddress()))
				.limit(50).toList();
	}

}
