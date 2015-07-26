package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.Ability.AbilityModuleManager;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.BendingManager;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Utilities.BlockSource;
import com.projectkorra.ProjectKorra.chiblocking.ChiMethods;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;

public class WaterMethods {
	
	static ProjectKorra plugin;
	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	private static Integer[] plantIds = { 6, 18, 31, 37, 38, 39, 40, 59, 81, 83, 86, 99, 100, 103, 104, 105, 106, 111, 161, 175};
	
	public WaterMethods(ProjectKorra plugin) {
		WaterMethods.plugin = plugin;
	}
	
	/**
	 * Checks to see if a Player is effected by BloodBending.
	 * @param player The player to check
	 * <p>
	 * @return true If {@link #isChiBlocked(String)} is true
	 * <br />
	 * false If player is BloodBender and Bending is toggled on, or if player is in AvatarState
	 * </p>
	 */
	public static boolean canBeBloodbent(Player player) {
		if (AvatarState.isAvatarState(player))
			if (ChiMethods.isChiBlocked(player.getName()))
				return true;
        return !(GeneralMethods.canBend(player.getName(), "Bloodbending")
                && !GeneralMethods.getBendingPlayer(player.getName()).isToggled());
    }
	
	/**
	 * Checks to see if a player can BloodBend.
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.bloodbending"
	 */
	public static boolean canBloodbend(Player player) {
        return player.hasPermission("bending.water.bloodbending");
    }

	public static boolean canBloodbendAtAnytime(Player player){
        return canBloodbend(player) && player.hasPermission("bending.water.bloodbending.anytime");
    }

    public static boolean canIcebend(Player player) {
        return player.hasPermission("bending.water.icebending");
    }

    public static boolean canWaterHeal(Player player){
        return player.hasPermission("bending.water.healing");
    }

    /**
	 * Checks to see if a player can PlantBend.
	 * @param player The player to check
	 * @return true If player has permission node "bending.ability.plantbending"
	 */
	public static boolean canPlantbend(Player player) {
		return player.hasPermission("bending.water.plantbending");
	}
	
