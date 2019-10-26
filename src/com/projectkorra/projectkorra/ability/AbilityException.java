package com.projectkorra.projectkorra.ability;

public class AbilityException extends RuntimeException {

	public AbilityException(String message) {
		super(message);
	}

	public AbilityException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbilityException(Throwable cause) {
		super(cause);
	}
}
