package com.projectkorra.projectkorra.ability.info;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.element.Element;
import com.projectkorra.projectkorra.player.BendingPlayer;

public interface AbilityInfo {

	/**
	 * The name of the ability is used for commands such as <b>/bending
	 * display</b> and <b>/bending help</b>. The name is also used for
	 * determining the tag for cooldowns
	 * {@link BendingPlayer#addCooldown(Ability)}, therefore if two abilities
	 * have the same name they will also share cooldowns. If two classes share
	 * the same name (SurgeWall/SurgeWave) but need to have independent
	 * cooldowns, then {@link BendingPlayer#addCooldown(String, long)} should be
	 * called explicitly.
	 *
	 * @return Returns the name of the ability
	 */
	String getName();

	Element getElement();

	/**
	 * @return true if this is a hidden ability.
	 */
	default boolean isHidden() {
		return false;
	}

	/**
	 * @return the name of the author of this AddonAbility
	 */
	default String getAuthor() {
		return "ProjectKorra";
	}

	/**
	 * @return The version of the ability as a String.
	 */
	default String getVersion() {
		return "1.0";
	}
}
