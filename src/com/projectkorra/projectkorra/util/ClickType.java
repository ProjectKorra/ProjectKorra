package com.projectkorra.projectkorra.util;

/**
 * An enum representation of the ways in which an ability can be activated.
 * @author kingbirdy
 *
 */
public enum ClickType {
	SHIFT, 
	/**
	 * The shift key being released
	 */
	SHIFT_DOWN, 
	/**
	 * The shift key being pressed
	 */
	SHIFT_UP, 
	CLICK, 
	LEFT_CLICK, 
	RIGHT_CLICK
}
