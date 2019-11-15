package jogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import exception.FileLogException;
import exception.LockLogException;

/**
 * This class implements a simple system for log errror of application
 * @author Andrea Serra
 *
 */
public class JoggerError extends JoggerAbstract {
	private String logDirPathError = Paths.get(Jogger.logDirPath, "error").toString();
	private String prefixFileLog = "log_error-";

	/* ################################################################################# */
	/* START CONSTRUCTORS */
	/* ################################################################################# */

	public JoggerError() {
	}

	/**
	 * constructor that set the log file name
	 * @param nameLog for log file
	 */
	public JoggerError(String nameLog) {
		this.nameLog = nameLog;
	}

	/**
	 * constructor that set log file name and the max size of file in bytes
	 * @param nameLog for log file
	 * @param maxSizeBytes of log file
	 */
	public JoggerError(String nameLog, Integer maxSizeBytes) {
		this.nameLog = nameLog;
		this.maxSizeBytes = maxSizeBytes;
	}

	/* ################################################################################# */
	/* END CONSTRUCTORS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */

	public String getLogDirPathError() {
		return logDirPathError;
	}
	public void setLogDirPathError(String logDirPathError) {
		this.logDirPathError = logDirPathError;
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
		return getFile(logDirPathError);
	}

	/* metodo per scrivere sul file di log un'eccezione */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @throws FileLogException
	 * @throws LockLogException
	 */
	public void writeLog(Exception exception) throws FileLogException, LockLogException {
		if (lock) {
			try {
				if (!REENTRANT_LOCK.tryLock(30, TimeUnit.SECONDS)) throw new LockLogException("Error Timeout Reentrant Lock");
			} catch (InterruptedException e) {
				return;
			}
		}
		File fLog = getLogFile();
		PrintWriter pwLog = null;
		try {
			String post = readLogFile(fLog);
			pwLog = new PrintWriter(fLog);
			pwLog.append("Date: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)); 
			pwLog.append(" -- Error message: " + exception.getMessage() + "\n\t");
			exception.printStackTrace(pwLog);
			pwLog.append("\n" + post);
			pwLog.flush();
		} catch (FileNotFoundException e) {
			throw new FileLogException("Unable to work on file log.\nMessage error: " + e.getMessage());
		} finally {
			pwLog.close();
			if (lock) REENTRANT_LOCK.unlock();
		}
	}

	/* metodo che legge il file log e ritorna stringa */
	/**
	 * method that get a string with content of log file
	 * @param file to be read
	 * @return string with content of log file
	 * @throws FileLogException
	 */
	private String readLogFile(File file) throws FileLogException {
		StringBuffer textOut = new StringBuffer();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			while(raf.getFilePointer() < raf.length()) textOut = textOut.append(raf.readLine() + (raf.getFilePointer() == raf.length()-1 ? "" : "\n"));
		} catch (IOException e) {
			try {
				raf.close();
			} catch (IOException e1) {
			}
			throw new FileLogException("Unable to work on file log.\nMessage error: " + e.getMessage());
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
		return textOut.toString();
	}
}
