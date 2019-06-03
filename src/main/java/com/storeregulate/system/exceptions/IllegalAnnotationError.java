package com.storeregulate.system.exceptions;

public class IllegalAnnotationError extends BusinessException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public IllegalAnnotationError() {
		super();
	}

	public IllegalAnnotationError(String messageKey, Object[] args) {
		super(messageKey, args);
	}

	public IllegalAnnotationError(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public IllegalAnnotationError(String message, Throwable cause) {
		super(message, cause);
	}

	public IllegalAnnotationError(String message) {
		super(message);
	}

	public IllegalAnnotationError(Throwable cause) {
		super(cause);
	}
	

}
