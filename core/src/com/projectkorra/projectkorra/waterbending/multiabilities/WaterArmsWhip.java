package com.projectkorra.projectkorra.waterbending.multiabilities;

import java.util.HashMap;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.util.ActionBar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.multiabilities.WaterArms.Arm;

public class WaterArmsWhip extends WaterAbility {

	/**
	 * Whip Enum value for deciding what ability should be executed.
	 */
	public static enum Whip {
		PULL, PUNCH, GRAPPLE, GRAB;
	}

	private static final HashMap<LivingEntity, WaterArmsWhip> GRABBED_ENTITIES = new HashMap<LivingEntity, WaterArmsWhip>();

	private boolean reverting;
	private boolean hasDamaged;
	private boolean grappled;
	private boolean grabbed;
	private boolean grappleRespectRegions;
	private boolean usageCooldownEnabled;
	@Attribute("WhipLength")
	private int whipLength;
	private int whipLengthWeak;
	private int whipLengthNight;
	private int whipLengthFullMoon;
	@Attribute("InitialLength")
	private int initLength;
	@Attribute("PunchLength")
	private int punchLength;
	private int punchLengthNight;
	private int punchLengthFullMoon;
	private int activeLength;
	@Attribute("WhipSpeed")
	private int whipSpeed;
	@Attribute("GrabDuration")
	private long grabDuration;
	@Attribute(Attribute.COOLDOWN)
	private long usageCooldown;
	private long time;
	private double pullMultiplier;
	@Attribute("PunchDamage")
	private double punchDamage;
	private double playerHealth;
	private Arm arm;
	private Whip ability;
	private LivingEntity grabbedEntity;
	private Location end;
	private WaterArms waterArms;

	public WaterArmsWhip(final Player player, final Whip ability) {
		super(player);

		this.ability = ability;
		this.reverting = false;
		this.hasDamaged = false;
		this.grappled = false;
		this.grabbed = false;
		this.grappleRespectRegions = getConfig().getBoolean("Abilities.Water.WaterArms.Whip.Grapple.RespectRegions");
		this.usageCooldownEnabled = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown.Enabled");
		this.whipLength = getConfig().getInt("Abilities.Water.WaterArms.Whip.MaxLength");
		this.whipLengthWeak = getConfig().getInt("Abilities.Water.WaterArms.Whip.MaxLengthWeak");
		this.whipLengthNight = getConfig().getInt("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.Normal");
		this.whipLengthFullMoon = getConfig().getInt("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.FullMoon");
		this.initLength = getConfig().getInt("Abilities.Water.WaterArms.Arms.InitialLength");
		this.punchLength = getConfig().getInt("Abilities.Water.WaterArms.Whip.Punch.MaxLength");
		this.punchLengthNight = getConfig().getInt("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.Normal");
		this.punchLengthFullMoon = getConfig().getInt("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.FullMoon");
		this.activeLength = this.initLength;
		this.whipSpeed = 1;
		this.grabDuration = getConfig().getLong("Abilities.Water.WaterArms.Whip.Grab.Duration");
		this.pullMultiplier = getConfig().getDouble("Abilities.Water.WaterArms.Whip.Pull.Multiplier");
		this.punchDamage = getConfig().getDouble("Abilities.Water.WaterArms.Whip.Punch.Damage");

		switch (ability) {
			case PULL:
				this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown.Pull");
				break;
			case PUNCH:
				this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown.Punch");
				break;
			case GRAPPLE:
				this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown.Grapple");
				break;
			case GRAB:
				this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown.Grab");
				break;
			default:
				this.usageCooldown = 200;

		}
		final WaterArmsWhip waw = getAbility(player, WaterArmsWhip.class);
		if (waw != null) {
			if (waw.grabbed) {
				waw.grabbed = false;
				if (waw.grabbedEntity != null) {
					GRABBED_ENTITIES.remove(waw.grabbedEntity);
					GeneralMethods.setVelocity(this, waw.grabbedEntity, waw.grabbedEntity.getVelocity().multiply(2.5));
				}
				return;
			}
			if (!waw.arm.equals(getAbility(player, WaterArms.class).getActiveArm())) {
				return;
			}
		}

		if (this.ability.equals(Whip.PUNCH)) {
			this.whipLength = this.punchLength;
		}
		this.createInstance();
	}

