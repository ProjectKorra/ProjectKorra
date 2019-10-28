package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.ability.info.AbilityInfo;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.AbilityConfig;
import com.projectkorra.projectkorra.configuration.configs.abilities.avatar.AvatarStateConfig;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class AvatarAbility<Info extends AbilityInfo, C extends AbilityConfig> extends ElementalAbility<Info, C> {

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

	public static void playAvatarSound(final Location loc) {
		AvatarStateConfig avatar = ConfigManager.getConfig(AvatarStateConfig.class);
		if (avatar.PlaySound) {
			loc.getWorld().playSound(loc, avatar.SoundType, avatar.SoundVolume, avatar.SoundPitch);
		}
	}
}
