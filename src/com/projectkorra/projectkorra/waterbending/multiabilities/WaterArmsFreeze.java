package com.projectkorra.projectkorra.waterbending.multiabilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms.Arm;

public class WaterArmsFreeze extends IceAbility {

	private boolean cancelled;
	private boolean usageCooldownEnabled;
	private int iceRange;
	private int distanceTravelled;
	private double iceDamage;
	private long usageCooldown;
	private Arm arm;
	private Location location;
	private Vector direction;
	private WaterArms waterArms;

	public WaterArmsFreeze(Player player) {
		super(player);

		this.usageCooldownEnabled = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled");
		this.iceRange = getConfig().getInt("Abilities.Water.WaterArms.Freeze.Range");
		this.iceDamage = getConfig().getInt("Abilities.Water.WaterArms.Freeze.Damage");
		this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown");
		this.direction = player.getEyeLocation().getDirection();

		createInstance();
	}

	private void createInstance() {
		waterArms = getAbility(player, WaterArms.class);

		if (waterArms != null) {
			waterArms.switchPreferredArm();
			arm = waterArms.getActiveArm();

			if (arm.equals(Arm.LEFT)) {
				if (waterArms.isLeftArmCooldown() || bPlayer.isOnCooldown("WaterArms_LEFT")) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_LEFT", usageCooldown);
					}
					waterArms.setLeftArmCooldown(true);
				}
			}

			if (arm.equals(Arm.RIGHT)) {
				if (waterArms.isRightArmCooldown() || bPlayer.isOnCooldown("WaterArms_RIGHT")) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_RIGHT", usageCooldown);
					}
					waterArms.setRightArmCooldown(true);
				}
			}

			Vector dir = player.getLocation().getDirection();
			location = waterArms.getActiveArmEnd().add(dir.normalize().multiply(1));
			direction = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(player, iceRange, new Integer[] { 8, 9, 79, 174 })).normalize();
		} else {
			return;
		}
		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (distanceTravelled > iceRange) {
			remove();
			return;
		}

		if (distanceTravelled >= 5 && !cancelled) {
			cancelled = true;
			if (hasAbility(player, WaterArms.class)) {
				if (arm.equals(Arm.LEFT)) {
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
		if (!isTransparent(player, block) && !((isWater(block)) && TempBlock.isTempBlock(block))) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return false;
		}
		return true;
	}

	private void progressIce() {
		ParticleEffect.SNOW_SHOVEL.display(location, (float) Math.random(), (float) Math.random(), (float) Math.random(), (float) 0.05, 5);
		new TempBlock(location.getBlock(), Material.ICE, (byte) 0);
		WaterArms.getBlockRevertTimes().put(location.getBlock(), System.currentTimeMillis() + 10L);

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.5)) {
			if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
				DamageHandler.damageEntity(entity, iceDamage, this);
				PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 40, 2);
				new TempPotionEffect((LivingEntity) entity, effect);
				remove();
				return;
			}
		}

		for (int i = 0; i < 2; i++) {
			location = location.add(direction.clone().multiply(1));
			if (!canPlaceBlock(location.getBlock())) {
				return;
			}
			distanceTravelled++;
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (hasAbility(player, WaterArms.class)) {
			if (!cancelled) {
				if (arm.equals(Arm.LEFT)) {
					waterArms.setLeftArmCooldown(false);
				} else {
					waterArms.setRightArmCooldown(false);
				}
				waterArms.setMaxIceBlasts(waterArms.getMaxIceBlasts() - 1);
			}
		}
	}

	@Override
	public String getName() {
		return "WaterArms";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return usageCooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isUsageCooldownEnabled() {
		return usageCooldownEnabled;
	}

	public void setUsageCooldownEnabled(boolean usageCooldownEnabled) {
		this.usageCooldownEnabled = usageCooldownEnabled;
	}

	public int getIceRange() {
		return iceRange;
	}

	public void setIceRange(int iceRange) {
		this.iceRange = iceRange;
	}

	public int getDistanceTravelled() {
		return distanceTravelled;
	}

	public void setDistanceTravelled(int distanceTravelled) {
		this.distanceTravelled = distanceTravelled;
	}

	public double getIceDamage() {
		return iceDamage;
	}

	public void setIceDamage(double iceDamage) {
		this.iceDamage = iceDamage;
	}

	public long getUsageCooldown() {
		return usageCooldown;
	}

	public void setUsageCooldown(long usageCooldown) {
		this.usageCooldown = usageCooldown;
	}

	public Arm getArm() {
		return arm;
	}

	public void setArm(Arm arm) {
		this.arm = arm;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public WaterArms getWaterArms() {
		return waterArms;
	}

	public void setWaterArms(WaterArms waterArms) {
		this.waterArms = waterArms;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}
