package edu.alexey.messengerserver.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import edu.alexey.messengerserver.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

	Optional<User> getByUserUuid(UUID userUuid);

	boolean existsByUserUuid(UUID userUuid);

	List<User> findTop10ByDisplayNameIgnoringCaseContaining(String displayNamePattern);

	@Query("select u from User u where lower( cast(u.userUuid as string) ) like ?1% escape '\\'")
	List<User> findByUserUuidPattern(String userUuidPatternLowerCase, Limit limit);
}
