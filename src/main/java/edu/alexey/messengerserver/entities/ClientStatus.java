package edu.alexey.messengerserver.entities;

import java.util.UUID;

public class ClientStatus {
	private final UUID userUuid;
	private volatile boolean hasIncomingMessages;

	public ClientStatus(UUID userUuid) {
		this.userUuid = userUuid;
	}

	public UUID getUserUuid() {
		return userUuid;
	}

	public boolean hasIncomingMessages() {
		return hasIncomingMessages;
	}

	public void setHasIncomingMessages() {
		this.hasIncomingMessages = true;
	}

	public void unsetHasIncomingMessages() {
		this.hasIncomingMessages = false;
	}

}
