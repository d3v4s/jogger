package jogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
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
public class JoggerDebug extends JoggerAbstract {
	private String logDirPathDebug = Paths.get(logDirPath, "debug").toString();
	private String prefixFileLog = "log_debug-";
	private boolean debug = false;
	private boolean printStackTrace = false;

	/* ################################################################################# */
	/* START CONSTRUCTORS */
	/* ################################################################################# */

	public JoggerDebug() {
	}

	/**
	 * constructor that set the log file name
	 * @param nameLog for log file
	 */
	public JoggerDebug(String nameLog) {
		this.nameLog = nameLog;
	}

	/**
	 * constructor that set log file name and the max size of file in bytes
	 * @param nameLog for log file
	 * @param maxSizeBytes of log file
	 */
	public JoggerDebug(String nameLog, Integer maxSizeBytes) {
		this.nameLog = nameLog;
		this.maxSizeBytes = maxSizeBytes;
	}

	/* ################################################################################# */
	/* END CONSTRUCTORS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */
	
	public String getLogDirPathDebug() {
		return logDirPathDebug;
	}
	public void setLogDirPathDebug(String logDirPathDebug) {
		this.logDirPathDebug = logDirPathDebug;
	}
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

	/* ################################################################################# */
	/* END GET AND SET */
	/* ################################################################################# */

	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that return a log file to work on
	 * @return log file
	 * @throws FileLogException
	 */
	public File getLogFile() throws FileLogException {
		setPrefixFileLog(prefixFileLog);
		return getFile(logDirPathDebug);
	}

	/**
	 * method that return a log file to work on if exists
	 * @return log file if exists, null otherwise
	 * @throws FileLogException
	 */
	public File getLogFileIfExists() throws FileLogException {
		setPrefixFileLog(prefixFileLog);
		return getFileIfExists(logDirPathDebug);
	}

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
		writeLog("STARTING -- " + write);
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
		writeLog("END -- " + write);
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
		writeLog("SUCCESS -- " + write);
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
		writeLog("FAIL -- " + write);
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
					RandomAccessFile raf = null;
					try {
						File fLog = getLogFile();
						StringBuffer out = new StringBuffer(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
						if (printStackTrace) {
							ArrayList<StackTraceElement> stackTrace =  new ArrayList<StackTraceElement>(Arrays.asList(Thread.currentThread().getStackTrace()));
							stackTrace.remove(0);
							stackTrace.remove(1);
							for (StackTraceElement e : stackTrace) {
								out.append("\n\t").append(e.toString());
							}
							out.append("\nMessage: ").append(write);
						} else {
							out.append(" :: ").append(write);
						}
						System.out.println(out.append("\n"));
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