	private void createInstance() {
		this.waterArms = getAbility(this.player, WaterArms.class);
		if (this.waterArms != null) {
			this.waterArms.switchPreferredArm();
			this.arm = this.waterArms.getActiveArm();
			this.time = System.currentTimeMillis() + this.grabDuration;
			this.playerHealth = this.player.getHealth();

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
		} else {
			return;
		}

		if (!this.waterArms.isFullSource()) {
			this.whipLength = this.whipLengthWeak;
		}
		this.start();
	}

	@Override
	public void progress() {
		if (!hasAbility(this.player, WaterArms.class)) {
			this.remove();
			return;
		} else if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		}
		if (!MultiAbilityManager.hasMultiAbilityBound(this.player, "WaterArms")) {
			this.remove();
			return;
		}

		if (this.activeLength < this.whipLength && !this.reverting) {
			this.activeLength += this.whipSpeed;
		} else if (this.activeLength > this.initLength) {
			if (!this.grabbed) {
				this.activeLength -= this.whipSpeed;
			}
		} else {
			this.remove();
			return;
		}

		if (this.activeLength >= this.whipLength && !this.grabbed) {
			this.reverting = true;
		}

		if (this.grabbed && (System.currentTimeMillis() > this.time || this.playerHealth > this.player.getHealth())) {
			this.grabbed = false;
			this.reverting = true;
		}

