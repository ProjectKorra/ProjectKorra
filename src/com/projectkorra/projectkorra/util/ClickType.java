package com.projectkorra.projectkorra.util;

/**
 * An enum representation of the ways in which an ability can be activated.
 */
public enum ClickType {
	/**
	 * Use this to call your own click type.
	 */
	CUSTOM,
	/**
	 * Player has swung their arm.
	 */
	LEFT_CLICK,
	/**
	 * Player has swung their arm and hit a block.
	 */
	LEFT_CLICK_BLOCK,
	/**
	 * Player has right clicked.
	 */
	RIGHT_CLICK,
	/**
	 * Player has right clicked a block.
	 */
	RIGHT_CLICK_BLOCK,
	/**
	 * The shift key being released.
	 */
	SHIFT_DOWN,
	/**
	 * The shift key being pressed.
	 */
	SHIFT_UP;
}
