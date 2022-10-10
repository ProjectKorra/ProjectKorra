package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class IceAbility extends WaterAbility implements SubAbility {

	public IceAbility(final Player player) {
		super(player);
	}

	public static void playIcebendingSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Water.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Water.IceSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Water.IceSound.Pitch");

			Sound sound = Sound.ITEM_FLINTANDSTEEL_USE;

			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Water.IceSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Water.IceSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.ICE;
	}

}
