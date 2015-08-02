package com.projectkorra.ProjectKorra.firebending;

import com.projectkorra.ProjectKorra.BendingManager;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FireMethods {

	static ProjectKorra plugin;
	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public FireMethods(ProjectKorra plugin) {
		FireMethods.plugin = plugin;
	}

	public static boolean canCombustionbend(Player player) {
		if (player.hasPermission("bending.fire.combustionbending"))
			return true;
		return false;
	}

	public static boolean canLightningbend(Player player) {
		if (player.hasPermission("bending.fire.lightningbending"))
			return true;
		return false;
	}

	/**
	 * Gets the firebending dayfactor from the config multiplied by a specific
	 * value if it is day.
	 * 
	 * @param value The value
	 * @param world The world to pass into {@link #isDay(World)}
	 *            <p>
	 * @return value DayFactor multiplied by specified value when
	 *         {@link #isDay(World)} is true <br />
	 *         else <br />
	 *         value The specified value in the parameters
	 *         </p>
	 * @see {@link #getFirebendingDayAugment(World)}
	 */
	public static double getFirebendingDayAugment(double value, World world) {
		if (isDay(world)) {
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

	/**
	 * Gets the FireColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getFireColor() {
		return ChatColor.valueOf(config.getString("Properties.Chat.Colors.Fire"));
	}

	public static boolean isCombustionbendingAbility(String ability) {
		return AbilityModuleManager.combustionabilities.contains(ability);
	}

	public static boolean isLightningbendingAbility(String ability) {
		return AbilityModuleManager.lightningabilities.contains(ability);
	}

	public static boolean isDay(World world) {
		long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END)
			return true;
		if (time >= 23500 || time <= 12500) {
			return true;
		}
		return false;
	}

	public static boolean isFireAbility(String ability) {
		return AbilityModuleManager.firebendingabilities.contains(ability);
	}

	public static void playLightningbendingParticle(Location loc) {
		playLightningbendingParticle(loc, (float) Math.random(), (float) Math.random(), (float) Math.random());
	}

	public static void playLightningbendingParticle(Location loc, float xOffset, float yOffset, float zOffset) {
		loc.setX(loc.getX() + Math.random() * (xOffset / 2 - -(xOffset / 2)));
		loc.setY(loc.getY() + Math.random() * (yOffset / 2 - -(yOffset / 2)));
		loc.setZ(loc.getZ() + Math.random() * (zOffset / 2 - -(zOffset / 2)));
		GeneralMethods.displayColoredParticle(loc, "#01E1FF");
	}

	public static void playFirebendingParticles(Location loc) {
		loc.getWorld().playEffect(loc, Effect.MOBSPAWNER_FLAMES, 0, 15);
	}

	public static void playFirebendingSound(Location loc) {
		if (plugin.getConfig().getBoolean("Properties.Fire.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.FIRE, 1, 10);
		}
	}

	public static void playCombustionSound(Location loc) {
		if (plugin.getConfig().getBoolean("Properties.Fire.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.FIREWORK_BLAST, 1, -1);
		}
	}

	/**
	 * Checks whether a location is within a FireShield.
	 * 
	 * @param loc The location to check
	 * @return true If the location is inside a FireShield.
	 */
	public static boolean isWithinFireShield(Location loc) {
		List<String> list = new ArrayList<String>();
		list.add("FireShield");
		return GeneralMethods.blockAbilities(null, list, loc, 0);
	}

	public static void stopBending() {
		FireStream.removeAll(FireStream.class);
		Fireball.removeAll(Fireball.class);
		WallOfFire.removeAll(WallOfFire.class);
		Lightning.removeAll(Lightning.class);
		FireShield.removeAll(FireShield.class);
		FireBlast.removeAll(FireBlast.class);
		FireBurst.removeAll(FireBurst.class);
		FireJet.removeAll(FireJet.class);
		Cook.removeAll(Cook.class);
		Illumination.removeAll(Illumination.class);
		FireCombo.removeAll();
	}
}
