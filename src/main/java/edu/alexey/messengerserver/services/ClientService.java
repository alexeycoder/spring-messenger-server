package edu.alexey.messengerserver.services;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

@Service
public class ClientService {

	private final ConcurrentMap<UUID, Boolean> clientStatusMap = new ConcurrentHashMap<UUID, Boolean>();
	private final ConcurrentMap<UUID, Set<UUID>> userClientsMap = new ConcurrentHashMap<UUID, Set<UUID>>();

	public void registerClient(UUID clientUuid, UUID userUuid) {
		clientStatusMap.put(clientUuid, true);
		userClientsMap.computeIfAbsent(
				userUuid,
				k -> Collections.newSetFromMap(new ConcurrentHashMap<UUID, Boolean>()))
				.add(clientUuid);
	}

	public boolean hasNewMessages(UUID clientUuid) {
		return clientStatusMap.getOrDefault(clientUuid, false);
	}

	public void notifyUserHasNewMessages(UUID userUuid) {

		userClientsMap.getOrDefault(userUuid, Set.of())
				.forEach(clientUuid -> clientStatusMap.put(clientUuid, true));
	}

	public void unsetHasNewMessages(UUID clientUuid) {
		clientStatusMap.put(clientUuid, false);
	}
}
