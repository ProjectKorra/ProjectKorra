package com.projectkorra.projectkorra.util.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Main handler used to listen to LogRecords and logs them.
 * <p>
 * Should be injected into plugin.getLogger().getParent(), <br>
 * if used anywhere else the handler may not work to expected.
 * </p>
 * Current Handler settings:
 * <ul>
 * <li>Level - Set to log {@link Level#WARNING warnings} and {@link Level#SEVERE
 * errors}</li>
 * <li>Formatter - {@link LogFormatter}</li>
 * </ul>
 * 
 * @author Jacklin213
 * @version 2.1.0
 */
public class PKLogHandler extends FileHandler {

	public PKLogHandler(String filename) throws IOException {
		super(filename, 500 * 1024, 20, true);
		this.setLevel(Level.WARNING);
		this.setFilter(new LogFilter());
		this.setFormatter(new LogFormatter());
	}

	@Override
	public synchronized void publish(LogRecord record) {
		super.publish(record);
		flush();
	}

}
