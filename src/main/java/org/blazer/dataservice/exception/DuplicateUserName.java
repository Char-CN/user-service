package org.blazer.dataservice.exception;

public class DuplicateUserName extends Exception {

	private static final long serialVersionUID = 2032770538319169959L;

	public DuplicateUserName() {
		super();
	}

	public DuplicateUserName(String message) {
		super(message);
	}

	public DuplicateUserName(String message, Throwable tb) {
		super(message, tb);
	}

	public DuplicateUserName(Throwable tb) {
		super(tb);
	}

}
