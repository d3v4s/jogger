package it.jogger.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.jogger.exception.FileLogException;

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
	 * @return log file
	 * @throws FileLogException
	 */
	public File getFile(String pathDirLog) throws FileLogException {
		File fileDirLog = new File(pathDirLog);
		File fileLog = null;
		
		if (!fileDirLog.exists()) fileDirLog.mkdirs();
		else if (fileDirLog.isFile()) throw new FileLogException("Error!!! Check that there no file with same name."); 
		
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
					throw new FileLogException("Unable to work on log file.\n"
												+ "Error message: " + e.getMessage());
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
					throw new FileLogException("Unable to work on log file.\n"
												+ "Error message: " + e.getMessage());
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
}
