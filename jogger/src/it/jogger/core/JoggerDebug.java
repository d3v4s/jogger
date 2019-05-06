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

public class JoggerDebug {
	private static JoggerDebug joggerDebug;
	private final String FILE_LOG = "log_debug-";
	private final String FILE_TYPE = ".log";
	private final String LOG_DIR_PATH = Paths.get(Jogger.LOG_DIR_PATH, "debug").toString();
	private final int MAX_SIZE_BYTES = 51200;

	private JoggerDebug() {
		super();
	}

	/* singleton */
	public static JoggerDebug getInstance() {
		return joggerDebug = (joggerDebug == null) ? new JoggerDebug() : joggerDebug;
	}

	/* metodo che ritorna path della cartella log */
	public String getLogDirPath() {
		return LOG_DIR_PATH;
	}

	/* metodo che ritorna il file di log su cui lavorare */
	public File getLogFile(String nameLog, Integer maxSizeBytes) throws FileLogException {
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
						throw new FileLogException("Impossibile lavorare sul file log, controllare i permessi");
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
			maxSizeBytes = (maxSizeBytes == null) ? MAX_SIZE_BYTES : maxSizeBytes;
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
	public String getLogFilePath(String nameLog, Integer maxSizeBytes) throws FileLogException {
		return getLogFile(nameLog, maxSizeBytes).getAbsolutePath();
	}

	/* metodo per scrivere sul file di log */
	public void writeLog(String write, String nameLog, Integer maxSizeBytes) throws FileLogException {
		File fLog = getLogFile(nameLog, maxSizeBytes);
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
