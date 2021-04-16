package com.projectkorra.projectkorra.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {

	/**
	 * Get the given time in a formatted String.
	 *
	 * @param time Time to be formatting (milliseconds)
	 * @return Formatted time
	 */
	public static String formatTime(long time) {
		String sign = "";
		if (time < 0) {
			sign = "-";
			time = Math.abs(time);
		}
		final long days = time / TimeUnit.DAYS.toMillis(1);
		final long hours = time % TimeUnit.DAYS.toMillis(1) / TimeUnit.HOURS.toMillis(1);
		final long minutes = time % TimeUnit.HOURS.toMillis(1) / TimeUnit.MINUTES.toMillis(1);
		final long seconds = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);
		String formatted = sign;
		if (days > 0) {
			formatted += String.valueOf(days) + "d ";
		}
		if (hours > 0) {
			formatted += String.valueOf(hours) + "h ";
		}
		if (minutes > 0) {
			formatted += String.valueOf(minutes) + "m ";
		}
		if (seconds >= 0) {
			formatted += String.valueOf(seconds) + "s";
		}
		return formatted;
	}

}
