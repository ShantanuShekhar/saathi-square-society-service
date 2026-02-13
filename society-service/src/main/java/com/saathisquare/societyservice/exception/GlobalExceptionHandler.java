package com.saathisquare.societyservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.saathisquare.societyservice.util.Constants;
import com.saathisquare.societyservice.util.Response;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<Response<String>> handleBusinessException(BusinessException ex) {
		LOGGER.warn("Business exception: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new Response<>(ex.getErrorCode(), ex.getMessage(), null));
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Response<String>> handleResourceNotFound(ResourceNotFoundException ex) {
		LOGGER.warn("Resource not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new Response<>(Constants.VALIDATION_ERROR_API_CODE, ex.getMessage(), null));
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Response<String>> handleBadCredentials(BadCredentialsException ex) {
		LOGGER.warn("Bad credentials: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new Response<>(Constants.VALIDATION_ERROR_API_CODE, ex.getMessage(), null));
	}

	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	public ResponseEntity<Response<Map<String, String>>> handleValidationExceptions(Exception ex) {
		Map<String, String> errors = new HashMap<>();
		if (ex instanceof MethodArgumentNotValidException) {
			MethodArgumentNotValidException validationEx = (MethodArgumentNotValidException) ex;
			validationEx.getBindingResult().getAllErrors().forEach((error) -> {
				String fieldName = ((FieldError) error).getField();
				String errorMessage = error.getDefaultMessage();
				errors.put(fieldName, errorMessage);
			});
		} else if (ex instanceof BindException) {
			BindException bindEx = (BindException) ex;
			bindEx.getBindingResult().getAllErrors().forEach((error) -> {
				String fieldName = ((FieldError) error).getField();
				String errorMessage = error.getDefaultMessage();
				errors.put(fieldName, errorMessage);
			});
		}
		LOGGER.warn("Validation errors: {}", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new Response<>(Constants.VALIDATION_ERROR_API_CODE, "Validation failed", errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Response<String>> handleGeneral(Exception ex) {
		LOGGER.error("Unexpected error occurred", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new Response<>(Constants.GLOBAL_ERROR_STATUS_CODE, Constants.GLOBAL_ERROR_MESSAGE, null));
	}

}
