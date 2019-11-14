package it.jogger.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import it.jogger.exception.FileLogException;
import it.jogger.exception.LockLogException;

/**
 * This class implements a simple system for debugging an application
 * @author Andrea Serra
 *
 */
public class JoggerDebug extends JoggerAbstract {
	private String logDirPathDebug = Paths.get(logDirPath, "debug").toString();
	private String prefixFileLog = "log_debug-";
	private boolean debug = true;

	public static void main(String[] args) {
		JoggerDebug jd = new JoggerDebug();
		try {
			jd.writeLog("dio cane");
		} catch (FileLogException | LockLogException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

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

	/* metodo per scrivere sul file di log */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @throws FileLogException
	 * @throws LockLogException
	 */
	public void writeLog(String write) throws FileLogException, LockLogException {
		if (debug) {
			if (lock) {
				try {
					if (!REENTRANT_LOCK.tryLock(30, TimeUnit.SECONDS)) throw new LockLogException("Error Timeout Reentrant Lock");
				} catch (InterruptedException e) {
					return;
				}
			}
			File fLog = getLogFile();
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
				throw new FileLogException("Unable to work on log file.\n"
						+ "Error message:" + e.getMessage());
			} finally {
				try {
					raf.close();
				} catch (IOException e) {
				}
				if (lock) REENTRANT_LOCK.unlock();
			}
		}
	}
}
