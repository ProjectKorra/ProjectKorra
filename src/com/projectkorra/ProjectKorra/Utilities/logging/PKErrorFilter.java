package com.projectkorra.ProjectKorra.Utilities.logging;

import java.util.logging.LogRecord;

public class PKErrorFilter extends PKFilter {

	@Override
	public boolean isLoggable(LogRecord record) {
		if (consoleError.contains(record.getMessage().replace("[ProjectKorra] ", ""))) {
			return false;
		}
		return true;
	}

}
