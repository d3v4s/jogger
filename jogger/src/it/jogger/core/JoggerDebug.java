package it.jogger.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JoggerDebug {
	public static final String LOG_DIR_PATH = Paths.get(Jogger.LOG_DIR_PATH, "debug").toString();
	private final ReentrantLock reentrantLock = new ReentrantLock();
	private final String FILE_LOG = "log_debug-";
	private final String FILE_TYPE = ".log";
	private boolean lock = false;
	private boolean debug = true;
	private int maxSizeBytes = 51200;
	private String nameLog = "jogger";

	/* costruttori */
	public JoggerDebug() {
	}
	public JoggerDebug(String nameLog) {
		this.nameLog = nameLog;
	}
	public JoggerDebug(String nameLog, Integer maxSizeBytes) {
		this.nameLog = nameLog;
		this.maxSizeBytes = maxSizeBytes;
	}

	/* get e set */
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
	public boolean isLock() {
		return lock;
	}
	public void setLock(boolean lock) {
		this.lock = lock;
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

//	/* metodo che ritorna path della cartella log */
//	public static String getLogDirPath() {
//		return LOG_DIR_PATH;
//	}
//
//	/* metodo che ritorna il file di log su cui lavorare */
//	public static File getLogFile(String nameLog) throws FileLogException {
//		JoggerDebug joggerDebug = new JoggerDebug(nameLog);
//		return joggerDebug.getLogFile();
//	}
//
//	/* metodo che ritorna il file di log su cui lavorare */
//	public static File getLogFile(String nameLog, Integer maxSizeBytes) throws FileLogException {
//		JoggerDebug joggerDebug = new JoggerDebug(nameLog, maxSizeBytes);
//		return joggerDebug.getLogFile();
//	}

	/* metodo che ritorna il file di log su cui lavorare */
	public File getLogFile() {
		File fileDirLog = new File(LOG_DIR_PATH);
		File fileLog = null;
		if (fileDirLog.exists() && fileDirLog.isFile())
			throw new RuntimeException("Impossibile lavorare sulla cartella 'log', controllare i permessi o che non ci sia un file con lo stesso nome.");
		else if (!fileDirLog.exists())
			fileDirLog.mkdirs();

		String fileNameLog = FILE_LOG + nameLog + "-";
		if (fileDirLog.isDirectory()) {
			String regex = fileNameLog + "([\\d]{6})" + FILE_TYPE;
			ArrayList<String> listFileLog = new ArrayList<String>();
			String[] lfl = fileDirLog.list();
			for (String fname : lfl)
				if (Pattern.matches(regex, fname))
						listFileLog.add(fname);

			if (listFileLog.isEmpty()) {
				fileLog = Paths.get(LOG_DIR_PATH, fileNameLog + "000000" + FILE_TYPE).toFile();
				try {
					if (!fileLog.createNewFile())
						throw new RuntimeException("Impossibile lavorare sul file log, controllare i permessi");
				} catch (IOException e) {
					throw new RuntimeException("Impossibile lavorare sul file log.\n"
												+ "Message error: " + e.getMessage());
				}
			} else {
				Collections.sort(listFileLog);
				Collections.reverse(listFileLog);
				fileLog = Paths.get(LOG_DIR_PATH, listFileLog.get(0)).toFile();
			}

			Long sizeLogByte = fileLog.length();
			if (sizeLogByte > maxSizeBytes) {
				Matcher m = Pattern.compile(regex).matcher(listFileLog.get(0));
				m.find();
				String nLog = String.format("%06d", Integer.valueOf(m.group(1)) + 1);
				
				fileLog = Paths.get(LOG_DIR_PATH, fileNameLog + nLog + FILE_TYPE).toFile();
				try {
					if (!fileLog.createNewFile())
						throw new RuntimeException("Impossibile lavorare sul file log, controllare i permessi.");
				} catch (IOException e) {
					throw new RuntimeException("Impossibile lavorare sul file log.\n"
												+ "Message error: " + e.getMessage());
				}
			}
		}
		return fileLog;
	}

//	/* metodo che ritorna path del file log da usare */
//	public static String getLogFilePath(String nameLog) throws FileLogException {
//		return getLogFile(nameLog).getAbsolutePath();
//	}
//
//	/* metodo che ritorna path del file log da usare */
//	public static String getLogFilePath(String nameLog, Integer maxSizeBytes) throws FileLogException {
//		return getLogFile(nameLog, maxSizeBytes).getAbsolutePath();
//	}

	/* metodo che ritorna path del file log da usare */
	public String getLogFilePath() {
		return getLogFile().getAbsolutePath();
	}

//	/* metodo per scrivere sul file di log */
//	public static void writeLog(String write, String nameLog) throws FileLogException, LockLogException {
//		JoggerDebug joggerDebug = new JoggerDebug(nameLog);
//		joggerDebug.writeLog(write);
//	}
//
//	/* metodo per scrivere sul file di log */
//	public static void writeLog(String write, String nameLog, Integer maxSizeBytes) throws FileLogException, LockLogException {
//		JoggerDebug joggerDebug = new JoggerDebug(nameLog, maxSizeBytes);
//		joggerDebug.writeLog(write);
//	}

	/* metodo per scrivere sul file di log */
	public void writeLog(String write) {
		if (debug) {
			if (lock) {
				try {
					if (!reentrantLock.tryLock(30, TimeUnit.SECONDS)) throw new RuntimeException("Error Timeout Reentrant Lock");
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
				throw new RuntimeException("Impossibile lavorare sul file log.\n"
						+ "Message error: " + e.getMessage());
			} finally {
				try {
					raf.close();
				} catch (IOException e) {
				}
				if (lock) reentrantLock.unlock();
			}
		}
	}
}
