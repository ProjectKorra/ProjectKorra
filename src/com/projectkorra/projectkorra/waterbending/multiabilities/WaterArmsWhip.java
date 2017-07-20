package com.projectkorra.projectkorra.waterbending.multiabilities;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.MultiAbilityManager;
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
	private int whipLength;
	private int whipLengthWeak;
	private int whipLengthNight;
	private int whipLengthFullMoon;
	private int initLength;
	private int punchLength;
	private int punchLengthNight;
	private int punchLengthFullMoon;
	private int activeLength;
	private int whipSpeed;
	private long holdTime;
	private long usageCooldown;
	private long time;
	private double pullMultiplier;
	private double punchDamage;
	private double playerHealth;
	private Arm arm;
	private Whip ability;
	private LivingEntity grabbedEntity;
	private Location end;
	private WaterArms waterArms;

	public WaterArmsWhip(Player player, Whip ability) {
		super(player);

		this.ability = ability;
		this.reverting = false;
		this.hasDamaged = false;
		this.grappled = false;
		this.grabbed = false;
		this.grappleRespectRegions = getConfig().getBoolean("Abilities.Water.WaterArms.Whip.Grapple.RespectRegions");
		this.usageCooldownEnabled = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled");
		this.whipLength = getConfig().getInt("Abilities.Water.WaterArms.Whip.MaxLength");
		this.whipLengthWeak = getConfig().getInt("Abilities.Water.WaterArms.Whip.MaxLengthWeak");
		this.whipLengthNight = getConfig().getInt("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.Normal");
		this.whipLengthFullMoon = getConfig().getInt("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.FullMoon");
		this.initLength = getConfig().getInt("Abilities.Water.WaterArms.Arms.InitialLength");
		this.punchLength = getConfig().getInt("Abilities.Water.WaterArms.Whip.Punch.MaxLength");
		this.punchLengthNight = getConfig().getInt("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.Normal");
		this.punchLengthFullMoon = getConfig().getInt("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.FullMoon");
		this.activeLength = initLength;
		this.whipSpeed = 2;
		this.holdTime = getConfig().getLong("Abilities.Water.WaterArms.Whip.Grab.HoldTime");
		this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown");
		this.pullMultiplier = getConfig().getDouble("Abilities.Water.WaterArms.Whip.Pull.Multiplier");
		this.punchDamage = getConfig().getDouble("Abilities.Water.WaterArms.Whip.Punch.PunchDamage");

		WaterArmsWhip waw = getAbility(player, WaterArmsWhip.class);
		if (waw != null) {
			if (waw.grabbed) {
				waw.grabbed = false;
				if (waw.grabbedEntity != null) {
					GRABBED_ENTITIES.remove(waw.grabbedEntity);
					waw.grabbedEntity.setVelocity(waw.grabbedEntity.getVelocity().multiply(2.5));
				}
				return;
			}
			if (!waw.arm.equals(getAbility(player, WaterArms.class).getActiveArm())) {
				return;
			}
		}

		getAugments();
		createInstance();
	}

	private void getAugments() {
		if (ability.equals(Whip.PUNCH)) {
			whipLength = punchLength;
		}
		World world = player.getWorld();
		if (isNight(world)) {
			if (GeneralMethods.hasRPG()) {
				if (isLunarEclipse(world)) {
					if (ability.equals(Whip.PUNCH)) {
						whipLength = punchLengthFullMoon;
					} else {
						whipLength = whipLengthFullMoon;
					}
				} else if (isFullMoon(world)) {
					if (ability.equals(Whip.PUNCH)) {
						whipLength = punchLengthFullMoon;
					} else {
						whipLength = whipLengthFullMoon;
					}
				} else {
					if (ability.equals(Whip.PUNCH)) {
						whipLength = punchLengthNight;
					} else {
						whipLength = whipLengthNight;
					}
				}
			} else {
				if (isFullMoon(world)) {
					if (ability.equals(Whip.PUNCH)) {
						whipLength = punchLengthFullMoon;
					} else {
						whipLength = whipLengthFullMoon;
					}
				} else {
					if (ability.equals(Whip.PUNCH)) {
						whipLength = punchLengthNight;
					} else {
						whipLength = whipLengthNight;
					}
				}
			}
		}
	}

	private void createInstance() {
		waterArms = getAbility(player, WaterArms.class);
		if (waterArms != null) {
			waterArms.switchPreferredArm();
			arm = waterArms.getActiveArm();
			time = System.currentTimeMillis() + holdTime;
			playerHealth = player.getHealth();

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
		} else {
			return;
		}

		if (!waterArms.isFullSource()) {
			whipLength = whipLengthWeak;
		}
		start();
	}

	@Override
	public void progress() {
		if (!hasAbility(player, WaterArms.class)) {
			remove();
			return;
		} else if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!MultiAbilityManager.hasMultiAbilityBound(player, "WaterArms")) {
			remove();
			return;
		}

		if (activeLength < whipLength && !reverting) {
			activeLength += whipSpeed;
		} else if (activeLength > initLength) {
			if (!grabbed) {
				activeLength -= whipSpeed;
			}
		} else {
			remove();
			return;
		}

		if (activeLength >= whipLength && !grabbed) {
			reverting = true;
		}

		if (grabbed && (System.currentTimeMillis() > time || playerHealth > player.getHealth())) {
			grabbed = false;
			reverting = true;
		}

		useArm();
		dragEntity(end);
	}

	private boolean canPlaceBlock(Block block) {
		if (!isTransparent(player, block) && !(isWater(block) && TempBlock.isTempBlock(block))) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return false;
		}
		return true;
	}

	private void useArm() {
		if (waterArms.canDisplayActiveArm()) {
			Location l1 = null;

			if (arm.equals(Arm.LEFT)) {
				l1 = waterArms.getLeftArmEnd().clone();
			} else {
				l1 = waterArms.getRightArmEnd().clone();
			}

			Vector dir = player.getLocation().getDirection();
			for (int i = 1; i <= activeLength; i++) {
				Location l2 = l1.clone().add(dir.normalize().multiply(i));

				if (!canPlaceBlock(l2.getBlock())) {
					if (!l2.getBlock().getType().equals(Material.BARRIER)) {
						grappled = true;
					}
					reverting = true;
					break;
				}

				new TempBlock(l2.getBlock(), Material.STATIONARY_WATER, (byte) 8);
				WaterArms.getBlockRevertTimes().put(l2.getBlock(), System.currentTimeMillis() + 10);

				if (i == activeLength) {
					Location l3 = null;
					if (arm.equals(Arm.LEFT)) {
						l3 = GeneralMethods.getRightSide(l2, 1);
					} else {
						l3 = GeneralMethods.getLeftSide(l2, 1);
					}

					end = l3.clone();
					if (canPlaceBlock(l3.getBlock())) {
						new TempBlock(l3.getBlock(), Material.STATIONARY_WATER, (byte) 3);
						WaterArms.getBlockRevertTimes().put(l3.getBlock(), System.currentTimeMillis() + 10);
						performAction(l3);
					} else {
						if (!l3.getBlock().getType().equals(Material.BARRIER)) {
							grappled = true;
						}
						reverting = true;
					}
				}
			}
		}
	}

	private void performAction(Location location) {
		Location endOfArm = waterArms.getLeftArmEnd().clone();
		switch (ability) {
			case PULL:
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (entity instanceof Player && Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}
					Vector vector = endOfArm.toVector().subtract(entity.getLocation().toVector());
					entity.setVelocity(vector.multiply(pullMultiplier));
				}
				break;
			case PUNCH:
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (entity instanceof Player && Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}

					Vector vector = entity.getLocation().toVector().subtract(endOfArm.toVector());
					entity.setVelocity(vector.multiply(0.15));
					if (entity instanceof LivingEntity) {
						if (entity.getEntityId() != player.getEntityId()) {
							hasDamaged = true;
							DamageHandler.damageEntity(entity, punchDamage, this);
						}
					}
				}
				break;
			case GRAPPLE:
				grapplePlayer(end);
				break;
			case GRAB:
				if (grabbedEntity == null) {
					for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
						if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !GRABBED_ENTITIES.containsKey(entity)) {
							GRABBED_ENTITIES.put((LivingEntity) entity, this);
							grabbedEntity = (LivingEntity) entity;
							grabbed = true;
							reverting = true;
							waterArms.setActiveArmCooldown(true);
							break;
						}
					}
				}
				break;
			default:
				break;
		}
	}

	private void dragEntity(Location location) {
		if (grabbedEntity != null && grabbed) {
			if (!waterArms.canDisplayActiveArm() || grabbedEntity.isDead()) {
				grabbed = false;
				GRABBED_ENTITIES.remove(grabbedEntity);
				return;
			}
			if (grabbedEntity instanceof Player && hasAbility((Player) grabbedEntity, WaterArmsWhip.class)) {
				WaterArmsWhip waw = getAbility((Player) grabbedEntity, WaterArmsWhip.class);
				if (waw.getAbility().equals(Whip.GRAB)) {
					grabbed = false;
					GRABBED_ENTITIES.remove(grabbedEntity);
					return;
				}
			}

			Location newLocation = grabbedEntity.getLocation();
			double distance = 0;
			if (location.getWorld().equals(newLocation.getWorld())) {
				distance = location.distance(newLocation);
			}

			double dx, dy, dz;
			dx = location.getX() - newLocation.getX();
			dy = location.getY() - newLocation.getY();
			dz = location.getZ() - newLocation.getZ();
			Vector vector = new Vector(dx, dy, dz);

			if (distance > 0.5) {
				grabbedEntity.setVelocity(vector.normalize().multiply(.65));
			} else {
				grabbedEntity.setVelocity(new Vector(0, 0, 0));
			}

			grabbedEntity.setFallDistance(0);
			if (grabbedEntity instanceof Creature) {
				((Creature) grabbedEntity).setTarget(null);
			}
		}
	}

	private void grapplePlayer(Location location) {
		if (reverting && grappled && player != null && end != null && ability.equals(Whip.GRAPPLE)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, location) && grappleRespectRegions) {
				return;
			}
			Vector vector = player.getLocation().toVector().subtract(location.toVector());
			player.setVelocity(vector.multiply(-0.25));
			player.setFallDistance(0);
		}
	}

	public static void checkValidEntities() {
		for (LivingEntity livingEnt : GRABBED_ENTITIES.keySet()) {
			WaterArmsWhip whip = GRABBED_ENTITIES.get(livingEnt);
			if (!whip.isRemoved()) {
				if (whip.grabbedEntity == null) {
					GRABBED_ENTITIES.remove(livingEnt);
				}
			} else {
				GRABBED_ENTITIES.remove(livingEnt);
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (hasAbility(player, WaterArms.class)) {
			if (arm.equals(Arm.LEFT)) {
				waterArms.setLeftArmCooldown(false);
			} else {
				waterArms.setRightArmCooldown(false);
			}
			if (hasDamaged) {
				waterArms.setMaxPunches(waterArms.getMaxPunches() - 1);
			}
			
			waterArms.setMaxUses(waterArms.getMaxUses() - 1);
		}
	}

	public static void progressAllCleanup() {
		checkValidEntities();
	}

	public static void removeAllCleanup() {
		GRABBED_ENTITIES.clear();
	}

	@Override
	public String getName() {
		return "WaterArms";
	}

	@Override
	public Location getLocation() {
		return end;
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

	public boolean isReverting() {
		return reverting;
	}

	public void setReverting(boolean reverting) {
		this.reverting = reverting;
	}

	public boolean isHasDamaged() {
		return hasDamaged;
	}

	public void setHasDamaged(boolean hasDamaged) {
		this.hasDamaged = hasDamaged;
	}

	public boolean isGrappled() {
		return grappled;
	}

	public void setGrappled(boolean grappled) {
		this.grappled = grappled;
	}

	public boolean isGrabbed() {
		return grabbed;
	}

	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}

	public boolean isGrappleRespectRegions() {
		return grappleRespectRegions;
	}

	public void setGrappleRespectRegions(boolean grappleRespectRegions) {
		this.grappleRespectRegions = grappleRespectRegions;
	}

	public boolean isUsageCooldownEnabled() {
		return usageCooldownEnabled;
	}

	public void setUsageCooldownEnabled(boolean usageCooldownEnabled) {
		this.usageCooldownEnabled = usageCooldownEnabled;
	}

	public int getWhipLength() {
		return whipLength;
	}

	public void setWhipLength(int whipLength) {
		this.whipLength = whipLength;
	}

	public int getWhipLengthWeak() {
		return whipLengthWeak;
	}

	public void setWhipLengthWeak(int whipLengthWeak) {
		this.whipLengthWeak = whipLengthWeak;
	}

	public int getWhipLengthNight() {
		return whipLengthNight;
	}

	public void setWhipLengthNight(int whipLengthNight) {
		this.whipLengthNight = whipLengthNight;
	}

	public int getWhipLengthFullMoon() {
		return whipLengthFullMoon;
	}

	public void setWhipLengthFullMoon(int whipLengthFullMoon) {
		this.whipLengthFullMoon = whipLengthFullMoon;
	}

	public int getInitLength() {
		return initLength;
	}

	public void setInitLength(int initLength) {
		this.initLength = initLength;
	}

	public int getPunchLength() {
		return punchLength;
	}

	public void setPunchLength(int punchLength) {
		this.punchLength = punchLength;
	}

	public int getPunchLengthNight() {
		return punchLengthNight;
	}

	public void setPunchLengthNight(int punchLengthNight) {
		this.punchLengthNight = punchLengthNight;
	}

	public int getPunchLengthFullMoon() {
		return punchLengthFullMoon;
	}

	public void setPunchLengthFullMoon(int punchLengthFullMoon) {
		this.punchLengthFullMoon = punchLengthFullMoon;
	}

	public int getActiveLength() {
		return activeLength;
	}

	public void setActiveLength(int activeLength) {
		this.activeLength = activeLength;
	}

	public int getWhipSpeed() {
		return whipSpeed;
	}

	public void setWhipSpeed(int whipSpeed) {
		this.whipSpeed = whipSpeed;
	}

	public long getHoldTime() {
		return holdTime;
	}

	public void setHoldTime(long holdTime) {
		this.holdTime = holdTime;
	}

	public long getUsageCooldown() {
		return usageCooldown;
	}

	public void setUsageCooldown(long usageCooldown) {
		this.usageCooldown = usageCooldown;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getPullMultiplier() {
		return pullMultiplier;
	}

	public void setPullMultiplier(double pullMultiplier) {
		this.pullMultiplier = pullMultiplier;
	}

	public double getPunchDamage() {
		return punchDamage;
	}

	public void setPunchDamage(double punchDamage) {
		this.punchDamage = punchDamage;
	}

	public double getPlayerHealth() {
		return playerHealth;
	}

	public void setPlayerHealth(double playerHealth) {
		this.playerHealth = playerHealth;
	}

	public Arm getArm() {
		return arm;
	}

	public void setArm(Arm arm) {
		this.arm = arm;
	}

	public Whip getAbility() {
		return ability;
	}

	public void setAbility(Whip ability) {
		this.ability = ability;
	}

	public LivingEntity getGrabbedEntity() {
		return grabbedEntity;
	}

	public void setGrabbedEntity(LivingEntity grabbedEntity) {
		this.grabbedEntity = grabbedEntity;
	}

	public Location getEnd() {
		return end;
	}

	public void setEnd(Location end) {
		this.end = end;
	}

	public WaterArms getWaterArms() {
		return waterArms;
	}

	public void setWaterArms(WaterArms waterArms) {
		this.waterArms = waterArms;
	}

	public static HashMap<LivingEntity, WaterArmsWhip> getGrabbedEntities() {
		return GRABBED_ENTITIES;
	}

}
