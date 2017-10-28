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
	 * Player has left clicked and hit an entity.
	 */
	LEFT_CLICK_ENTITY,
	/**
	 * For any instance of right clicking that isn't with an entity or a block
	 * (Right clicking air will not work).
	 */
	RIGHT_CLICK,
	/**
	 * Specifically for right clicking an entity.
	 */
	RIGHT_CLICK_ENTITY,
	/**
	 * Specifically for right clicking a block.
	 */
	RIGHT_CLICK_BLOCK,
	/**
	 * The shift key being pressed.
	 */
	SHIFT_DOWN,
	/**
	 * The shift key being released.
	 */
	SHIFT_UP,
	/**
	 * The item swap hand key was pressed
	 */
	OFFHAND_TRIGGER;
}
