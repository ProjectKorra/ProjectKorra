package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;

public abstract class AvatarAbility extends ElementalAbility {

	public AvatarAbility(final Player player) {
		super(player);
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public final Element getElement() {
		return Element.AVATAR;
	}

	public static void playAvatarSound(final Location loc) {
		if (ConfigManager.defaultConfig.get().getBoolean("Abilities.Avatar.AvatarState.PlaySound")) {
			final float volume = (float) ConfigManager.defaultConfig.get().getDouble("Abilities.Avatar.AvatarState.Sound.Volume");
			final float pitch = (float) ConfigManager.defaultConfig.get().getDouble("Abilities.Avatar.AvatarState.Sound.Pitch");

			Sound sound = Sound.BLOCK_ANVIL_LAND;

			try {
				sound = Sound.valueOf(ConfigManager.defaultConfig.get().getString("Abilities.Avatar.AvatarState.Sound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Abilities.Avatar.AvatarState.Sound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	/**
	 * Determines whether the ability requires the user to be an avatar in order
	 * to be able to use it. Set this to <tt>false</tt> for moves that should be
	 * able to be used without players needing to have the avatar element
	 */
	public boolean requireAvatar() {
		return true;
	}

}
