package org.blazer.dataservice.exception;

public class DuplicateKey extends Exception {

	private static final long serialVersionUID = 2032770538319169959L;

	public DuplicateKey() {
		super();
	}

	public DuplicateKey(String message) {
		super(message);
	}

	public DuplicateKey(String message, Throwable tb) {
		super(message, tb);
	}

	public DuplicateKey(Throwable tb) {
		super(tb);
	}

}
