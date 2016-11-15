package org.blazer.userservice.exception;

public class DuplicateKeyException extends Exception {

	private static final long serialVersionUID = 2032770538319169959L;

	public DuplicateKeyException() {
		super();
	}

	public DuplicateKeyException(String message) {
		super(message);
	}

	public DuplicateKeyException(String message, Throwable tb) {
		super(message, tb);
	}

	public DuplicateKeyException(Throwable tb) {
		super(tb);
	}

}
