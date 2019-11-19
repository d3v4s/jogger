package jogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import exception.FileLogException;
import exception.LockLogException;

/**
 * This class implements a simple system to manage the logs of an application
 * @author Andrea Serra
 */
public class Jogger extends JoggerAbstract {
	private String[] dirLogList = {"jogger"};

	/* ################################################################################# */
	/* START CONSTRUCTORS */
	/* ################################################################################# */

	public Jogger() {
	}

	/**
	 * constructor that set the log file name
	 * @param nameLog for log file
	 */
	public Jogger(String nameLog) {
		this.nameLog = nameLog;
	}

	/**
	 * constructor that set the log file name and the path to save the log file
	 * @param nameLog for log file
	 * @param dirLogList to save log file
	 */
	public Jogger(String nameLog, String... dirLogList) {
		this.nameLog = nameLog;
		if (dirLogList.length > 0) this.dirLogList =  dirLogList;
	}

	/**
	 * constructor that set log file name, the max size of file in bytes and the path to save the log file
	 * @param nameLog for log file
	 * @param maxSizeBytes of log file
	 * @param dirLogList to save log file
	 */
	public Jogger(String nameLog, Integer maxSizeBytes, String... dirLogList) {
		this.nameLog = nameLog;
		this.maxSizeBytes = maxSizeBytes;
		if (dirLogList.length > 0) this.dirLogList =  dirLogList;
	}

	/* ################################################################################# */
	/* END CONSTRUCTORS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */

	public String[] getDirLogList() {
		return dirLogList;
	}
	public void setDirLogList(String[] dirLogList) {
		this.dirLogList = dirLogList;
	}

	/* ################################################################################# */
	/* END GET AND SET */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START STATIC METHODS */
	/* ################################################################################# */

	/* metodo che ritorna path del file log da usare */
	/**
	 * method that return path of log file to be used
	 * @param nameLog of log file
	 * @param dirLog list where log file is located
	 * @return string of log file path
	 * @throws FileLogException
	 */
	public static String getLogFilePath(String nameLog, String... dirLog) throws FileLogException {
		return getLogFile(nameLog, dirLog).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * static method that get a path of log file to be used
	 * @param nameLog of log file
	 * @param maxSizeBytes of log file
	 * @param dirLog list where log file is located
	 * @return string of log file path
	 * @throws FileLogException
	 */
	public static String getLogFilePath(String nameLog, Integer maxSizeBytes, String... dirLog) throws FileLogException {
		return getLogFile(nameLog, maxSizeBytes, dirLog).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * method that return path of log file to be used
	 * @param nameLog of log file
	 * @param dirLog list where log file is located
	 * @return string of log file path
	 * @throws FileLogException
	 */
	public static String getLogFilePathIfExists(String nameLog, String... dirLog) throws FileLogException {
		return getLogFileIfExists(nameLog, dirLog).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * static method that get a path of log file to be used
	 * @param nameLog of log file
	 * @param maxSizeBytes of log file
	 * @param dirLog list where log file is located
	 * @return string of log file path
	 * @throws FileLogException
	 */
	public static String getLogFilePathIfExists(String nameLog, Integer maxSizeBytes, String... dirLog) throws FileLogException {
		return getLogFileIfExists(nameLog, maxSizeBytes, dirLog).getAbsolutePath();
	}

	/* metodo per scrivere sul file di log */
	/**
	 * static method that write to the log file
	 * @param write string to be written
	 * @param nameLog of file to write on
	 * @param dirLogList where log file is located
	 * @throws FileLogException
	 * @throws LockLogException
	 */
	public static void writeLog(String write, String nameLog, String... dirLogList) throws FileLogException, LockLogException {
		Jogger jogger = new Jogger(nameLog, dirLogList);
		jogger.writeLog(write);
	}

	/* metodo per scrivere sul file di log */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @param nameLog of file to write on
	 * @param maxSizeBytes of log file
	 * @param dirLogList where log file is located
	 * @throws FileLogException
	 * @throws LockLogException
	 */
	public static void writeLog(String write, String nameLog, Integer maxSizeBytes, String... dirLogList) throws FileLogException, LockLogException {
		Jogger jogger = new Jogger(nameLog, maxSizeBytes, dirLogList);
		jogger.writeLog(write);
	}

	/* metodo che ritorna path */
	/**
	 * method that get the string of the path for log file
	 * @param dirLogList
	 * @return string of the path
	 */
	public static String getLogDirPath(String... dirLogList) {
		String pathDirLog = logDirPath;
		if (dirLogList != null) pathDirLog = Paths.get(pathDirLog, dirLogList).toString();

		return pathDirLog;
	}

	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that get a log file to work on
	 * @param nameLog file to work on
	 * @param dirLogList where the file is located
	 * @return log file
	 * @throws FileLogException
	 */
	public static File getLogFile(String nameLog, String... dirLogList) throws FileLogException {
		Jogger jogger = new Jogger(nameLog, dirLogList);
		return jogger.getLogFile();
	}
	
	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that get a log file to work on
	 * @param nameLog file to work on
	 * @param maxSizeBytes of log file
	 * @param dirLogList where the file is located
	 * @return log file
	 * @throws FileLogException
	 */
	public static File getLogFile(String nameLog, Integer maxSizeBytes, String... dirLogList) throws FileLogException {
		Jogger jogger = new Jogger(nameLog, maxSizeBytes, dirLogList);
		return jogger.getLogFile();
	}

	/**
	 * method that get a log file to work on if exists
	 * @param nameLog file to work on
	 * @param dirLogList where the file is located
	 * @return log file
	 * @throws FileLogException
	 */
	public static File getLogFileIfExists(String nameLog, String... dirLogList) throws FileLogException {
		Jogger jogger = new Jogger(nameLog, dirLogList);
		return jogger.getLogFileIfExists();
	}
	
	/**
	 * method that get a log file to work on if exists
	 * @param nameLog file to work on
	 * @param maxSizeBytes of log file
	 * @param dirLogList where the file is located
	 * @return log file
	 * @throws FileLogException
	 */
	public static File getLogFileIfExists(String nameLog, Integer maxSizeBytes, String... dirLogList) throws FileLogException {
		Jogger jogger = new Jogger(nameLog, maxSizeBytes, dirLogList);
		return jogger.getLogFileIfExists();
	}

	/* ################################################################################# */
	/* END STATIC METHODS */
	/* ################################################################################# */

	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that return a log file to work on
	 * @return log file
	 * @throws FileLogException
	 */
	public File getLogFile() throws FileLogException {
		return getFile(getLogDirPath(dirLogList));
	}

	/**
	 * method that return a log file to work on
	 * @return log file
	 * @throws FileLogException
	 */
	public File getLogFileIfExists() throws FileLogException {
		return getFileIfExists(getLogDirPath(dirLogList));
	}

	/* metodo per scrivere sul file di log */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @throws FileLogException
	 * @throws LockLogException
	 */
	public void writeLog(String write) throws FileLogException, LockLogException {
		if (lock) {
			try {
				if (!REENTRANT_LOCK.tryLock(30, TimeUnit.SECONDS)) throw new LockLogException("Error Timeout Reentrant Lock");
			} catch (InterruptedException e) {
				return;
			}
		}
		File fLog = getLogFile();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(fLog, "rw");
			raf.seek(raf.length());
			raf.writeBytes(write + "\n");
		} catch (IOException e) {
			try {
				raf.close();
			} catch (IOException e1) {
			}
			throw new FileLogException("Unable to work on log file.\nError message: " + e.getMessage());
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
			if (lock) REENTRANT_LOCK.unlock();
		}
	}
}
