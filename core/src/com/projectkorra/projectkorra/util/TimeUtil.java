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
		final long ms = time % 1000;
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
		if (seconds > 0) {
			formatted += String.valueOf(seconds) + "s";
		}
		if (ms > 0 && (formatted.equals("") || formatted.equals("-"))) {
			formatted += "0." + String.valueOf(ms / 100) + "s";
		}
		if (formatted.length() == 0) return "0s";
		return formatted;
	}

	/**
	 * Get a formatted time String and convert it back into Milliseconds
	 *
	 * @param formattedTime Time to be formatting
	 * @return Formatted time in milliseconds
	 */
	public static long unformatTime(String formattedTime) {
		long time = 0;
		formattedTime = formattedTime.toLowerCase();
		if (formattedTime.contains("d")) {
			time += TimeUnit.DAYS.toMillis(Integer.parseInt(formattedTime.split("d")[0].trim()));
			formattedTime = formattedTime.split("d", -1)[1].trim();
		}
		if (formattedTime.contains("h")) {
			time += TimeUnit.HOURS.toMillis(Integer.parseInt(formattedTime.split("h")[0].trim()));
			formattedTime = formattedTime.split("h", -1)[1].trim();
		}
		if (formattedTime.contains("ms")) {
			time += Long.parseLong(formattedTime.split("ms")[0].trim());
			formattedTime = formattedTime.split("ms", -1)[1].trim();
		}
		if (formattedTime.contains("m")) {
			time += TimeUnit.MINUTES.toMillis(Integer.parseInt(formattedTime.split("m")[0].trim()));
			formattedTime = formattedTime.split("m", -1)[1].trim();
		}
		if (formattedTime.contains("s")) {
			time += TimeUnit.SECONDS.toMillis(Integer.parseInt(formattedTime.split("s")[0].trim()));
			formattedTime = formattedTime.split("s", -1)[1].trim();
		}
		if (!formattedTime.equals(""))
			time += Long.parseLong(formattedTime.trim());
		return time;
	}

}
