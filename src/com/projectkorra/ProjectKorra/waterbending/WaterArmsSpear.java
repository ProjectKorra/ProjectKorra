package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.BendingManager;
import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Utilities.TempBlock;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterArms.Arm;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class WaterArmsSpear {

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static ConcurrentHashMap<Integer, WaterArmsSpear> instances = new ConcurrentHashMap<Integer, WaterArmsSpear>();

	private Player player;
	private WaterArms waterArms;

	private List<Location> spearLocations = new ArrayList<>();

	private int spearRange = config.getInt("Abilities.Water.WaterArms.Spear.Range");
	private double spearDamage = config.getDouble("Abilities.Water.WaterArms.Spear.Damage");
	private boolean spearDamageEnabled = config.getBoolean("Abilities.Water.WaterArms.Spear.DamageEnabled");
	private int spearSphere = config.getInt("Abilities.Water.WaterArms.Spear.Sphere");
	private long spearDuration = config.getLong("Abilities.Water.WaterArms.Spear.Duration");
	private int spearLength = config.getInt("Abilities.Water.WaterArms.Spear.Length");

	private int spearRangeNight = config.getInt("Abilities.Water.WaterArms.Spear.NightAugments.Range.Normal");
	private int spearRangeFullMoon = config.getInt("Abilities.Water.WaterArms.Spear.NightAugments.Range.FullMoon");
	private int spearSphereNight = config.getInt("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.Normal");
	private int spearSphereFullMoon = config.getInt("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.FullMoon");
	private long spearDurationNight = config.getLong("Abilities.Water.WaterArms.Spear.NightAugments.Duration.Normal");
	private long spearDurationFullMoon = config.getLong("Abilities.Water.WaterArms.Spear.NightAugments.Duration.FullMoon");

	private boolean usageCooldownEnabled = config.getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled");
	private long usageCooldown = config.getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown");

	private Location location;
	private Location initLocation;
	private int distanceTravelled;
	private Arm arm;
	private int layer;
	private boolean hitEntity;
	private boolean canFreeze;

	private int id;
	private static int ID = Integer.MIN_VALUE;

	Random rand = new Random();

	public WaterArmsSpear(Player player, boolean freeze) {
		this.player = player;
		this.canFreeze = freeze;
		getNightAugments();
		createInstance();
	}

	private void getNightAugments() {
		World world = player.getWorld();
		if (WaterMethods.isNight(world)) {
			if (GeneralMethods.hasRPG()) {
				if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.LunarEclipse.toString())) {
					spearRange = spearRangeFullMoon;
					spearSphere = spearSphereFullMoon;
					spearDuration = spearDurationFullMoon;
				} else if (BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) {
					spearRange = spearRangeFullMoon;
					spearSphere = spearSphereFullMoon;
					spearDuration = spearDurationFullMoon;
				} else {
					spearRange = spearRangeNight;
					spearSphere = spearSphereNight;
					spearDuration = spearDurationNight;
				}
			} else {
				if (WaterMethods.isFullMoon(world)) {
					spearRange = spearRangeFullMoon;
					spearSphere = spearSphereFullMoon;
					spearDuration = spearDurationFullMoon;
				} else {
					spearRange = spearRangeNight;
					spearSphere = spearSphereNight;
					spearDuration = spearDurationNight;
				}
			}
		}
	}

	private void createInstance() {
		if (WaterArms.instances.containsKey(player)) {
			waterArms = WaterArms.instances.get(player);
			waterArms.switchPreferredArm();
			arm = waterArms.getActiveArm();
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
			if (arm.equals(Arm.Left)) {
				if (waterArms.isLeftArmCooldown() || bPlayer.isOnCooldown("WaterArms_LEFT") || !waterArms.displayLeftArm()) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_LEFT", usageCooldown);
					}
					waterArms.setLeftArmConsumed(true);
					waterArms.setLeftArmCooldown(true);
				}
			}
			if (arm.equals(Arm.Right)) {
				if (waterArms.isRightArmCooldown() || bPlayer.isOnCooldown("WaterArms_RIGHT") || !waterArms.displayRightArm()) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_RIGHT", usageCooldown);
					}
					waterArms.setRightArmConsumed(true);
					waterArms.setRightArmCooldown(true);
				}
			}
			Vector dir = player.getLocation().getDirection();
			location = waterArms.getActiveArmEnd().add(dir.normalize().multiply(1));
			initLocation = location.clone();
		} else {
			return;
		}
		id = ID;
		instances.put(id, this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (distanceTravelled > spearRange) {
			remove();
			return;
		}
		if (!hitEntity) {
			progressSpear();
		} else {
			createIceBall();
		}
		if (layer >= spearSphere) {
			remove();
			return;
		}
		if (!canPlaceBlock(location.getBlock())) {
			if (canFreeze) {
				createSpear();
			}
			remove();
			return;
		}
	}

	private void progressSpear() {
		for (int i = 0; i < 2; i++) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					hitEntity = true;
					location = entity.getLocation();

					if (spearDamageEnabled) {
						GeneralMethods.damageEntity(player, entity, spearDamage);
					}

					return;
				}
			}
			new TempBlock(location.getBlock(), Material.STATIONARY_WATER, (byte) 8);
			WaterArms.revert.put(location.getBlock(), System.currentTimeMillis() + 600L);
			Vector direction = GeneralMethods.getDirection(initLocation, GeneralMethods.getTargetedLocation(player, spearRange, new Integer[] { 8, 9, 79, 174 })).normalize();
			location = location.add(direction.clone().multiply(1));
			spearLocations.add(location.clone());
			if (!canPlaceBlock(location.getBlock())) {
				return;
			}
			distanceTravelled++;
		}
	}

	private void createSpear() {
		for (int i = spearLocations.size() - spearLength; i < spearLocations.size(); i++) {
			if (i >= 0) {
				Block block = spearLocations.get(i).getBlock();
				if (canPlaceBlock(block)) {
					WaterMethods.playIcebendingSound(block.getLocation());
					if (WaterArms.revert.containsKey(block)) {
						WaterArms.revert.remove(block);
					}
					new TempBlock(block, Material.ICE, (byte) 0);
					WaterArms.revert.put(block, System.currentTimeMillis() + spearDuration + (long) (Math.random() * 500));
				}
			}
		}
	}

	private void createIceBall() {
		layer++;
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, layer)) {
			if (EarthMethods.isTransparentToEarthbending(player, block) && block.getType() != Material.ICE && !WaterArms.isUnbreakable(block)) {
				WaterMethods.playIcebendingSound(block.getLocation());
				new TempBlock(block, Material.ICE, (byte) 0);
				WaterArms.revert.put(block, System.currentTimeMillis() + spearDuration + (long) (Math.random() * 500));
			}
		}
	}

	private boolean canPlaceBlock(Block block) {
		if (!EarthMethods.isTransparentToEarthbending(player, block) && !((WaterMethods.isWater(block) || WaterMethods.isIcebendable(block)) && (TempBlock.isTempBlock(block) && !WaterArms.revert.containsKey(block)))) {
			return false;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterArms", block.getLocation())) {
			return false;
		}
		if (WaterArms.isUnbreakable(block) && !WaterMethods.isWater(block)) {
			return false;
		}
		return true;
	}

	private void remove() {
		if (WaterArms.instances.containsKey(player)) {
			if (arm.equals(Arm.Left)) {
				waterArms.setLeftArmCooldown(false);
			} else {
				waterArms.setRightArmCooldown(false);
			}
			waterArms.setMaxUses(waterArms.getMaxUses() - 1);
		}
		instances.remove(id);
	}

	public static void progressAll() {
		for (int ID : instances.keySet())
			instances.get(ID).progress();
	}

	public static void removeAll() {
		instances.clear();
	}

	public Player getPlayer() {
		return player;
	}

	public boolean getCanFreeze() {
		return canFreeze;
	}

	public void setCanFreeze(boolean freeze) {
		this.canFreeze = freeze;
	}

	public boolean getHasHitEntity() {
		return hitEntity;
	}

	public void setHitEntity(boolean hit) {
		this.hitEntity = hit;
	}
}
