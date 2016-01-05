package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AbilityModuleManager;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.event.EventManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FireMethods {

	static ProjectKorra plugin;
	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static ConcurrentHashMap<Location, Information> tempFire = new ConcurrentHashMap<Location, Information>();

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
	 * Returns if fire is allowed to completely replace blocks or if it should place a temp fire
	 * block.
	 */
	public static boolean canFireGrief() {
		return config.getBoolean("Properties.Fire.FireGriefing");
	}

	/**
	 * Creates a fire block meant to replace other blocks but reverts when the fire dissipates or is
	 * destroyed.
	 */
	public static void createTempFire(Location loc) {
		if (loc.getBlock().getType() == Material.AIR) {
			loc.getBlock().setType(Material.FIRE);
			return;
		}
		Information info = new Information();
		long time = config.getLong("Properties.Fire.RevertTicks")
				+ (long) (GeneralMethods.rand.nextDouble() * config.getLong("Properties.Fire.RevertTicks")); // Generate
																												// a
																												// long
																												// between
																												// the
																												// config
																												// time
																												// and
																												// config
																												// time
																												// x
																												// 2.
																												// Just
																												// so
																												// it
																												// appears
																												// random
		if (tempFire.containsKey(loc)) {
			info = tempFire.get(loc);
		} else {
			info.setBlock(loc.getBlock());
			info.setLocation(loc);
			info.setState(loc.getBlock().getState());
		}
		info.setTime(time + System.currentTimeMillis());
		loc.getBlock().setType(Material.FIRE);
		tempFire.put(loc, info);
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
		if (isDay(world)) {
			if (GeneralMethods.hasRPG()) {
				if (EventManager.marker.get(world).equalsIgnoreCase("SozinsComet")) {
					return RPGMethods.getFactor("SozinsComet") * value;
				} else if (EventManager.marker.get(world).equalsIgnoreCase("SolarEclipse")) {
					return RPGMethods.getFactor("SolarEclipse") * value;
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

	/**
	 * Gets the FireSubColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getFireSubColor() {
		return ChatColor.valueOf(config.getString("Properties.Chat.Colors.FireSub"));
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

	public static void playFirebendingParticles(Location loc, int amount, float xOffset, float yOffset, float zOffset) {
		ParticleEffect.FLAME.display(loc, xOffset, yOffset, zOffset, 0, amount);
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

	/** Removes all temp fire that no longer needs to be there */
	public static void removeFire() {
		Iterator<Location> it = tempFire.keySet().iterator();
		while (it.hasNext()) {
			Location loc = it.next();
			Information info = tempFire.get(loc);
			if (info.getLocation().getBlock().getType() != Material.FIRE
					&& info.getLocation().getBlock().getType() != Material.AIR) {
				revertTempFire(loc);
			} else if (info.getBlock().getType() == Material.AIR || System.currentTimeMillis() > info.getTime()) {
				revertTempFire(loc);
			}
		}
	}

	/**
	 * Revert the temp fire at the location if any is there.
	 * 
	 * @param location The Location
	 * */
	@SuppressWarnings("deprecation")
	public static void revertTempFire(Location location) {
		if (!tempFire.containsKey(location))
			return;
		Information info = tempFire.get(location);
		if (info.getLocation().getBlock().getType() != Material.FIRE && info.getLocation().getBlock().getType() != Material.AIR) {
			if (info.getState().getType() == Material.RED_ROSE || info.getState().getType() == Material.YELLOW_FLOWER) {
				info.getState()
						.getBlock()
						.getWorld()
						.dropItemNaturally(info.getLocation(),
								new ItemStack(info.getState().getData().getItemType(), 1, info.getState().getRawData()));
			}
		} else {
			info.getBlock().setType(info.getState().getType());
			info.getBlock().setData(info.getState().getRawData());
		}
		tempFire.remove(location);
	}

	public static void stopBending() {
		FireStream.removeAll();
		Fireball.removeAll();
		WallOfFire.removeAll();
		Lightning.removeAll();
		FireShield.removeAll();
		FireBlast.removeAll();
		FireBurst.removeAll();
		FireJet.removeAll();
		Cook.removeAll();
		Illumination.removeAll();
		FireCombo.removeAll();
		for (Location loc : tempFire.keySet()) {
			revertTempFire(loc);
		}
	}
}
