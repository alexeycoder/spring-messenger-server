package edu.alexey.messengerserver.controllers;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import io.swagger.v3.oas.annotations.Hidden;

@Hidden
@RestControllerAdvice
public class CommonExceptionAdvice {

	@ExceptionHandler(NoSuchElementException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ResponseEntity<String> noSuchEntryHandler(NoSuchElementException ex) {
		return new ResponseEntity<>(Objects.requireNonNullElse(ex.getMessage(), ""), HttpStatus.NOT_FOUND);
	}

	//	@ExceptionHandler(AlreadyCoveredIssueException.class)
	//	@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
	//	ResponseEntity<String> alreadyCoveredIssueHandler(AlreadyCoveredIssueException ex) {
	//		return new ResponseEntity<>(ex.getMessage(), HttpStatus.PRECONDITION_FAILED);
	//	}

	@ExceptionHandler({
			HttpMessageNotReadableException.class,
			MethodArgumentTypeMismatchException.class,
			MethodArgumentNotValidException.class,
			MissingPathVariableException.class,
			IllegalArgumentException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	ResponseEntity<String> badRequestHandler(Exception ex) {
		System.out.println(ex.getClass().getSimpleName());
		return new ResponseEntity<>(Objects.requireNonNullElse(ex.getMessage(), ""), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	ResponseEntity<String> possiblyBadRequestHandler(NoResourceFoundException ex) {
		System.out.println(ex.getClass().getSimpleName());
		//		ex.printStackTrace();
		return new ResponseEntity<>(
				"Запрашиваемый ресурс не найден или опущен обязательный элемент в URL.",
				HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	ResponseEntity<String> suspiciousRequestHandler(RuntimeException ex) {
		System.out.println(ex.getClass().getSimpleName());
		//		ex.printStackTrace();
		return new ResponseEntity<>(Objects.requireNonNullElse(ex.getMessage(), ""), HttpStatus.FORBIDDEN);
	}
}
