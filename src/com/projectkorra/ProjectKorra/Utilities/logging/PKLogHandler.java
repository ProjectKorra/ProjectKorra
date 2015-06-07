package com.projectkorra.ProjectKorra.Utilities.logging;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

/**
 * Main handler used to listen to LogRecords and logs them.
 * <p>
 * Should be injected into plugin.getLogger().getParent(),
 * <br>
 * if used anywhere else the handler may not work to expected.
 * </p>
 * Current Handler settings:
 * <ul>
 *   <li>Level - Set to log {@link Level#WARNING warnings} and {@link Level#SEVERE errors}</li>
 *   <li>Filter - {@link ErrorLogFilter}</li>
 *   <li>Formatter - {@link LogFormatter}</li>
 * </ul>
 * @author Jacklin213
 * @version 2.0
 */
public class PKLogHandler extends FileHandler {

	public PKLogHandler(String filename) throws IOException {
		super(filename, true);
		this.setLevel(Level.WARNING);
		this.setFilter(new ErrorLogFilter());
		this.setFormatter(new LogFormatter());
	}

}
