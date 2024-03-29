package edu.alexey.messengerserver.utils;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

public final class ControllerUtils {

	public static String getClientIp(HttpServletRequest request) {
		return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
				.orElseGet(request::getRemoteAddr);
	}
}
