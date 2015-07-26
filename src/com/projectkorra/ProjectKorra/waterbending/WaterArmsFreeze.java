package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.TempPotionEffect;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterArms.Arm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

public class WaterArmsFreeze {

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

    public static ConcurrentHashMap<Integer, WaterArmsFreeze> instances = new ConcurrentHashMap<>();

	private Player player;
	private WaterArms waterArms;

	private int iceRange = config
			.getInt("Abilities.Water.WaterArms.Freeze.Range");
	private double iceDamage = config
			.getInt("Abilities.Water.WaterArms.Freeze.Damage");

	private boolean usageCooldownEnabled = config
			.getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled");
	private long usageCooldown = config
			.getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown");

	private Location location;
	private Vector direction;
	private int distanceTravelled;
	private Arm arm;
	private boolean cancelled;

	private int id;
	private static int ID = Integer.MIN_VALUE;

	public WaterArmsFreeze(Player player) {
		this.player = player;
		direction = player.getEyeLocation().getDirection();
		createInstance();
	}

	private void createInstance() {
		if (WaterArms.instances.containsKey(player)) {
			waterArms = WaterArms.instances.get(player);
			waterArms.switchPreferredArm();
			arm = waterArms.getActiveArm();
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player
					.getName());
			if (arm.equals(Arm.Left)) {
				if (waterArms.isLeftArmCooldown()
						|| bPlayer.isOnCooldown("WaterArms_LEFT")) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_LEFT", usageCooldown);
					}
					waterArms.setLeftArmCooldown(true);
				}
			}
			if (arm.equals(Arm.Right)) {
				if (waterArms.isRightArmCooldown()
						|| bPlayer.isOnCooldown("WaterArms_RIGHT")) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_RIGHT", usageCooldown);
					}
					waterArms.setRightArmCooldown(true);
				}
			}
			Vector dir = player.getLocation().getDirection();
			location = waterArms.getActiveArmEnd().add(
					dir.normalize().multiply(1));
			direction = GeneralMethods.getDirection(
					location,
                    GeneralMethods.getTargetedLocation(player, iceRange, 8, 9, 79, 174)).normalize();
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
		if (distanceTravelled > iceRange) {
			remove();
			return;
		}
		if (distanceTravelled >= 5 && !cancelled) {
			cancelled = true;
			if (WaterArms.instances.containsKey(player)) {
				if (arm.equals(Arm.Left)) {
					waterArms.setLeftArmCooldown(false);
				} else {
					waterArms.setRightArmCooldown(false);
				}
				waterArms.setMaxIceBlasts(waterArms.getMaxIceBlasts() - 1);
			}
		}
		if (!canPlaceBlock(location.getBlock())) {
			remove();
			return;
		}
		progressIce();
	}

	private boolean canPlaceBlock(Block block) {
        return !(!EarthMethods.isTransparentToEarthbending(player, block)
                && !((WaterMethods.isWater(block)
                || WaterMethods.isIcebendable(block))
                && TempBlock.isTempBlock(block)))
                && !GeneralMethods.isRegionProtectedFromBuild(player, "WaterArms", block.getLocation());
    }

	private void progressIce() {
		ParticleEffect.SNOW_SHOVEL.display(location, (float) Math.random(),
				(float) Math.random(), (float) Math.random(), (float) 0.05, 5);
		new TempBlock(location.getBlock(), Material.ICE, (byte) 0);
		WaterArms.revert.put(location.getBlock(),
				System.currentTimeMillis() + 10L);

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location,
				2.5)) {
			if (entity instanceof LivingEntity
					&& entity.getEntityId() != player.getEntityId()
					&& !(entity instanceof ArmorStand)) {
				GeneralMethods.damageEntity(player, entity, iceDamage);
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW,
						40, 2);
				new TempPotionEffect((LivingEntity) entity, effect);
				remove();
				return;
			}
		}

		for (int i = 0; i < 2; i++) {
			location = location.add(direction.clone().multiply(1));
			if (!canPlaceBlock(location.getBlock()))
				return;
			distanceTravelled++;
		}
	}

	private void remove() {
		if (WaterArms.instances.containsKey(player)) {
			if (!cancelled) {
				if (arm.equals(Arm.Left)) {
					waterArms.setLeftArmCooldown(false);
				} else {
					waterArms.setRightArmCooldown(false);
				}
				waterArms.setMaxIceBlasts(waterArms.getMaxIceBlasts() - 1);
			}
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

	public boolean getCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}