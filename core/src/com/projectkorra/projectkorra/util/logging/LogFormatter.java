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
	public String format(final LogRecord record) {
		final StringBuilder builder = new StringBuilder();
		final Throwable ex = record.getThrown();

		builder.append("(");
		builder.append(this.date.format(record.getMillis()));
		builder.append(")");
		builder.append(" [");
		builder.append(record.getLevel().getLocalizedName().toUpperCase());
		builder.append("] ");
		builder.append(this.formatMessage(record));
		builder.append('\n');

		if (ex != null) {
			final StringWriter writer = new StringWriter();
			ex.printStackTrace(new PrintWriter(writer));
			builder.append(writer);
		}

		return builder.toString();
	}

}
