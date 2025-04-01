package com.projectkorra.projectkorra.object;

@Deprecated(since = "1.13.0", forRemoval = true)
public class PlayerFlyData {

	private final boolean canFly;
	private final boolean isFlying;

	public PlayerFlyData(final boolean canFly, final boolean isFlying) {
		this.canFly = canFly;
		this.isFlying = isFlying;
	}

	/**
	 * @return Does the player have access to fly mode?
	 */
	public boolean canFly() {
		return this.canFly;
	}

	/**
	 * @return Was the player flying?
	 */
	public boolean isFlying() {
		return this.isFlying;
	}
}
