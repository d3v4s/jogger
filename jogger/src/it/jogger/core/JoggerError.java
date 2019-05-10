package it.jogger.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
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

import it.jogger.exception.FileLogException;
import it.jogger.exception.LockLogException;

public class JoggerError {
	private final String FILE_LOG = "log_error-";
	private final String FILE_TYPE = ".log";
	private static final String LOG_DIR_PATH = Paths.get(Jogger.LOG_DIR_PATH, "error").toString();
	private int maxSizeBytes = 51200;
	private String nameLog = "jogger";
	private boolean lock = false;
	private final ReentrantLock reentrantLock = new ReentrantLock();

	/* costruttori */
	public JoggerError() {
	}
	public JoggerError(String nameLog) {
		this.nameLog = nameLog;
	}
	public JoggerError(String nameLog, Integer maxSizeBytes) {
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

	/* metodo che ritorna la cartella dei log */
	public static String getLogDirPath() {
		return LOG_DIR_PATH;
	}

	/* metodo che ritorna il file di log su cui lavorare */
	public static File getLogFile(String nameLog) throws FileLogException {
		JoggerError joggerError = new JoggerError(nameLog);
		return joggerError.getLogFile();
	}

	/* metodo che ritorna il file di log su cui lavorare */
	public static File getLogFile(String nameLog, Integer maxSizeBytes) throws FileLogException {
		JoggerError joggerError = new JoggerError(nameLog, maxSizeBytes);
		return joggerError.getLogFile();
	}

	/* metodo che ritorna il file di log su cui lavorare */
	public File getLogFile() throws FileLogException {
		File fileDirLog = new File(LOG_DIR_PATH);
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
				fileLog = Paths.get(LOG_DIR_PATH, nameLog + "000000" + FILE_TYPE).toFile();
				try {
					if (!fileLog.createNewFile())
						throw new FileLogException("Impossibile lavorare sul file log, controllare i permessi.");
				} catch (IOException e) {
					throw new FileLogException("Impossibile lavorare sul file log.\n"
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
				
				fileLog = Paths.get(LOG_DIR_PATH, nameLog + nLog + FILE_TYPE).toFile();
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
	public static String getLogFilePath(String nameLog) throws FileLogException {
		return getLogFile(nameLog).getAbsolutePath();
	}

	/* metodo che ritorna path del file log da usare */
	public static String getLogFilePath(String nameLog, Integer maxSizeBytes) throws FileLogException {
		return getLogFile(nameLog, maxSizeBytes).getAbsolutePath();
	}
	
	/* metodo che ritorna path del file log da usare */
	public String getLogFilePath() throws FileLogException {
		return getLogFile().getAbsolutePath();
	}

	/* metodo per scrivere sul file di log un'eccezione */
	public static void writeLog(Exception exception, String nameLog) throws FileLogException, LockLogException {
		JoggerError joggerError = new JoggerError(nameLog);
		joggerError.writeLog(exception);
	}

	/* metodo per scrivere sul file di log un'eccezione */
	public static void writeLog(Exception exception, String nameLog, Integer maxSizeBytes) throws FileLogException, LockLogException {
		JoggerError joggerError = new JoggerError(nameLog, maxSizeBytes);
		joggerError.writeLog(exception);
	}

	/* metodo per scrivere sul file di log un'eccezione */
	public void writeLog(Exception exception) throws FileLogException, LockLogException {
		if (lock) {
			try {
				if (!reentrantLock.tryLock(30, TimeUnit.SECONDS)) throw new LockLogException("Error Timeout Reentrant Lock");
			} catch (InterruptedException e) {
				return;
			}
		}
		File fLog = getLogFile(nameLog, maxSizeBytes);
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
			throw new FileLogException("Impossibile lavorare sul file log.\n"
										+ "Message error: " + e.getMessage());
		} finally {
			pwLog.close();
			if (lock) reentrantLock.unlock();
		}
	}

	/* metodo che legge il file log e ritorna stringa */
	private String readLogFile(File file) throws FileLogException {
		StringBuffer textOut = new StringBuffer();
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			raf.seek(0);
			while(raf.getFilePointer() < raf.length())
				textOut = textOut.append(raf.readLine() + (raf.getFilePointer() == raf.length()-1 ? "" : "\n"));
		} catch (IOException e) {
			try {
				raf.close();
			} catch (IOException e1) {
			}
			throw new FileLogException("Impossibile lavorare sul file.\n"
										+ "Message error: " + e.getMessage());
		} finally {
			try {
				raf.close();
			} catch (IOException e) {
			}
		}
		return textOut.toString();
	}
}
