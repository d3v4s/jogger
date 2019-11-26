package jogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import exception.FileLogException;
import exception.LockLogException;

/**
 * This class implements a simple system for debugging an application
 * @author Andrea Serra
 *
 */
public class JoggerDebug extends LogManager {
	private final String LOG_DIR_DEBUG = Paths.get(LOGS_DIR, "debug").toString();
	private final String PREFIX_LOG_FILE_DEBUG = "log_debug-";
	private boolean debug = false;
	private boolean printStackTrace = true;

	/* ################################################################################# */
	/* START CONSTRUCTORS */
	/* ################################################################################# */

	/**
	 * simple construct
	 */
	public JoggerDebug() {
		this.prefixLogFile = PREFIX_LOG_FILE_DEBUG;
		this.logDirWorkPath = LOG_DIR_DEBUG;
	}

	/**
	 * constructor that set the log file name
	 * @param nameLog for log file
	 */
	public JoggerDebug(String nameLog) {
		this.nameLog = nameLog;
		this.prefixLogFile = PREFIX_LOG_FILE_DEBUG;
		this.logDirWorkPath = LOG_DIR_DEBUG;
	}

	/**
	 * constructor that set log file name and the max size of file in bytes
	 * @param nameLog for log file
	 * @param maxSizeBytes of log file
	 */
	public JoggerDebug(String nameLog, Integer maxSizeBytes) {
		this.nameLog = nameLog;
		this.maxSizeBytes = maxSizeBytes;
		this.prefixLogFile = PREFIX_LOG_FILE_DEBUG;
		this.logDirWorkPath = LOG_DIR_DEBUG;
	}

	/* ################################################################################# */
	/* END CONSTRUCTORS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */
	
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public boolean isPrintStackTrace() {
		return printStackTrace;
	}
	public void setPrintStackTrace(boolean printStackTrace) {
		this.printStackTrace = printStackTrace;
	}
	/* GET */
	public String getLogDirWorkPath() {
		return LOG_DIR_DEBUG;
	}

	/* ################################################################################# */
	/* END GET AND SET */
	/* ################################################################################# */

	/**
	 * method that write starting
	 */
	public void writeStart() {
		writeLog("STARTING");
	}

	/**
	 * method that write starting
	 * @param write to be append in out
	 */
	public void writeStart(String write) {
		writeLog(MessageFormat.format("STARTING -- {0}", write));
	}

	/**
	 * method that write end
	 */
	public void writeEnd() {
		writeLog("END");
	}

	/**
	 * method that write end
	 * @param write to be append in out
	 */
	public void writeEnd(String write) {
		writeLog(MessageFormat.format("END -- {0}", write));
	}

	/**
	 * method that write success
	 */
	public void writeSuccess() {
		writeLog("SUCCESS");
	}

	/**
	 * method that write success
	 * @param write to be append in out
	 */
	public void writeSuccess(String write) {
		writeLog(MessageFormat.format("SUCCESS -- {0}", write));
	}

	/**
	 * method that write fail
	 */
	public void writeFail() {
		writeLog("FAIL");
	}

	/**
	 * method that write fail
	 * @param write to be append in out
	 */
	public void writeFail(String write) {
		writeLog(MessageFormat.format("FAIL -- {0}", write));
	}

	/**
	 * method that write error
	 */
	public void writeError() {
		writeLog("ERROR");
	}

	/**
	 * method that write fail
	 * @param write to be append in out
	 */
	public void writeError(String write) {
		writeLog(MessageFormat.format("ERROR -- {0}", write));
	}

	/* metodo per scrivere sul file di log */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @throws FileLogException
	 * @throws LockLogException
	 */
	public void writeLog(String write) {
		if (debug) {
			try {
				if (tryLock()) {
					/* read file with random access file */
					RandomAccessFile raf = null;
					try {
						File fLog = getFile();
						StringBuilder out = new StringBuilder();
						out.append(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
						if (printStackTrace) {
							/* append message */
							out.append(" :: Message: ").append(write);

							/* get stack trace */
							ArrayList<StackTraceElement> stackTrace =  new ArrayList<StackTraceElement>(Arrays.asList(Thread.currentThread().getStackTrace()));

							/* remove JoggerDebug and Thread from stack trace */
							stackTrace.remove(0);
							stackTrace.remove(0);

							/* append stack trace */
							for (StackTraceElement e : stackTrace) out.append("\n\t").append(e.toString());

							/* append simple output */
						} else out.append(" :: ").append(write);

						/* print output */
						System.out.println(out.append("\n"));

						/* write output on file */
						raf = new RandomAccessFile(fLog, "rw");
						raf.seek(raf.length());
						raf.writeBytes(out.append("\n").toString());
					} catch (IOException | FileLogException e) {
						e.printStackTrace();
					} finally {
						try {
							raf.close();
						} catch (IOException e) {
						}
						tryUnlock();
					}
				}
			} catch (LockLogException e) {
				e.printStackTrace();
			}
		}
	}
}
