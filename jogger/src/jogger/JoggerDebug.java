package jogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
	private boolean debug = true;

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
					File fLog;
					try {
						fLog = getLogFile();
					} catch (FileLogException e) {
						e.printStackTrace();
						return;
					}
					RandomAccessFile raf = null;
					String out = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + " :: " + write;
					System.out.println(out);
					try {
						raf = new RandomAccessFile(fLog, "rw");
						raf.seek(raf.length());
						raf.writeBytes(out + "\n");
					} catch (IOException e) {
						try {
							raf.close();
						} catch (IOException e1) {
						}
						throw new RuntimeException("Unable to work on log file.\nError message:" + e.getMessage());
					} finally {
						try {
							raf.close();
						} catch (IOException e) {
						}
						tryUnlock();
					}
				}
			} catch (LockLogException e) {
				throw new RuntimeException("Lock log file exception.\nError message:" + e.getMessage());
			}
		}
	}
}
