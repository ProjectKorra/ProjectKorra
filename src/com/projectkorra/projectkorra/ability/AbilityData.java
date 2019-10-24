package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ability.loader.AbilityLoader;
import com.projectkorra.projectkorra.player.BendingPlayer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AbilityData {

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
	String name();

	/**
	 * @return the name of the author of this AddonAbility
	 */
	String author() default "ProjectKorra";

	/**
	 * @return The version of the ability as a String.
	 */
	String version() default "1.0";

	/**
	 * @return The class used to register this ability.
	 */
	Class<? extends AbilityLoader> abilityLoader();
}
