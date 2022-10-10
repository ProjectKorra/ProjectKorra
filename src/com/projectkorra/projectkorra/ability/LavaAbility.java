package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class LavaAbility extends EarthAbility implements SubAbility {

	public LavaAbility(final Player player) {
		super(player);
	}

    public static void playLavabendingSound(final Location loc) {
        if (getConfig().getBoolean("Properties.Earth.PlaySound")) {
            final float volume = (float) getConfig().getDouble("Properties.Earth.LavaSound.Volume");
            final float pitch = (float) getConfig().getDouble("Properties.Earth.LavaSound.Pitch");

            Sound sound = Sound.BLOCK_LAVA_AMBIENT;

            try {
                sound = Sound.valueOf(getConfig().getString("Properties.Earth.LavaSound.Sound"));
            } catch (final IllegalArgumentException exception) {
                ProjectKorra.log.warning("Your current value for 'Properties.Earth.LavaSound.Sound' is not valid.");
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
		return Element.LAVA;
	}

}
