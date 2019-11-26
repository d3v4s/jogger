package jogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exception.FileLogException;
import exception.LockLogException;

/**
 * Class base for log manager
 * @author Andrea Serra
 *
 */
public class LogManager {
	/* PRIVATE */
	/* message formats */
	private final String DIR_SAME_NAME_MSGFRMT = "Error!!! Check that there no file with same name.\nDirectory path: {0}";
	private final String UNBL_WORK_FILE_MSGFRMT = "Unable to work on log file '{0}'.\nError message: {1}";
	private final String UNBL_WORK_DIR_MSGFRMT = "Unable to work on log directory '{0}'.";

	/* PROTECTED */
	protected static final String LOGS_DIR = Paths.get(System.getProperty("user.dir"), "log").toString();
	protected final ReentrantLock REENTRANT_LOCK = new ReentrantLock();
	protected String logDirWorkPath = LOGS_DIR;
	protected String prefixLogFile = "log_";
	protected String nameLog = "jogger";
	protected String fileType = ".log";
	protected int maxSizeBytes = 51200;
	protected boolean lock = false;

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */
	
	public String getLogDirWorkPath() {
		return logDirWorkPath;
	}
	public void setLogDirWorkPath(String logDirPath) {
		this.logDirWorkPath = logDirPath;
	}
	public String getPrefixLogFile() {
		return prefixLogFile;
	}
	public void setPrefixLogFile(String prefixLogFile) {
		this.prefixLogFile = prefixLogFile;
	}
	public String getNameLog() {
		return nameLog;
	}
	public void setNameLog(String nameLog) {
		this.nameLog = nameLog;
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

	/* ################################################################################# */
	/* END GET AND SET */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START PUBLIC METHODS */
	/* ################################################################################# */

	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that return a log file to work on
	 * @param pathDirLog path directory
	 * @return log file
	 * @throws FileLogException
	 */
	public File getFile() throws FileLogException {
		File logDir = getLogDir(logDirWorkPath);
		File logFile = null;

		/* build file name */
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(prefixLogFile);
		stringBuilder.append(nameLog);
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
				if (!logFile.createNewFile()) throw new FileLogException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), "Check permissions."));
			} catch (IOException e) {
				throw new FileLogException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), e.getMessage()));
			}
			/* else get file to work on */
		} else logFile = getLogFileToWork(logFileList, logDirWorkPath, regex, fileNameLog);

		return logFile;
	}

	/**
	 * method that return file log if exists
	 * @param pathDirLog path directory
	 * @return file if exists, false otherwise
	 * @throws FileLogException
	 */
	public File getFileIfExists() throws FileLogException {
		File logDir = getLogDir(logDirWorkPath); 
		File fileLog = null;

		/* build file name */
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(prefixLogFile);
		stringBuilder.append(nameLog);
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

	/* metodo che ritorna path del file log da usare */
	/**
	 * method that get a path of log file to work on
	 * @return string of log file path
	 * @throws FileLogException
	 */
	public String getLogFilePath() throws FileLogException {
		return getFile().getAbsolutePath();
	}

	/* ################################################################################# */
	/* END PUBLIC METHODS */
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
		if (lock) {
			try {
				if (!REENTRANT_LOCK.tryLock(30, TimeUnit.SECONDS)) throw new LockLogException("Error Timeout Reentrant Lock");
			} catch (InterruptedException e) {
				return false;
			}
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
	private File getLogDir(String pathDirLog) throws FileLogException {
		File logDir = new File(pathDirLog);

		/* create if no exists */
		if (!logDir.exists()) logDir.mkdirs();
		/* check if is not a file */
		else if (logDir.isFile()) throw new FileLogException(MessageFormat.format(DIR_SAME_NAME_MSGFRMT, logDir.getAbsolutePath()));

		/* check if the directory is created */
		if (!logDir.isDirectory()) throw new FileLogException(MessageFormat.format(UNBL_WORK_DIR_MSGFRMT, logDir.getPath()));

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
	private File getLogFileToWork(ArrayList<String> logFileList, String pathDirLog, String regex, String fileNameLog) throws FileLogException {
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
				if (!logFile.createNewFile()) throw new FileLogException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), "Check permissions."));
			} catch (IOException e) {
				throw new FileLogException(MessageFormat.format(UNBL_WORK_FILE_MSGFRMT, logFile.getPath(), e.getMessage()));
			}
		}

		return logFile;
	}

	/* ################################################################################# */
	/* END PRIVATE METHODS */
	/* ################################################################################# */
}
