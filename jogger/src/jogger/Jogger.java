package jogger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exception.LogFileException;
import exception.LockLogException;

/**
 * This class implements a simple system to manage the logs of an application
 * @author Andrea Serra
 */
public class Jogger {
	/* PRIVATE */
	private String[] splitLogDir = {"jogger"};
	/* message formats */
	private final String DIR_SAME_NAME_MSGFRMT = "Error!!! Check that there no file with same name.\nDirectory path: {0}";
	private final String UNBL_WORK_FILE_MSGFRMT = "Unable to work on log file '{0}'.\nError message: {1}";
	private final String UNBL_WORK_DIR_MSGFRMT = "Unable to work on log directory '{0}'.";

	/* PROTECTED */
	protected static final String LOGS_DIR = Paths.get(System.getProperty("user.dir"), "log").toString();
	protected final ReentrantLock REENTRANT_LOCK = new ReentrantLock();
	protected String logDirWorkPath = getLogDirPath(splitLogDir);
	protected String prefixLogFile = "log_";
	protected String logName = "jogger";
	protected String fileType = ".log";
	protected int maxSizeBytes = 51200;
	protected boolean lock = false;

	/* ################################################################################# */
	/* START CONSTRUCTORS */
	/* ################################################################################# */

	/**
	 * simple construct
	 */
	public Jogger(String... splitLogDir) {
		if (splitLogDir.length > 0) {
			this.splitLogDir =  splitLogDir;
			this.logDirWorkPath = getLogDirPath(splitLogDir);
		}
	}

	/**
	 * constructor that set the log file name and the path to save the log file
	 * @param logName for log file
	 * @param splitLogDir to save log file
	 */
	public Jogger(String logName, String... splitLogDir) {
		this.logName = logName;
		if (splitLogDir.length > 0) {
			this.splitLogDir =  splitLogDir;
			this.logDirWorkPath = getLogDirPath(splitLogDir);
		}
	}

	/**
	 * constructor that set log file name, the max size of file in bytes and the path to save the log file
	 * @param logName for log file
	 * @param maxSizeBytes of log file
	 * @param splitLogDir to save log file
	 */
	public Jogger(String logName, Integer maxSizeBytes, String... splitLogDir) {
		this.logName = logName;
		this.maxSizeBytes = maxSizeBytes;
		if (splitLogDir.length > 0) {
			this.splitLogDir =  splitLogDir;
			this.logDirWorkPath = getLogDirPath(splitLogDir);
		}
	}

	/* ################################################################################# */
	/* END CONSTRUCTORS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START STATIC METHODS */
	/* ################################################################################# */


	/* metodo per scrivere sul file di log */
	/**
	 * static method that write to the log file
	 * @param write string to be written
	 * @param logName of file to write on
	 * @param splitLogDir where log file is located
	 * @throws LockLogException
	 */
	public static void writeLog(String write, String logName, String... splitLogDir) throws LockLogException {
		Jogger jogger = new Jogger(logName, splitLogDir);
		jogger.writeLog(write);
	}

	/* metodo per scrivere sul file di log */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @param logName of file to write on
	 * @param maxSizeBytes of log file
	 * @param splitLogDir where log file is located
	 * @throws LockLogException
	 */
	public static void writeLog(String write, String logName, Integer maxSizeBytes, String... splitLogDir) throws LockLogException {
		Jogger jogger = new Jogger(logName, maxSizeBytes, splitLogDir);
		jogger.writeLog(write);
	}

	/* metodo che ritorna path */
	/**
	 * method that get the string of the path for log file
	 * @param splitLogDir
	 * @return string of the path
	 */
	public static String getLogDirPath(String... splitLogDir) {
		String pathDirLog = LOGS_DIR;
		if (splitLogDir != null) pathDirLog = Paths.get(pathDirLog, splitLogDir).toString();
		return pathDirLog;
	}

	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that get a log file to work on
	 * @param logName file to work on
	 * @param splitLogDir where the file is located
	 * @return log file
	 * @throws LogFileException
	 */
	public static File getLogFile(String logName, String... splitLogDir) throws LogFileException {
		Jogger jogger = new Jogger(logName, splitLogDir);
		return jogger.getFile();
	}
	
	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that get a log file to work on
	 * @param logName file to work on
	 * @param maxSizeBytes of log file
	 * @param splitLogDir where the file is located
	 * @return log file
	 * @throws LogFileException
	 */
	public static File getLogFile(String logName, Integer maxSizeBytes, String... splitLogDir) throws LogFileException {
		Jogger jogger = new Jogger(logName, maxSizeBytes, splitLogDir);
		return jogger.getFile();
	}

