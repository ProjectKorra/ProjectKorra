package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;

public abstract class CombustionAbility extends FireAbility implements SubAbility {

	public CombustionAbility(final Player player) {
		super(player);
	}

	public static void playCombustionSound(final Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			final float volume = (float) getConfig().getDouble("Properties.Fire.CombustionSound.Volume");
			final float pitch = (float) getConfig().getDouble("Properties.Fire.CombustionSound.Pitch");

			Sound sound = Sound.ENTITY_FIREWORK_ROCKET_BLAST;
			try {
				sound = Sound.valueOf(getConfig().getString("Properties.Fire.CombustionSound.Sound"));
			} catch (final IllegalArgumentException exception) {
				ProjectKorra.log.warning("Your current value for 'Properties.Fire.CombustionSound.Sound' is not valid.");
			} finally {
				loc.getWorld().playSound(loc, sound, volume, pitch);
			}
		}
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}

	@Override
	public Element getElement() {
		return Element.COMBUSTION;
	}

	@Override
	public boolean isExplosiveAbility() {
		return true;
	}

	//Overriding these methods to make sure Combustion abilities don't get buffed by blue fire
	@Override
	public double applyModifiersDamage(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public double applyModifiersRange(double value) {
		return GeneralMethods.applyModifiers(value, getDayFactor(1.0));
	}

	@Override
	public long applyModifiersCooldown(long value) {
		return (long) GeneralMethods.applyInverseModifiers(value, getDayFactor(1.0));
	}

}
