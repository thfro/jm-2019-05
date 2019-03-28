package com.frotscher.demo.googleclient;

public class OverQueryLimitException extends RuntimeException {

	public OverQueryLimitException(String message, Throwable cause) {
		super(message, cause);
	}

	public OverQueryLimitException(String message) {
		super(message);
	}
}
