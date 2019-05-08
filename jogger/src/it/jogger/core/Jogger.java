package it.jogger.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.jogger.exception.FileLogException;
import it.jogger.exception.LockLogException;

public class Jogger {
	private final String FILE_LOG = "log_";
	private final String FILE_TYPE = ".log";
	protected static final String LOG_DIR_PATH = Paths.get(System.getProperty("user.dir"), "log").toString();
	private int maxSizeBytes = 51200;
	private String[] dirLogList = {"jogger"};
	private String nameLog = "jogger";
	private boolean lock = false;
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/* costruttori */
	public Jogger() {
	}
	public Jogger(String nameLog) {
		this.nameLog = nameLog;
	}
	public Jogger(String nameLog, String... dirLogList) {
		this.nameLog = nameLog;
		if (dirLogList.length > 0)
			this.dirLogList =  dirLogList;
	}
	public Jogger(String nameLog, Integer maxSizeBytes, String... dirLogList) {
		this.nameLog = nameLog;
		this.maxSizeBytes = maxSizeBytes;
		if (dirLogList.length > 0)
			this.dirLogList =  dirLogList;
	}

	/* get set */
	public String getNameLog() {
		return nameLog;
	}
	public void setNameLog(String nameLog) {
		this.nameLog = nameLog;
	}
	public int getMaxSizeBytes() {
		return maxSizeBytes;
	}
	public void setMaxSizeBytes(int maxSizeBytes) {
		this.maxSizeBytes = maxSizeBytes;
	}
	public String[] getDirLogList() {
		return dirLogList;
	}
	public void setDirLogList(String[] dirLogList) {
		this.dirLogList = dirLogList;
	}
	public boolean isLock() {
		return lock;
	}
	public void setLock(boolean lock) {
		this.lock = lock;
	}

	/* metodo che ritorna path della cartella log */
	public static String getLogDirPath(String... dirLog) {
		String pathDirLog = LOG_DIR_PATH;
		if (dirLog != null)
			pathDirLog = Paths.get(pathDirLog, dirLog).toString();
		
		return pathDirLog;
	}
	
	/* metodo che ritorna il file di log su cui lavorare */
	public static File getLogFile(String nameLog, String... dirLogList) throws FileLogException {
		Jogger jogger = new Jogger(nameLog, dirLogList);
		return jogger.getLogFile();
	}
	
	/* metodo che ritorna il file di log su cui lavorare */
	public static File getLogFile(String nameLog, Integer maxSizeBytes, String... dirLogList) throws FileLogException {
		Jogger jogger = new Jogger(nameLog, maxSizeBytes, dirLogList);
		return jogger.getLogFile();
	}

	/* metodo che ritorna il file di log su cui lavorare */
	public File getLogFile() throws FileLogException {
		String pathDirLog = getLogDirPath(dirLogList);
		
		File fileDirLog = new File(pathDirLog);
		File fileLog = null;
		if (fileDirLog.exists() && fileDirLog.isFile())
			throw new FileLogException("Impossibile lavorare sulla cartella 'log', controllare i permessi o che non ci sia un file con lo stesso nome.");
		else if (!fileDirLog.exists())
			fileDirLog.mkdirs();

		nameLog = FILE_LOG + nameLog + "-";
		if (fileDirLog.isDirectory()) {
			String regex = nameLog + "([\\d]{6})" + FILE_TYPE;
			ArrayList<String> listFileLog = new ArrayList<String>();
			String[] lfl = fileDirLog.list();
			for (String fname : lfl)
				if (Pattern.matches(regex, fname))
						listFileLog.add(fname);

			if (listFileLog.isEmpty()) {
				fileLog = Paths.get(pathDirLog, nameLog + "000000" + FILE_TYPE).toFile();
				try {
					if (!fileLog.createNewFile())
						throw new FileLogException("Impossibile lavorare sul file log, controllare i permessi");
				} catch (IOException e) {
					throw new FileLogException("Impossibile lavorare sul file log.\n"
												+ "Message error: " + e.getMessage());
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
				
				fileLog = Paths.get(pathDirLog, nameLog + nLog + FILE_TYPE).toFile();
				try {
					if (!fileLog.createNewFile())
						throw new FileLogException("Impossibile lavorare sul file log, controllare i permessi.");
				} catch (IOException e) {
					throw new FileLogException("Impossibile lavorare sul file log.\n"
												+ "Message error: " + e.getMessage());
				}
			}
		}
		
		return fileLog;
	}

	/* metodo che ritorna path del file log da usare */
	public static String getLogFilePath(String nameLog, String... dirLog) throws FileLogException {
		return getLogFile(nameLog, dirLog).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	public static String getLogFilePath(String nameLog, Integer maxSizeBytes, String... dirLog) throws FileLogException {
		return getLogFile(nameLog, maxSizeBytes, dirLog).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	public String getLogFilePath() throws FileLogException {
		return getLogFile().getAbsolutePath();
	}

	/* metodo per scrivere sul file di log */
	public static void writeLog(String write, String nameLog, String... dirLogList) throws FileLogException, LockLogException {
		Jogger jogger = new Jogger(nameLog, dirLogList);
		jogger.writeLog(write);
	}

	/* metodo per scrivere sul file di log */
	public static void writeLog(String write, String nameLog, Integer maxSizeBytes, String... dirLogList) throws FileLogException, LockLogException {
		Jogger jogger = new Jogger(nameLog, maxSizeBytes, dirLogList);
		jogger.writeLog(write);
	}

	/* metodo per scrivere sul file di log */
	public void writeLog(String write) throws FileLogException, LockLogException {
		if (lock) {
			try {
				if (!reentrantLock.tryLock(30, TimeUnit.SECONDS)) throw new LockLogException("Error Timeout Reentrant Lock");
			} catch (InterruptedException e) {
			}
		}
		File fLog = getLogFile(nameLog, maxSizeBytes, dirLogList);
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
			throw new FileLogException("Impossibile lavorare sul file log.\n"
										+ "Message error: " + e.getMessage());
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
	}
}
