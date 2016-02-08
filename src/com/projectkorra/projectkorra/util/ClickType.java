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
	 * The player has jumped
	 */
	JUMP,
	/**
	 * Player has swung their arm.
	 */
	LEFT_CLICK,
	/**
	 * The player has moved backwards
	 */
	MOVE_BACKWARDS,
	/**
	 * The player has moved forwards
	 */
	MOVE_FORWARDS,
	/**
	 * The player has moved left
	 */
	MOVE_LEFT,
	/**
	 * The player has moved right
	 */
	MOVE_RIGHT,
	/**
	 * For any instance of right clicking that isn't with an entity or a block (Right clicking air will not work).
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
	 * The shift key being released.
	 */
	SHIFT_DOWN, 
	/**
	 * The shift key being pressed.
	 */
	SHIFT_UP;
}
