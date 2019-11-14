package it.jogger.exception;

/**
 * Class for file log exception
 * @author Andrea Serra
 *
 */
public class FileLogException extends java.lang.Exception {
	private static final long serialVersionUID = -6001453549907088936L;

	public FileLogException() {
	}

	public FileLogException(String message) {
		super(message);
	}

	public FileLogException(Throwable cause) {
		super(cause);
	}

	public FileLogException(String message, Throwable cause) {
		super(message, cause);
	}

	public FileLogException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