		this.useArm();
		if (this.end != null) { //not 100% sure if this null check is a root cause fix or not
			this.dragEntity(this.end);
		}
	}

	private boolean canPlaceBlock(final Block block) {
		if (!isTransparent(this.player, block) && !(isWater(block) && TempBlock.isTempBlock(block))) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return false;
		}
		return true;
	}

	private void useArm() {
		if (this.waterArms.canDisplayActiveArm()) {
			Location l1 = null;

			if (this.arm.equals(Arm.LEFT)) {
				l1 = this.waterArms.getLeftArmEnd().clone();
			} else {
				l1 = this.waterArms.getRightArmEnd().clone();
			}

			final Vector dir = this.player.getLocation().getDirection().clone();
			for (int i = 1; i <= this.activeLength; i++) {
				final Location l2 = l1.clone().add(dir.normalize().multiply(i));

				if (!this.canPlaceBlock(l2.getBlock())) {
					if (l2.getBlock().getType() != Material.BARRIER) {
						this.grappled = true;
					}
					this.reverting = true;
					this.performAction(l2);
					break;
				}

				final int j = (int) Math.ceil(8 / (Math.pow(i, 1 / 3)));
				this.waterArms.addToArm(l2.getBlock(), this.arm);
				this.waterArms.addBlock(l2.getBlock(), GeneralMethods.getWaterData(j), 40);

				if (i == this.activeLength) {
					this.end = l2.clone();
					if (this.arm == Arm.LEFT) {
						this.end = GeneralMethods.getRightSide(this.end, 1);
					} else {
						this.end = GeneralMethods.getLeftSide(this.end, 1);
					}

					if (!this.canPlaceBlock(this.end.getBlock())) {
						if (this.end.getBlock().getType() != Material.BARRIER) {
							this.grappled = true;
						}
						this.reverting = true;
						this.performAction(this.end);
						break;
					}

					this.waterArms.addToArm(this.end.getBlock(), this.arm);
					this.waterArms.addBlock(this.end.getBlock(), GeneralMethods.getWaterData(5), 40);
					this.performAction(this.end);
				} else {
					this.performAction(l2);
				}
			}
		}
	}

	private void performAction(final Location location) {
		final Location endOfArm = this.waterArms.getActiveArmEnd().clone();
		switch (this.ability) {
			case PULL:
				for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
						continue;
					}
					final Vector vector = endOfArm.toVector().subtract(entity.getLocation().toVector());
					GeneralMethods.setVelocity(this, entity, vector.multiply(this.pullMultiplier));
				}
				break;
			case PUNCH:
				for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
						continue;
					}

					final Vector vector = entity.getLocation().toVector().subtract(endOfArm.toVector());
					GeneralMethods.setVelocity(this, entity, vector.multiply(0.15));
					if (entity instanceof LivingEntity) {
						if (entity.getEntityId() != this.player.getEntityId()) {
							this.hasDamaged = true;
							DamageHandler.damageEntity(entity, this.punchDamage, this);
						}
					}
				}
				break;
			case GRAPPLE:
				this.grapplePlayer(location);
				break;
			case GRAB:
				if (this.grabbedEntity == null) {
					for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
						if (entity instanceof LivingEntity && entity.getEntityId() != this.player.getEntityId() && !GRABBED_ENTITIES.containsKey(entity)) {
							if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
								continue;
							}
							GRABBED_ENTITIES.put((LivingEntity) entity, this);
							this.grabbedEntity = (LivingEntity) entity;
							this.grabbed = true;
							this.reverting = true;
							this.waterArms.setActiveArmCooldown(true);
							break;
						}
					}
				}
				break;
			default:
				break;
		}
	}

	private void dragEntity(final Location location) {
		if (this.grabbedEntity != null && this.grabbed) {
			if (!this.waterArms.canDisplayActiveArm() || this.grabbedEntity.isDead()) {
				this.grabbed = false;
				GRABBED_ENTITIES.remove(this.grabbedEntity);
				return;
			}
			if (this.grabbedEntity instanceof Player && hasAbility((Player) this.grabbedEntity, WaterArmsWhip.class)) {
				final WaterArmsWhip waw = getAbility((Player) this.grabbedEntity, WaterArmsWhip.class);
				if (waw.getAbility().equals(Whip.GRAB)) {
					this.grabbed = false;
					GRABBED_ENTITIES.remove(this.grabbedEntity);
					return;
				}
			}

			final Location newLocation = this.grabbedEntity.getLocation();
			double distance = 0;
			if (location.getWorld().equals(newLocation.getWorld())) {
				distance = location.distance(newLocation);
			}

			double dx, dy, dz;
			dx = location.getX() - newLocation.getX();
			dy = location.getY() - newLocation.getY();
			dz = location.getZ() - newLocation.getZ();
			final Vector vector = new Vector(dx, dy, dz);

			if (distance > 0.5) {
				GeneralMethods.setVelocity(this, this.grabbedEntity, vector.normalize().multiply(.65));
			} else {
				GeneralMethods.setVelocity(this, this.grabbedEntity, new Vector(0, 0, 0));
			}

			this.grabbedEntity.setFallDistance(0);
			if (this.grabbedEntity instanceof Creature) {
				((Creature) this.grabbedEntity).setTarget(null);
			}
		}
	}

	private void grapplePlayer(final Location location) {
		if (this.reverting && this.grappled && this.player != null && this.end != null && this.ability.equals(Whip.GRAPPLE)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, location) && this.grappleRespectRegions) {
				return;
			}

			final Vector vector = this.player.getLocation().toVector().subtract(location.toVector());
			GeneralMethods.setVelocity(this, this.player, vector.multiply(-0.25));
			this.player.setFallDistance(0);
		}
	}

	public static void checkValidEntities() {
		GRABBED_ENTITIES.entrySet().removeIf(entry -> entry.getValue().isRemoved() || entry.getValue().grabbedEntity == null);
	}

	@Override
	public void remove() {
		super.remove();
		if (hasAbility(this.player, WaterArms.class)) {
			if (this.arm.equals(Arm.LEFT)) {
				this.waterArms.setLeftArmCooldown(false);
			} else {
				this.waterArms.setRightArmCooldown(false);
			}
			if (this.hasDamaged) {
				this.waterArms.setMaxPunches(this.waterArms.getMaxPunches() - 1);
				ActionBar.sendActionBar(Element.WATER.getSubColor() + "Punches Left: " + this.waterArms.getMaxPunches(), this.player);
			} else {
				ActionBar.sendActionBar(Element.WATER.getSubColor() + "Uses Left: " + this.waterArms.getMaxUses(), this.player);
			}

			this.waterArms.setMaxUses(this.waterArms.getMaxUses() - 1);
		}
	}

	public static void progressAllCleanup() {
		checkValidEntities();
	}

	public static void removeAllCleanup() {
		GRABBED_ENTITIES.clear();
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public String getName() {
		return "WaterArmsWhip";
	}

	@Override
	public Location getLocation() {
		return this.end;
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

	public boolean isReverting() {
		return this.reverting;
	}

	public void setReverting(final boolean reverting) {
		this.reverting = reverting;
	}

	public boolean isHasDamaged() {
		return this.hasDamaged;
	}

	public void setHasDamaged(final boolean hasDamaged) {
		this.hasDamaged = hasDamaged;
	}

	public boolean isGrappled() {
		return this.grappled;
	}

	public void setGrappled(final boolean grappled) {
		this.grappled = grappled;
	}

	public boolean isGrabbed() {
		return this.grabbed;
	}

	public void setGrabbed(final boolean grabbed) {
		this.grabbed = grabbed;
	}

	public boolean isGrappleRespectRegions() {
		return this.grappleRespectRegions;
	}

	public void setGrappleRespectRegions(final boolean grappleRespectRegions) {
		this.grappleRespectRegions = grappleRespectRegions;
	}

	public boolean isUsageCooldownEnabled() {
		return this.usageCooldownEnabled;
	}

	public void setUsageCooldownEnabled(final boolean usageCooldownEnabled) {
		this.usageCooldownEnabled = usageCooldownEnabled;
	}

	public int getWhipLength() {
		return this.whipLength;
	}

	public void setWhipLength(final int whipLength) {
		this.whipLength = whipLength;
	}

	public int getWhipLengthWeak() {
		return this.whipLengthWeak;
	}

	public void setWhipLengthWeak(final int whipLengthWeak) {
		this.whipLengthWeak = whipLengthWeak;
	}

	public int getWhipLengthNight() {
		return this.whipLengthNight;
	}

	public void setWhipLengthNight(final int whipLengthNight) {
		this.whipLengthNight = whipLengthNight;
	}

	public int getWhipLengthFullMoon() {
		return this.whipLengthFullMoon;
	}

	public void setWhipLengthFullMoon(final int whipLengthFullMoon) {
		this.whipLengthFullMoon = whipLengthFullMoon;
	}

	public int getInitLength() {
		return this.initLength;
	}

	public void setInitLength(final int initLength) {
		this.initLength = initLength;
	}

	public int getPunchLength() {
		return this.punchLength;
	}

	public void setPunchLength(final int punchLength) {
		this.punchLength = punchLength;
	}

	public int getPunchLengthNight() {
		return this.punchLengthNight;
	}

	public void setPunchLengthNight(final int punchLengthNight) {
		this.punchLengthNight = punchLengthNight;
	}

	public int getPunchLengthFullMoon() {
		return this.punchLengthFullMoon;
	}

	public void setPunchLengthFullMoon(final int punchLengthFullMoon) {
		this.punchLengthFullMoon = punchLengthFullMoon;
	}

	public int getActiveLength() {
		return this.activeLength;
	}

	public void setActiveLength(final int activeLength) {
		this.activeLength = activeLength;
	}

	public int getWhipSpeed() {
		return this.whipSpeed;
	}

	public void setWhipSpeed(final int whipSpeed) {
		this.whipSpeed = whipSpeed;
	}

	public long getGrabDuration() {
		return this.grabDuration;
	}

	public void setGrabDuration(final long grabDuration) {
		this.grabDuration = grabDuration;
	}

	public long getUsageCooldown() {
		return this.usageCooldown;
	}

	public void setUsageCooldown(final long usageCooldown) {
		this.usageCooldown = usageCooldown;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public double getPullMultiplier() {
		return this.pullMultiplier;
	}

	public void setPullMultiplier(final double pullMultiplier) {
		this.pullMultiplier = pullMultiplier;
	}

	public double getPunchDamage() {
		return this.punchDamage;
	}

	public void setPunchDamage(final double punchDamage) {
		this.punchDamage = punchDamage;
	}

	public double getPlayerHealth() {
		return this.playerHealth;
	}

	public void setPlayerHealth(final double playerHealth) {
		this.playerHealth = playerHealth;
	}

	public Arm getArm() {
		return this.arm;
	}

	public void setArm(final Arm arm) {
		this.arm = arm;
	}

	public Whip getAbility() {
		return this.ability;
	}

	public void setAbility(final Whip ability) {
		this.ability = ability;
	}

	public LivingEntity getGrabbedEntity() {
		return this.grabbedEntity;
	}

	public void setGrabbedEntity(final LivingEntity grabbedEntity) {
		this.grabbedEntity = grabbedEntity;
	}

	public Location getEnd() {
		return this.end;
	}

	public void setEnd(final Location end) {
		this.end = end;
	}

	public WaterArms getWaterArms() {
		return this.waterArms;
	}

	public void setWaterArms(final WaterArms waterArms) {
		this.waterArms = waterArms;
	}

	public static HashMap<LivingEntity, WaterArmsWhip> getGrabbedEntities() {
		return GRABBED_ENTITIES;
	}

}
