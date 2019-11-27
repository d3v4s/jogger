package exception;

/**
 * Class for file log exception
 * @author Andrea Serra
 *
 */
public class LogFileException extends Exception {
	private static final long serialVersionUID = -6001453549907088936L;

	public LogFileException() {
	}

	public LogFileException(String message) {
		super(message);
	}

	public LogFileException(Throwable cause) {
		super(cause);
	}

	public LogFileException(String message, Throwable cause) {
		super(message, cause);
	}

	public LogFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