	public static double getWaterbendingNightAugment(World world) {
		if (GeneralMethods.hasRPG()) {
			if (isNight(world)) {
				if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.LunarEclipse.toString())) {
					return RPGMethods.getFactor(WorldEvents.LunarEclipse);
				}
				else if (BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) {
					return config.getDouble("Properties.Water.FullMoonFactor");
				}
				return config.getDouble("Properties.Water.NightFactor");
			} else {
				return 1;
			}
		} else {
			if (isNight(world) && BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) return config.getDouble("Properties.Water.FullMoonFactor");
			if (isNight(world)) return config.getDouble("Properties.Water.NightFactor");
			return 1;
		}
	}
	
	/**
	 * Gets the WaterColor from the config.
	 * @return Config specified ChatColor
	 */
	public static ChatColor getWaterColor() {
		return ChatColor.valueOf(config.getString("Properties.Chat.Colors.Water"));
	}
	
	/**
	 * Finds a valid Water source for a Player. To use dynamic source selection, use
	 * BlockSource.getWaterSourceBlock() instead of this method. Dynamic source selection
	 * saves the user's previous source for future use.
	 * {@link BlockSource#getWaterSourceBlock(Player, double)}
	 * @param player the player that is attempting to Waterbend.
	 * @param range the maximum block selection range.
	 * @param plantbending true if the player can bend plants.
	 * @return a valid Water source block, or null if one could not be found.
	 */
	@SuppressWarnings("deprecation")
	public static Block getWaterSourceBlock(Player player, double range,
			boolean plantbending) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i))
					.getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation",
					location))
				continue;
			if (isWaterbendable(block, player)
					&& (!isPlant(block) || plantbending)) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock tb = TempBlock.get(block);
					byte full = 0x0;
					if (tb.getState().getRawData() != full
							&& (tb.getState().getType() != Material.WATER || tb.getState()
							.getType() != Material.STATIONARY_WATER)) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}
	
	public static Block getIceSourceBlock(Player player, double range) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "IceBlast", location))
				continue;
			if (isIcebendable(block)) {
				if (TempBlock.isTempBlock(block))
					continue;
				return block;
			}
		}
		return null;
	}
	
	public static Block getPlantSourceBlock(Player player, double range, boolean onlyLeaves) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();
		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "PlantDisc", location))
				continue;
			if (isPlantbendable(block, onlyLeaves)) {
				if (TempBlock.isTempBlock(block))
					continue;
				return block;
			}
		}
		return null;
	}
	
	public static boolean isAdjacentToFrozenBlock(Block block) {
		BlockFace[] faces = { BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH,
				BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH };
		boolean adjacent = false;
		for (BlockFace face : faces) {
			if (FreezeMelt.frozenblocks.containsKey((block.getRelative(face))))
				adjacent = true;
		}

		return adjacent;
	}
	
	public static boolean isHealingAbility(String ability){
		return AbilityModuleManager.healingabilities.contains(ability);
	}
	
	public static boolean isIcebendingAbility(String ability){
		return AbilityModuleManager.iceabilities.contains(ability);
	}
	
	public static boolean isPlantbendingAbility(String ability){
		return AbilityModuleManager.plantabilities.contains(ability);
	}
	
	public static boolean isBloodbendingAbility(String ability){
		return AbilityModuleManager.bloodabilities.contains(ability);
	}

	public static boolean isFullMoon(World world) {
		long days = world.getFullTime() / 24000;
		long phase = days%8;
        return phase == 0;
    }

    public static boolean isMeltable(Block block) {
        return block.getType() == Material.ICE || block.getType() == Material.SNOW;
    }

	public static boolean isNight(World world) {
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		long time = world.getTime();
        return time >= 12950 && time <= 23050;
    }

    @SuppressWarnings("deprecation")
	public static boolean isPlant(Block block) {
        return block != null && Arrays.asList(plantIds).contains(block.getTypeId());
    }

    public static boolean isWater(Block block) {
        return block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER;
    }

	public static boolean isWaterAbility(String ability) {
		return AbilityModuleManager.waterbendingabilities.contains(ability);
	}

	
	@SuppressWarnings("deprecation")
	public static boolean isWaterbendable(Block block, Player player) {
        byte full = 0x0;
        return !TempBlock.isTempBlock(block)
                && ((block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
                && block.getData() == full || block.getType() == Material.ICE || block.getType() == Material.SNOW || block.getType() == Material.PACKED_ICE
                && plugin.getConfig().getBoolean("Properties.Water.CanBendPackedIce") || canPlantbend(player)
                && isPlant(block));
    }

	public static boolean isIcebendable(Block block) {
        return block.getType() == Material.ICE || block.getType() == Material.PACKED_ICE
                && plugin.getConfig().getBoolean("Properties.Water.CanBendPackedIce");
    }

    public static boolean isPlantbendable(Block block, boolean leavesOnly) {
        return block.getType() == Material.LEAVES
                || block.getType() == Material.LEAVES_2
                || isPlant(block) && !leavesOnly;
    }

    public static void playFocusWaterEffect(Block block) {
		block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 4, 20);
	}
	
	/**
	 * Removes all water spouts in a location within a certain radius.
	 * @param loc The location to use
	 * @param radius The radius around the location to remove spouts in
	 * @param source The player causing the removal
	 */
	public static void removeWaterSpouts(Location loc, double radius, Player source) {
		WaterSpout.removeSpouts(loc, radius, source);
	}
	
	/**
	 * Removes all water spouts in a location with a radius of 1.5.
	 * @param loc The location to use
	 * @param source The player causing the removal
	 */
	public static void removeWaterSpouts(Location loc, Player source) {
		removeWaterSpouts(loc, 1.5, source);
	}
	
	public static double waterbendingNightAugment(double value, World world) {
		if (isNight(world)) {
			if (GeneralMethods.hasRPG()) {
				if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.LunarEclipse.toString())) {
					return RPGMethods.getFactor(WorldEvents.LunarEclipse) * value;
				}
				else if (BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) {
					return plugin.getConfig().getDouble("Properties.Water.FullMoonFactor") * value;
				}
				else {
					return value;
				}
			} else {
				if (isFullMoon(world)) {
					return plugin.getConfig().getDouble("Properties.Water.FullMoonFactor") * value;
				} else {
					return plugin.getConfig().getDouble("Properties.Water.NightFactor") * value;
				}
			}
		} else {
			return value;
		}
	}
	
	public static boolean isNegativeEffect(PotionEffectType effect) {
        return effect.equals(PotionEffectType.POISON)
                || effect.equals(PotionEffectType.BLINDNESS)
                || effect.equals(PotionEffectType.CONFUSION)
                || effect.equals(PotionEffectType.HARM)
                || effect.equals(PotionEffectType.HUNGER)
                || effect.equals(PotionEffectType.SLOW)
                || effect.equals(PotionEffectType.SLOW_DIGGING)
                || effect.equals(PotionEffectType.WEAKNESS)
                || effect.equals(PotionEffectType.WITHER);
    }

	public static boolean isPositiveEffect(PotionEffectType effect) {
        return effect.equals(PotionEffectType.ABSORPTION)
                || effect.equals(PotionEffectType.DAMAGE_RESISTANCE)
                || effect.equals(PotionEffectType.FAST_DIGGING)
                || effect.equals(PotionEffectType.FIRE_RESISTANCE)
                || effect.equals(PotionEffectType.HEAL)
                || effect.equals(PotionEffectType.HEALTH_BOOST)
                || effect.equals(PotionEffectType.INCREASE_DAMAGE)
                || effect.equals(PotionEffectType.JUMP)
                || effect.equals(PotionEffectType.NIGHT_VISION)
                || effect.equals(PotionEffectType.REGENERATION)
                || effect.equals(PotionEffectType.SATURATION)
                || effect.equals(PotionEffectType.SPEED)
                || effect.equals(PotionEffectType.WATER_BREATHING);
    }

	public static boolean isNeutralEffect(PotionEffectType effect) {
        return effect.equals(PotionEffectType.INVISIBILITY);
    }

    public static void playWaterbendingSound(Location loc) {
		if (plugin.getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.WATER, 1, 10);
		}
	}

	public static void playIcebendingSound(Location loc) {
		if (plugin.getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.FIRE_IGNITE, 10, 4);
		}
	}
	
	public static void playPlantbendingSound(Location loc) {
		if (plugin.getConfig().getBoolean("Properties.Water.PlaySound")) {
			loc.getWorld().playSound(loc, Sound.STEP_GRASS, 1, 10);
		}
	}
	
	public static void stopBending() {
		FreezeMelt.removeAll();
		IceSpike.removeAll();
		IceSpike2.removeAll();
		WaterManipulation.removeAll();
		WaterSpout.removeAll();
		WaterWall.removeAll();
		Wave.removeAll();
		Plantbending.regrowAll();
		OctopusForm.removeAll();
		Bloodbending.instances.clear();
		WaterWave.removeAll();
		WaterCombo.removeAll();
		WaterReturn.removeAll();
		WaterArms.removeAll();
		PlantArmor.removeAll();
	}
}