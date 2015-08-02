package com.projectkorra.projectkorra.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Logger formatter class based on bukkit's formatter.
 * 
 * @author Jacklin213
 * @version 2.1.0
 */
public class LogFormatter extends Formatter {

	private final SimpleDateFormat date = new SimpleDateFormat("MMM-dd|HH:mm:ss");

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder();
		Throwable ex = record.getThrown();

		builder.append("(");
		builder.append(date.format(record.getMillis()));
		builder.append(")");
		builder.append(" [");
		builder.append(record.getLevel().getLocalizedName().toUpperCase());
		builder.append("] ");
		builder.append(formatMessage(record));
		builder.append('\n');

		if (ex != null) {
			StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(writer));
			builder.append(writer);
		}

		return builder.toString();
	}

}
