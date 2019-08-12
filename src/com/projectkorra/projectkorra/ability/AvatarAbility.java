package com.projectkorra.projectkorra.ability;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.configuration.better.ConfigManager;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.avatar.AvatarStateConfig;

public abstract class AvatarAbility extends ElementalAbility<AvatarStateConfig> {

	public AvatarAbility(final AvatarStateConfig config, final Player player) {
		super(config, player);
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
		AvatarStateConfig avatar = ConfigManager.getConfig(AvatarStateConfig.class);
		if (avatar.PlaySound) {
			loc.getWorld().playSound(loc, avatar.SoundType, avatar.SoundVolume, avatar.SoundPitch);
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