	/**
	 * method that get a log file to work on if exists
	 * @param logName file to work on
	 * @param splitLogDir where the file is located
	 * @return log file
	 * @throws LogFileException
	 */
	public static File getLogFileIfExists(String logName, String... splitLogDir) throws LogFileException {
		Jogger jogger = new Jogger(logName, splitLogDir);
		return jogger.getFileIfExists();
	}
	
	/**
	 * method that get a log file to work on if exists
	 * @param logName file to work on
	 * @param maxSizeBytes of log file
	 * @param splitLogDir where the file is located
	 * @return log file
	 * @throws LogFileException
	 */
	public static File getLogFileIfExists(String logName, Integer maxSizeBytes, String... splitLogDir) throws LogFileException {
		Jogger jogger = new Jogger(logName, maxSizeBytes, splitLogDir);
		return jogger.getFileIfExists();
	}

	/* START GET PATH */

	/* metodo che ritorna path del file log da usare */
	/**
	 * method that return path of log file to be used
	 * @param logName of log file
	 * @param splitLogDir list where log file is located
	 * @return string of log file path
	 * @throws LogFileException
	 */
	public static String getLogFilePath(String logName, String... splitLogDir) throws LogFileException {
		return getLogFile(logName, splitLogDir).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * static method that get a path of log file to be used
	 * @param logName of log file
	 * @param maxSizeBytes of log file
	 * @param splitLogDir list where log file is located
	 * @return string of log file path
	 * @throws LogFileException
	 */
	public static String getLogFilePath(String logName, Integer maxSizeBytes, String... splitLogDir) throws LogFileException {
		return getLogFile(logName, maxSizeBytes, splitLogDir).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * method that return path of log file to be used
	 * @param logName of log file
	 * @param splitLogDir list where log file is located
	 * @return string of log file path
	 * @throws LogFileException
	 */
	public static String getLogFilePathIfExists(String logName, String... splitLogDir) throws LogFileException {
		return getLogFileIfExists(logName, splitLogDir).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * static method that get a path of log file to be used
	 * @param logName of log file
	 * @param maxSizeBytes of log file
	 * @param splitLogDir list where log file is located
	 * @return string of log file path
	 * @throws LogFileException
	 */
	public static String getLogFilePathIfExists(String logName, Integer maxSizeBytes, String... splitLogDir) throws LogFileException {
		return getLogFileIfExists(logName, maxSizeBytes, splitLogDir).getAbsolutePath();
	}

	/* END GET PATH */

	/* ################################################################################# */
	/* END STATIC METHODS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */

	public String getPrefixLogFile() {
		return prefixLogFile;
	}
	public void setPrefixLogFile(String prefixLogFile) {
		this.prefixLogFile = prefixLogFile;
	}
	public String getLogName() {
		return logName;
	}
	public void setLogName(String logName) {
		this.logName = logName;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public int getMaxSizeBytes() {
		return maxSizeBytes;
	}
	public void setMaxSizeBytes(int maxSizeBytes) {
		this.maxSizeBytes = maxSizeBytes;
	}
	public boolean isLock() {
		return lock;
	}
	public void setLock(boolean lock) {
		this.lock = lock;
	}
	public String[] getSplitLogDir() {
		return splitLogDir;
	}
	public void setSplitLogDir(String... splitLogDir) {
		this.splitLogDir = splitLogDir;
		this.logDirWorkPath = getLogDirPath(splitLogDir);
	}

	/* ################################################################################# */
	/* END GET AND SET */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START LOG METHODS */
	/* ################################################################################# */

	/* metodo per scrivere sul file di log */
	/**
	 * method that write to the log file
	 * @param write string to be written
	 * @throws LogFileException
	 * @throws LockLogException
	 */
	public void writeLog(String write) throws LockLogException {
		if (!tryLock()) return;

		RandomAccessFile raf = null;
		try {
			File fLog = getFile();
			raf = new RandomAccessFile(fLog, "rw");
			raf.seek(raf.length());
			raf.writeBytes(write.concat("\n"));
		} catch (IOException | LogFileException e) {
			e.printStackTrace();
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
			tryUnlock();
		}
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * method that get a path of log file to work on
	 * @return string of log file path
	 * @throws LogFileException
	 */
	public String getLogFilePath() throws LogFileException {
		return getFile().getAbsolutePath();
	}

	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that return a log file to work on
	 * @param pathDirLog path directory
	 * @return log file
	 * @throws LogFileException
	 */
	public File getFile() throws LogFileException {
		File logDir = getLogDir(logDirWorkPath);
		File logFile = null;

		/* build file name */
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(prefixLogFile);
		stringBuilder.append(logName);
		stringBuilder.append('-');
		String fileNameLog = stringBuilder.toString();

		/* build regex */
		stringBuilder = new StringBuilder();
		stringBuilder.append(fileNameLog);
		stringBuilder.append("([\\d]{6})");
		stringBuilder.append(fileType);
		String regex = stringBuilder.toString();

		/* search log files in the directory */
		ArrayList<String> logFileList = getLogfiles(logDir, regex);

		if (logFileList.isEmpty()) {
			/* if no log file found create new log file */
			logFile = Paths.get(logDirWorkPath, fileNameLog + "000000" + fileType).toFile();
			try {
				if (!logFile.createNewFile()) throw new LogFileException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), "Check permissions."));
			} catch (IOException e) {
				throw new LogFileException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), e.getMessage()));
			}
			/* else get file to work on */
		} else logFile = getLogFileToWork(logFileList, logDirWorkPath, regex, fileNameLog);

