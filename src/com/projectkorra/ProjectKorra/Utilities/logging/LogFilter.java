package com.projectkorra.ProjectKorra.Utilities.logging;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.projectkorra.ProjectKorra.ProjectKorra;

public class LogFilter implements Filter {

	List<String> consoleError = Arrays.asList(
			"###################################################",
			"##################====[ERROR]====##################",
			"              An error has been caught",
			" Please check the ERROR.log file for stack trace.",
			"   Create a bug report with the log contents at.",
			"http://projectkorra.com/forum/forums/bug-reports.6/",
			"##################====[ERROR]====##################",
			"###################################################"
	);
	
	@Override
	public boolean isLoggable(LogRecord record) {
		if (record.getLevel() == Level.SEVERE && record.getMessage().contains("ProjectKorra") && record.getThrown() != null) {
			for (String line : consoleError) {
				ProjectKorra.log.severe(line);
			}
			ProjectKorra.handler.publish(record);
			ProjectKorra.handler.flush();
			return false;
		}
		return true;
	}

}
