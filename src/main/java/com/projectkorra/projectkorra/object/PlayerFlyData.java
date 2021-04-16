package com.projectkorra.projectkorra.object;

public class PlayerFlyData {

	private final boolean canFly;
	private final boolean isFlying;

	public PlayerFlyData(final boolean canFly, final boolean isFlying) {
		this.canFly = canFly;
		this.isFlying = isFlying;
	}

	/**
	 * Does the player have access to fly mode?
	 *
	 * @return
	 */
	public boolean canFly() {
		return this.canFly;
	}

	/**
	 * Was the player flying?
	 *
	 * @return
	 */
	public boolean isFlying() {
		return this.isFlying;
	}
}
