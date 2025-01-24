package com.projectkorra.projectkorra.waterbending.multiabilities;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import com.projectkorra.projectkorra.util.ActionBar;
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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms.Arm;

public class WaterArmsFreeze extends IceAbility {

	private boolean cancelled;
	private boolean usageCooldownEnabled;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double iceRange;
	private int distanceTravelled;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double iceDamage;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long usageCooldown;
	private Arm arm;
	private Location location;
	private Vector direction;
	private WaterArms waterArms;

	public WaterArmsFreeze(final Player player) {
		super(player);

		this.usageCooldownEnabled = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown.Enabled");
		this.iceRange = getConfig().getDouble("Abilities.Water.WaterArms.Freeze.Range");
		this.iceDamage = getConfig().getInt("Abilities.Water.WaterArms.Freeze.Damage");
		this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown.Freeze");
		this.direction = player.getEyeLocation().getDirection();

		this.createInstance();
	}

	private void createInstance() {
		this.waterArms = getAbility(this.player, WaterArms.class);

		if (this.waterArms != null) {
			this.waterArms.switchPreferredArm();
			this.arm = this.waterArms.getActiveArm();

			if (this.arm.equals(Arm.LEFT)) {
				if (this.waterArms.isLeftArmCooldown() || this.bPlayer.isOnCooldown("WaterArms_LEFT")) {
					return;
				} else {
					if (this.usageCooldownEnabled) {
						this.bPlayer.addCooldown("WaterArms_LEFT", this.usageCooldown);
					}
					this.waterArms.setLeftArmCooldown(true);
				}
			}

			if (this.arm.equals(Arm.RIGHT)) {
				if (this.waterArms.isRightArmCooldown() || this.bPlayer.isOnCooldown("WaterArms_RIGHT")) {
					return;
				} else {
					if (this.usageCooldownEnabled) {
						this.bPlayer.addCooldown("WaterArms_RIGHT", this.usageCooldown);
					}
					this.waterArms.setRightArmCooldown(true);
				}
			}

			final Vector dir = this.player.getLocation().getDirection();
			this.location = this.waterArms.getActiveArmEnd().add(dir.normalize().multiply(1));
			this.direction = GeneralMethods.getDirection(this.location, GeneralMethods.getTargetedLocation(this.player, this.iceRange, Material.WATER, Material.ICE, Material.PACKED_ICE)).normalize();
		} else {
			return;
		}
		this.start();
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		} else if (this.distanceTravelled > this.iceRange) {
			this.remove();
			return;
		}

		if (this.distanceTravelled >= 5 && !this.cancelled) {
			this.cancelled = true;
			if (hasAbility(this.player, WaterArms.class)) {
				if (this.arm.equals(Arm.LEFT)) {
					this.waterArms.setLeftArmCooldown(false);
				} else {
					this.waterArms.setRightArmCooldown(false);
				}
				this.waterArms.setMaxIceBlasts(this.waterArms.getMaxIceBlasts() - 1);
				ActionBar.sendActionBar(Element.WATER.getSubColor() + "Ice Blasts Left: " + this.waterArms.getMaxIceBlasts(), this.player);
			}
		}

		if (!this.canPlaceBlock(this.location.getBlock())) {
			this.remove();
			return;
		}
		this.progressIce();
	}

	private boolean canPlaceBlock(final Block block) {
		if (!isTransparent(this.player, block) && !((isWater(block)) && TempBlock.isTempBlock(block))) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return false;
		}
		return true;
	}

	private void progressIce() {
		ParticleEffect.SNOW_SHOVEL.display(this.location, 5, Math.random(), Math.random(), Math.random(), 0.05);
		new TempBlock(this.location.getBlock(), Material.ICE.createBlockData(), this).setCanSuffocate(false).setRevertTime(10);

		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, 2.5)) {
			if (entity instanceof LivingEntity && entity.getEntityId() != this.player.getEntityId() && !(entity instanceof ArmorStand)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
					continue;
				}
				DamageHandler.damageEntity(entity, this.iceDamage, this);
				final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 40, 2);
				new TempPotionEffect((LivingEntity) entity, effect);
				this.remove();
				return;
			}
		}

		for (int i = 0; i < 2; i++) {
			this.location = this.location.add(this.direction.clone().multiply(1));
			if (!this.canPlaceBlock(this.location.getBlock())) {
				return;
			}
			this.distanceTravelled++;
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (hasAbility(this.player, WaterArms.class)) {
			if (!this.cancelled) {
				if (this.arm.equals(Arm.LEFT)) {
					this.waterArms.setLeftArmCooldown(false);
				} else {
					this.waterArms.setRightArmCooldown(false);
				}
				this.waterArms.setMaxIceBlasts(this.waterArms.getMaxIceBlasts() - 1);
				ActionBar.sendActionBar(Element.WATER.getSubColor() + "Ice Blasts Left: " + this.waterArms.getMaxIceBlasts(), this.player);
			}
		}
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public String getName() {
		return "WaterArmsFreeze";
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public long getCooldown() {
		return this.usageCooldown;
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
		return this.cancelled;
	}

	public void setCancelled(final boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isUsageCooldownEnabled() {
		return this.usageCooldownEnabled;
	}

	public void setUsageCooldownEnabled(final boolean usageCooldownEnabled) {
		this.usageCooldownEnabled = usageCooldownEnabled;
	}

	public double getIceRange() {
		return this.iceRange;
	}

	public void setIceRange(final int iceRange) {
		this.iceRange = iceRange;
	}

	public int getDistanceTravelled() {
		return this.distanceTravelled;
	}

	public void setDistanceTravelled(final int distanceTravelled) {
		this.distanceTravelled = distanceTravelled;
	}

	public double getIceDamage() {
		return this.iceDamage;
	}

	public void setIceDamage(final double iceDamage) {
		this.iceDamage = iceDamage;
	}

	public long getUsageCooldown() {
		return this.usageCooldown;
	}

	public void setUsageCooldown(final long usageCooldown) {
		this.usageCooldown = usageCooldown;
	}

	public Arm getArm() {
		return this.arm;
	}

	public void setArm(final Arm arm) {
		this.arm = arm;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public WaterArms getWaterArms() {
		return this.waterArms;
	}

	public void setWaterArms(final WaterArms waterArms) {
		this.waterArms = waterArms;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

}