		return logFile;
	}

	/**
	 * method that return file log if exists
	 * @param pathDirLog path directory
	 * @return file if exists, false otherwise
	 * @throws LogFileException
	 */
	public File getFileIfExists() throws LogFileException {
		File logDir = getLogDir(logDirWorkPath); 
		File fileLog = null;

		/* build file name */
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(prefixLogFile);
		stringBuilder.append(logName);
		stringBuilder.append('-');
		String fileNameLog = stringBuilder.toString();

		/* build regex */
		stringBuilder = new StringBuilder();
		stringBuilder.append(fileNameLog);
		stringBuilder.append("([\\d]{6})");
		stringBuilder.append(fileType);
		String regex = stringBuilder.toString();

		/* search log files in the directory */
		ArrayList<String> logFileList = getLogfiles(logDir, regex);
		
		/* return if not log file found */
		if (logFileList.isEmpty()) return null;

		/* get log file to work on and return it */
		fileLog = getLogFileToWork(logFileList, logDirWorkPath, regex, fileNameLog);
		return fileLog;
	}

	/* ################################################################################# */
	/* END LOG METHODS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START PROTECTED METHODS */
	/* ################################################################################# */
	
	/**
	 * method that check if is set lock and try to lock a document
	 * @return true if lock is disabled or successfully lock a file, false otherwise  
	 * @throws JSXLockException
	 */
	protected boolean tryLock() throws LockLogException {
		if (!lock) return true;

		try {
			if (!REENTRANT_LOCK.tryLock(30, TimeUnit.SECONDS)) throw new LockLogException("Error Timeout Reentrant Lock");
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * method that check if lock is set and unlock a document 
	 */
	protected void tryUnlock() {
		if (lock) REENTRANT_LOCK.unlock();
	}

	/* ################################################################################# */
	/* END PROTECTED METHODS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START PRIVATE METHODS */
	/* ################################################################################# */

	/* method that get the log directory */
	private File getLogDir(String pathDirLog) throws LogFileException {
		File logDir = new File(pathDirLog);

		/* create if no exists */
		if (!logDir.exists()) logDir.mkdirs();
		/* check if is not a file */
		else if (logDir.isFile()) throw new LogFileException(MessageFormat.format(DIR_SAME_NAME_MSGFRMT, logDir.getAbsolutePath()));

		/* check if the directory is created */
		if (!logDir.isDirectory()) throw new LogFileException(MessageFormat.format(UNBL_WORK_DIR_MSGFRMT, logDir.getPath()));

		return logDir;
	}

	/* method that search log files in the directory */
	private ArrayList<String> getLogfiles(File logDir, String regex) {
		ArrayList<String> logFiles = new ArrayList<String>();

		/* list the file in the directory */
		String[] fileList = logDir.list();
		/* if is a log file, add it in the array */
		for (String fname : fileList) if (Pattern.matches(regex, fname)) logFiles.add(fname);

		return logFiles;
	}

	/* method that get a log file to work on */
	private File getLogFileToWork(ArrayList<String> logFileList, String pathDirLog, String regex, String fileNameLog) throws LogFileException {
		File logFile = null;
		/* sort and reverse the array */
		Collections.sort(logFileList);
		Collections.reverse(logFileList);

		/* get file to work on */
		logFile = Paths.get(pathDirLog, logFileList.get(0)).toFile();

		/* check the max size of log file */
		if (logFile.length() > maxSizeBytes) {
			/* get number of log */
			Matcher m = Pattern.compile(regex).matcher(logFileList.get(0));
			m.find();
			int nLog = Integer.valueOf(m.group(1));

			/* increment nLog for new log file and create string */
			String nLogStr = String.format("%06d",  ++nLog);

			/* get new log file */
			logFile = Paths.get(pathDirLog, fileNameLog + nLogStr + fileType).toFile();
			try {
				/* create the new log file */
				if (!logFile.createNewFile()) throw new LogFileException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), "Check permissions."));
			} catch (IOException e) {
				throw new LogFileException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), e.getMessage()));
			}
		}

		return logFile;
	}

	/* ################################################################################# */
	/* END PRIVATE METHODS */
	/* ################################################################################# */

}
