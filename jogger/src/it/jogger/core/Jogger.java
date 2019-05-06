package it.jogger.core;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.jogger.exception.FileLogException;

public class Jogger {
	private static Jogger jogger;
	private final String FILE_LOG = "log_";
	private final String FILE_TYPE = ".log";
	private final int MAX_SIZE_BYTES = 51200;
	protected static final String LOG_DIR_PATH = Paths.get(System.getProperty("user.dir"), "log").toString();
	
	private Jogger() {
	}

	/* singleton */
	public static Jogger getInstance() {
		return (jogger = (jogger == null) ? new Jogger() : jogger);
	}

	/* metodo che ritorna path della cartella log */
	public String getLogDirPath(String... dirLog) {
		String pathDirLog = LOG_DIR_PATH;
		if (dirLog != null)
			pathDirLog = Paths.get(pathDirLog, dirLog).toString();
		
		return pathDirLog;
	}
	
	/* metodo che ritorna il file di log su cui lavorare */
	public File getLogFile(String nameLog, Integer maxSizeBytes, String... dirLog) throws FileLogException {
		String pathDirLog = getLogDirPath(dirLog);
		
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
			maxSizeBytes = (maxSizeBytes == null) ? MAX_SIZE_BYTES : maxSizeBytes;
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
	public String getLogFilePath(String nameLog, Integer maxSizeBytes, String... dirLog) throws FileLogException {
		return getLogFile(nameLog, maxSizeBytes, dirLog).getAbsolutePath();
	}

	/* metodo per scrivere sul file di log */
	public void writeLog(String write, String nameLog, Integer maxSizeBytes, String... dirLog) throws FileLogException {
		File fLog = getLogFile(nameLog, maxSizeBytes, dirLog);
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(fLog, "rw");
			raf.seek(raf.length());
			raf.writeBytes("\n" + write);
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
