package com.projectkorra.ProjectKorra.Utilities.logging;

import java.util.logging.LogRecord;

/**
 * Not to be confused with {@link LogFilter}, {@code ErrorLogFilter} is
 * a subclass of {@link LogFilter}.
 * <p>
 * Can NOT and SHOULD NOT be used as a filter for {@code plugin.getLogger().getParent()}
 * If used in such way, unnecessary {@link LogRecord} may be logged to file 
 * </p>
 * 
 * @author Jacklin213
 * @version 2.0
 */
public class ErrorLogFilter extends LogFilter {

	@Override
	public boolean isLoggable(LogRecord record) {
		if (consoleError.contains(record.getMessage().replace("[ProjectKorra] ", ""))) {
			return false;
		}
		return true;
	}

}
