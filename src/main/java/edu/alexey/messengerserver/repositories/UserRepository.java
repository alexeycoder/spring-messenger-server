package edu.alexey.messengerserver.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerserver.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

	Optional<User> getByUserUuid(UUID userUuid);

	boolean existsByUserUuid(UUID userUuid);
}
