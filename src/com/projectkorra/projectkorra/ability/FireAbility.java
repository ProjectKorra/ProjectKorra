package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.rpg.RPGMethods;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FireAbility extends ElementalAbility {
	
	private static final Map<Location, Information> TEMP_FIRE = new ConcurrentHashMap<Location, Information>();
	private static final Material[] IGNITABLE_MATERIALS = { Material.BEDROCK, Material.BOOKSHELF, Material.BRICK, Material.CLAY, Material.CLAY_BRICK,
			Material.COAL_ORE, Material.COBBLESTONE, Material.DIAMOND_ORE, Material.DIAMOND_BLOCK, Material.DIRT,
			Material.ENDER_STONE, Material.GLOWING_REDSTONE_ORE, Material.GOLD_BLOCK, Material.GRAVEL, Material.GRASS,
			Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2, Material.LAPIS_BLOCK, Material.LAPIS_ORE, Material.LOG,
			Material.MOSSY_COBBLESTONE, Material.MYCEL, Material.NETHER_BRICK, Material.NETHERRACK, Material.OBSIDIAN,
			Material.REDSTONE_ORE, Material.SAND, Material.SANDSTONE, Material.SMOOTH_BRICK, Material.STONE, Material.SOUL_SAND,
			Material.WOOD, Material.WOOL, Material.LEAVES, Material.LEAVES_2, Material.MELON_BLOCK, Material.PUMPKIN,
			Material.JACK_O_LANTERN, Material.NOTE_BLOCK, Material.GLOWSTONE, Material.IRON_BLOCK, Material.DISPENSER,
			Material.SPONGE, Material.IRON_ORE, Material.GOLD_ORE, Material.COAL_BLOCK, Material.WORKBENCH,
			Material.HAY_BLOCK, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.EMERALD_ORE,
			Material.EMERALD_BLOCK, Material.REDSTONE_BLOCK, Material.QUARTZ_BLOCK, Material.QUARTZ_ORE,
			Material.STAINED_CLAY, Material.HARD_CLAY };
		
	public FireAbility(Player player) {
		super(player);
	}
	
	@Override
	public boolean isIgniteAbility() {
		return true;
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return true;
	}

	@Override
	public Element getElement() {
		return Element.FIRE;
	}
		
	public double getDayFactor(double value) {
		return player != null ? getDayFactor(value, player.getWorld()) : 1;
	}
		
	/**
	 * Returns if fire is allowed to completely replace blocks or if it should place a temp fire
	 * block.
	 */
	public static boolean canFireGrief() {
		return getConfig().getBoolean("Properties.Fire.FireGriefing");
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
		long time = getConfig().getLong("Properties.Fire.RevertTicks") 
				+ (long) ((new Random()).nextDouble() * getConfig().getLong("Properties.Fire.RevertTicks"));
		if (TEMP_FIRE.containsKey(loc)) {
			info = TEMP_FIRE.get(loc);
		} else {
			info.setBlock(loc.getBlock());
			info.setLocation(loc);
			info.setState(loc.getBlock().getState());
		}
		info.setTime(time + System.currentTimeMillis());
		loc.getBlock().setType(Material.FIRE);
		TEMP_FIRE.put(loc, info);
	}

	public static double getDayFactor() {
		return getConfig().getDouble("Properties.Fire.DayFactor");
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
	public static double getDayFactor(double value, World world) {
		if (isDay(world)) {
			if (GeneralMethods.hasRPG()) {
				if (isSozinsComet(world)) {
					return RPGMethods.getFactor("SozinsComet") * value;
				} else if (isLunarEclipse(world)) {
					return RPGMethods.getFactor("SolarEclipse") * value;
				} else {
					return value * getDayFactor();
				}
			} else {
				return value * getDayFactor();
			}
		}
		return value;
	}
	
	public static ChatColor getSubChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.FireSub"));
	}

	public static boolean isIgnitable(Block block) {
		return block != null ? isIgnitable(block.getType()) : false;
	}
	
	public static boolean isIgnitable(Material material) {
		return Arrays.asList(IGNITABLE_MATERIALS).contains(material);
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

	public static void playCombustionSound(Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.FIREWORK_BLAST, 1, -1);
		}
	}

	public static void playFirebendingParticles(Location loc, int amount, float xOffset, float yOffset, float zOffset) {
		ParticleEffect.FLAME.display(loc, xOffset, yOffset, zOffset, 0, amount);
	}

	public static void playFirebendingSound(Location loc) {
		if (getConfig().getBoolean("Properties.Fire.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.FIRE, 1, 10);
		}
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

	/** Removes all temp fire that no longer needs to be there */
	public static void removeFire() {
		Iterator<Location> it = TEMP_FIRE.keySet().iterator();
		while (it.hasNext()) {
			Location loc = it.next();
			Information info = TEMP_FIRE.get(loc);
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
		if (!TEMP_FIRE.containsKey(location)) {
			return;
		}
		Information info = TEMP_FIRE.get(location);
		if (info.getLocation().getBlock().getType() != Material.FIRE && info.getLocation().getBlock().getType() != Material.AIR) {
			if (info.getState().getType() == Material.RED_ROSE || info.getState().getType() == Material.YELLOW_FLOWER) {
				ItemStack itemStack = new ItemStack(info.getState().getData().getItemType(), 1, info.getState().getRawData());
				info.getState().getBlock().getWorld().dropItemNaturally(info.getLocation(), itemStack);
			}
		} else {
			info.getBlock().setType(info.getState().getType());
			info.getBlock().setData(info.getState().getRawData());
		}
		TEMP_FIRE.remove(location);
	}

	public static void stopBending() {
		BlazeArc.removeAllCleanup();
		for (Location loc : TEMP_FIRE.keySet()) {
			revertTempFire(loc);
		}
	}
	
}
