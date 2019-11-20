package jogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import exception.FileLogException;
import exception.LockLogException;

/**
 * Abstract class for Jogger (Java logger)
 * @author Andrea Serra
 *
 */
public abstract class JoggerAbstract {
	protected static String logDirPath = Paths.get(System.getProperty("user.dir"), "log").toString();
	protected final ReentrantLock REENTRANT_LOCK = new ReentrantLock();
	protected String prefixFileLog = "log_";
	protected String nameLog = "jogger";
	protected String fileType = ".log";
	protected int maxSizeBytes = 51200;
	protected boolean lock = false;

	/* ################################################################################# */
	/* START ABSTRACT METHODS */
	/* ################################################################################# */

	public abstract File getLogFile() throws FileLogException;

	/* ################################################################################# */
	/* END ABSTRACT METHODS */
	/* ################################################################################# */

	/* ################################################################################# */
	/* START GET AND SET */
	/* ################################################################################# */
	
	public String getLogDirPath() {
		return logDirPath;
	}
	public void setLogDirPath(String logDirPath) {
		Jogger.logDirPath = logDirPath;
	}
	public String getPrefixFileLog() {
		return prefixFileLog;
	}
	public void setPrefixFileLog(String prefixFileLog) {
		this.prefixFileLog = prefixFileLog;
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

	/* metodo che ritorna il file di log su cui lavorare */
	/**
	 * method that return a log file to work on
	 * @param pathDirLog path directory
	 * @return log file
	 * @throws FileLogException
	 */
	public File getFile(String pathDirLog) throws FileLogException {
		File fileDirLog = new File(pathDirLog);
		File fileLog = null;
		
		if (!fileDirLog.exists()) fileDirLog.mkdirs();
		else if (fileDirLog.isFile()) throw new FileLogException("Error!!! Check that there no file with same name.\nDirectory path: " + fileDirLog.getAbsolutePath()); 
		
		String fileNameLog = prefixFileLog + nameLog + "-";
		if (fileDirLog.isDirectory()) {
			String regex = fileNameLog + "([\\d]{6})" + fileType;
			ArrayList<String> listFileLog = new ArrayList<String>();
			String[] lfl = fileDirLog.list();
			for (String fname : lfl) if (Pattern.matches(regex, fname)) listFileLog.add(fname);

			if (listFileLog.isEmpty()) {
				fileLog = Paths.get(pathDirLog, fileNameLog + "000000" + fileType).toFile();
				try {
					if (!fileLog.createNewFile()) throw new FileLogException("Unable to work on log file, check permissions.");
				} catch (IOException e) {
					throw new FileLogException("Unable to work on log file.\nError message: " + e.getMessage());
				}
			} else {
				Collections.sort(listFileLog);
				Collections.reverse(listFileLog);
				fileLog = Paths.get(pathDirLog, listFileLog.get(0)).toFile();
			}

			Long sizeLogByte = fileLog.length();
			if (sizeLogByte > maxSizeBytes) {
				Matcher m = Pattern.compile(regex).matcher(listFileLog.get(0));
				m.find();
				String nLog = String.format("%06d", Integer.valueOf(m.group(1)) + 1);

				fileLog = Paths.get(pathDirLog, fileNameLog + nLog + fileType).toFile();
				try {
					if (!fileLog.createNewFile()) throw new FileLogException("Unable to work on log file, check permissions.");
				} catch (IOException e) {
					throw new FileLogException("Unable to work on log file.\nError message: " + e.getMessage());
				}
			}
		} else throw new FileLogException("Unable to work on log directory, check permissions.");
		
		return fileLog;
	}

	/**
	 * method that return file log if exists
	 * @param pathDirLog path directory
	 * @return file if exists, false otherwise
	 * @throws FileLogException
	 */
	public File getFileIfExists(String pathDirLog) throws FileLogException {
		File fileDirLog = new File(pathDirLog);
		File fileLog = null;
		
		if (!fileDirLog.exists()) return null;
		else if (fileDirLog.isFile()) throw new FileLogException("Error!!! Check that there no file with same name.\nDirectory path: " + fileDirLog.getAbsolutePath()); 
		
		String fileNameLog = prefixFileLog + nameLog + "-";
		if (fileDirLog.isDirectory()) {
			String regex = fileNameLog + "([\\d]{6})" + fileType;
			ArrayList<String> listFileLog = new ArrayList<String>();
			String[] lfl = fileDirLog.list();
			for (String fname : lfl) if (Pattern.matches(regex, fname)) listFileLog.add(fname);

			if (listFileLog.isEmpty()) {
				return null;
			} else {
				Collections.sort(listFileLog);
				Collections.reverse(listFileLog);
				fileLog = Paths.get(pathDirLog, listFileLog.get(0)).toFile();
			}

			Long sizeLogByte = fileLog.length();
			if (sizeLogByte > maxSizeBytes) {
				Matcher m = Pattern.compile(regex).matcher(listFileLog.get(0));
				m.find();
				String nLog = String.format("%06d", Integer.valueOf(m.group(1)) + 1);

				fileLog = Paths.get(pathDirLog, fileNameLog + nLog + fileType).toFile();
				try {
					if (!fileLog.createNewFile()) throw new FileLogException("Unable to work on log file, check permissions.");
				} catch (IOException e) {
					throw new FileLogException("Unable to work on log file.\nError message: " + e.getMessage());
				}
			}
		} else throw new FileLogException("Unable to work on log directory, check permissions.");
		
		return fileLog;
	}

	/* metodo che ritorna path del file log da usare */
	/**
	 * method that get a path of log file to be used
	 * @return string of log file path
	 * @throws FileLogException
	 */
	public String getLogFilePath() throws FileLogException {
		return getLogFile().getAbsolutePath();
	}

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
}
