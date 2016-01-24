package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public abstract class AvatarAbility extends ElementalAbility {
	
	public AvatarAbility(Player player) {
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
	
	public static void playAvatarSound(Location loc) {
		loc.getWorld().playSound(loc, Sound.ANVIL_LAND, 1, 10);
	}

}
