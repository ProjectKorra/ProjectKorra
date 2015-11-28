package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.BendingManager;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.firebending.FireMethods;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public abstract class FireAbility extends CoreAbility {

	private double dayFactor;
	
	public FireAbility(Player player, boolean autoStart) {
		super(player, autoStart);
		this.dayFactor = FireAbility.getFirebendingDayAugment(1, player.getWorld());
	}

	public FireAbility(Player player) {
		this(player, false);
	}
	
	public double getDayFactor() {
		return dayFactor;
	}
	
	public double getDayFactor(double value) {
		return dayFactor * value;
	}

	@Override
	public final String getElementName() {
		return "Fire";
	}

	@Override
	public final ChatColor getElementColor() {
		return getFireColor();
	}
	
	/**
	 * Gets the FireColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getFireColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Fire"));
	}
	
	public static boolean canFireGrief() {
		return ConfigManager.getConfig().getBoolean("Properties.Fire.FireGriefing");
	}
	
	/**
	 * Gets the firebending dayfactor from the config multiplied by a specific value if it is day.
	 * 
	 * @param value The value
	 * @param world The world to pass into {@link #isDay(World)}
	 * @return value DayFactor multiplied by specified value when {@link #isDay(World)} is true <br />
	 *         else <br />
	 *         value The specified value in the parameters
	 */
	public static double getFirebendingDayAugment(double value, World world) {
		FileConfiguration config = ConfigManager.getConfig();
		if (FireMethods.isDay(world)) {			// TODO move the isDay method
			if (GeneralMethods.hasRPG()) {
				if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.SozinsComet.toString())) {
					return RPGMethods.getFactor(WorldEvents.SozinsComet) * value;
				} else if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.SolarEclipse.toString())) {
					return RPGMethods.getFactor(WorldEvents.SolarEclipse) * value;
				} else {
					return value * config.getDouble("Properties.Fire.DayFactor");
				}
			} else {
				return value * config.getDouble("Properties.Fire.DayFactor");
			}
		}
		return value;
	}
	
}
