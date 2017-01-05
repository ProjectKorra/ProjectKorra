package com.projectkorra.projectkorra.object;

public class PlayerFlyData {
	
	private boolean canFly;
	private boolean isFlying;
	
	public PlayerFlyData(boolean canFly, boolean isFlying) {
		this.canFly = canFly;
		this.isFlying = isFlying;
	}
	
	/**
	 * Does the player have access to fly mode?
	 * @return
	 */
	public boolean canFly() {
		return canFly;
	}
	
	/**
	 * Was the player flying?
	 * @return
	 */
	public boolean isFlying() {
		return isFlying;
	}
}