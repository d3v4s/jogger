package jogger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import exception.LogFileException;
import exception.LockLogException;

/**
 * This class implements a simple system for log error of application
 * @author Andrea Serra
 *
 */
public class JoggerError extends Jogger {
	private final static String[] LOG_DIR_ERROR_LIST = {"error"};
	private final String PREFIX_LOG_FILE_ERROR = "log_error-";

	/* ################################################################################# */
	/* START CONSTRUCTORS */
	/* ################################################################################# */

	/**
	 * simple construct
	 */
	public JoggerError() {
		super(LOG_DIR_ERROR_LIST);
		this.prefixLogFile = PREFIX_LOG_FILE_ERROR;
		
	}

	/**
	 * constructor that set the log file name
	 * @param nameLog for log file
	 */
	public JoggerError(String nameLog) {
		super(nameLog, LOG_DIR_ERROR_LIST);
		this.prefixLogFile = PREFIX_LOG_FILE_ERROR;
	}

	/**
	 * constructor that set log file name and the max size of file in bytes
	 * @param nameLog for log file
	 * @param maxSizeBytes of log file
	 */
	public JoggerError(String nameLog, Integer maxSizeBytes) {
		super(nameLog, maxSizeBytes, LOG_DIR_ERROR_LIST);
	}

	/* ################################################################################# */
	/* END CONSTRUCTORS */
	/* ################################################################################# */

	@Override
	@Deprecated
	public void writeLog(String write) {
	}

	/* metodo per scrivere sul file di log un'eccezione */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @throws LogFileException
	 * @throws LockLogException
	 */
	public void writeLog(Exception exception) throws LockLogException {
		if (!tryLock()) return;

		PrintWriter pwLog = null;
		try {
			File fLog = getFile();
			String post = readLogFile(fLog);
			pwLog = new PrintWriter(fLog);
			pwLog.append(MessageFormat.format("Date: {0} -- Error message: {1}\n\t", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME), exception.getMessage())); 
			exception.printStackTrace(pwLog);
			pwLog.append("\n" + post);
			pwLog.flush();
		} catch (FileNotFoundException | LogFileException e) {
			e.printStackTrace();
		} finally {
			pwLog.close();
			tryUnlock();
			exception.printStackTrace();
		}
	}

	/* metodo che legge il file log e ritorna stringa */
	/**
	 * method that get a string with content of log file
	 * @param file to be read
	 * @return string with content of log file
	 * @throws LogFileException
	 */
	private String readLogFile(File file) throws LogFileException {
		StringBuilder textOut = new StringBuilder();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			while(raf.getFilePointer() < raf.length()) textOut.append(raf.readLine().concat(raf.getFilePointer() == raf.length()-1 ? "" : "\n"));
		} catch (IOException e) {
			try {
				raf.close();
			} catch (IOException e1) {
			}
			throw new LogFileException(MessageFormat.format("Unable to work on file log.\nMessage error: {0}", e.getMessage()));
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
		return textOut.toString();
	}
}
