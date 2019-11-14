package it.jogger.exception;

/**
 * Class for lock log exception
 * @author Andrea Serra
 *
 */
public class LockLogException extends Exception {
	private static final long serialVersionUID = -5186996946749733063L;

	public LockLogException() {
	}

	public LockLogException(String message) {
		super(message);
	}

	public LockLogException(Throwable cause) {
		super(cause);
	}

	public LockLogException(String message, Throwable cause) {
		super(message, cause);
	}

	public LockLogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
