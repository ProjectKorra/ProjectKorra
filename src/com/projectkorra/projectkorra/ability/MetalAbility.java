package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class MetalAbility extends EarthAbility implements SubAbility {

	public MetalAbility(final Player player) {
		super(player);
	}

    public static void playMetalbendingSound(final Location loc) {
        if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
            final float volume = (float) getConfig().getDouble("Properties.Earth.MetalSound.Volume");
            final float pitch = (float) getConfig().getDouble("Properties.Earth.MetalSound.Pitch");

            Sound sound = Sound.ENTITY_IRON_GOLEM_HURT;

            try {
                sound = Sound.valueOf(getConfig().getString("Properties.Earth.MetalSound.Sound"));
            } catch (final IllegalArgumentException exception) {
                ProjectKorra.log.warning("Your current value for 'Properties.Earth.MetalSound.Sound' is not valid.");
            } finally {
                loc.getWorld().playSound(loc, sound, volume, pitch);
            }
        }
    }

    @Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.METAL;
	}

}
