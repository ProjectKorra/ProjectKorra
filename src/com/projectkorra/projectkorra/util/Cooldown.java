package com.projectkorra.projectkorra.util;

public class Cooldown {

	/**
	 * The amount of time the cooldown is valid including the system time when
	 * the cooldown was created
	 */
	private long cooldown;
	/** If the cooldown should be saved in the database */
	private boolean database;

	public Cooldown(long cooldown, boolean database) {
		this.cooldown = cooldown;
		this.database = database;
	}

	public long getCooldown() {
		return cooldown;
	}

	public boolean isDatabase() {
		return database;
	}

}
