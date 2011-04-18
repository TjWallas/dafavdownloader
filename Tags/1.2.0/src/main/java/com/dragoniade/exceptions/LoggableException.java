package com.dragoniade.exceptions;

public class LoggableException extends Exception {
	private static final long serialVersionUID = -763332302301425251L;
	public LoggableException(String message) {
		super(message);
	}
	
	public LoggableException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public LoggableException(Throwable cause) {
		super(cause);
	}
}
