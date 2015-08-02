package com.projectkorra.projectkorra.util.logging;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Bukkit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.LogRecord;

/**
 * This class should only be used to set {@link PKLogHandler}'s filter.
 * 
 * @author Jacklin213
 * @version 2.1.0
 */
public class LogFilter implements Filter {

	private List<String> loggedRecords = new ArrayList<>();

	@Override
	public boolean isLoggable(LogRecord record) {
		if (record.getMessage() == null && record.getThrown() == null) {
			return false;
		}
		String recordString = "";
		if (record.getMessage() != null) {
			if (!record.getMessage().contains("ProjectKorra")) {
				if (record.getThrown() == null) {
					return false;
				}
				if (record.getThrown().getMessage() == null) {
					return false;
				}
				if (!record.getThrown().getMessage().contains("ProjectKorra")) {
					return false;
				}
				// record message doesnt have ProjectKorra but throwable does
			}
			recordString = buildString(record);
		} else {
			if (record.getThrown() != null) {
				if (record.getThrown().getMessage() == null) {
					return false;
				}
				if (!record.getThrown().getMessage().contains("ProjectKorra")) {
					return false;
				}
				// record message null but throwable has ProjectKorra
				recordString = buildString(record);
			}
		}

		if (loggedRecords.contains(recordString)) {
			// Logged records contains record 
			return false;
		}

		final String toRecord = recordString;
		Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, new Runnable() {
			public void run() {
				loggedRecords.add(toRecord);
			}
		}, 10);
		return true;
	}

	private String buildString(LogRecord record) {
		StringBuilder builder = new StringBuilder();
		if (record.getMessage() != null) {
			builder.append(record.getMessage());
		}
		if (record.getThrown() != null) {
			StringWriter writer = new StringWriter();
			record.getThrown().printStackTrace(new PrintWriter(writer));
			builder.append(writer);
		}

		return builder.toString();
	}
}
