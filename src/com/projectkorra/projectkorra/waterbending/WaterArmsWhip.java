package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingManager;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterArms.Arm;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class WaterArmsWhip {

	/**
	 * Whip Enum value for deciding what ability should be executed.
	 */
	public enum Whip {
		Pull, Punch, Grapple, Grab;
	}

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static ConcurrentHashMap<Integer, WaterArmsWhip> instances = new ConcurrentHashMap<Integer, WaterArmsWhip>();
	public static HashMap<LivingEntity, Integer> grabbedEntities = new HashMap<LivingEntity, Integer>();

	private Player player;
	private WaterArms waterArms;

	private int whipLength = config.getInt("Abilities.Water.WaterArms.Whip.MaxLength");
	private int whipLengthWeak = config.getInt("Abilities.Water.WaterArms.Whip.MaxLengthWeak");

	private int whipLengthNight = config.getInt("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.Normal");
	private int whipLengthFullMoon = config.getInt("Abilities.Water.WaterArms.Whip.NightAugments.MaxLength.FullMoon");

	private int initLength = config.getInt("Abilities.Water.WaterArms.Arms.InitialLength");
	private double pullMultiplier = config.getDouble("Abilities.Water.WaterArms.Whip.Pull.Multiplier");
	private double punchDamage = config.getDouble("Abilities.Water.WaterArms.Whip.Punch.PunchDamage");
	private int punchLength = config.getInt("Abilities.Water.WaterArms.Whip.Punch.MaxLength");
	private int punchLengthNight = config.getInt("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.Normal");
	private int punchLengthFullMoon = config.getInt("Abilities.Water.WaterArms.Whip.Punch.NightAugments.MaxLength.FullMoon");
	private boolean grappleRespectRegions = config.getBoolean("Abilities.Water.WaterArms.Whip.Grapple.RespectRegions");
	private long holdTime = config.getLong("Abilities.Water.WaterArms.Whip.Grab.HoldTime");
	private boolean usageCooldownEnabled = config.getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled");
	private long usageCooldown = config.getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown");

	private int activeLength = initLength;
	private int whipSpeed = 2;
	private boolean reverting = false;
	private boolean hasDamaged = false;
	private boolean grappled = false;
	private boolean grabbed = false;
	private double playerHealth;
	private long time;

	private LivingEntity grabbedEntity;
	private Location end;
	private Arm arm;
	private Whip ability;

	private int id;
	private static int ID = Integer.MIN_VALUE;

	public WaterArmsWhip(Player player, Whip ability) {
		if (instances.containsKey(getId(player))) {
			WaterArmsWhip waw = instances.get(getId(player));
			if (waw.grabbed) {
				waw.grabbed = false;
				if (waw.grabbedEntity != null) {
					grabbedEntities.remove(waw.grabbedEntity);
					waw.grabbedEntity.setVelocity(waw.grabbedEntity.getVelocity().multiply(2.5));
				}
				return;
			}
			if (!waw.arm.equals(WaterArms.instances.get(player).getActiveArm())) {
				return;
			}
		}
		this.player = player;
		this.ability = ability;
		getAugments();
		createInstance();
	}

	private void getAugments() {
		if (ability.equals(Whip.Punch)) {
			whipLength = punchLength;
		}
		World world = player.getWorld();
		if (WaterMethods.isNight(world)) {
			if (GeneralMethods.hasRPG()) {
				if (BendingManager.events.get(world).equalsIgnoreCase(WorldEvents.LunarEclipse.toString())) {
					if (ability.equals(Whip.Punch)) {
						whipLength = punchLengthFullMoon;
					} else {
						whipLength = whipLengthFullMoon;
					}
				} else if (BendingManager.events.get(world).equalsIgnoreCase("FullMoon")) {
					if (ability.equals(Whip.Punch)) {
						whipLength = punchLengthFullMoon;
					} else {
						whipLength = whipLengthFullMoon;
					}
				} else {
					if (ability.equals(Whip.Punch)) {
						whipLength = punchLengthNight;
					} else {
						whipLength = whipLengthNight;
					}
				}
			} else {
				if (WaterMethods.isFullMoon(world)) {
					if (ability.equals(Whip.Punch)) {
						whipLength = punchLengthFullMoon;
					} else {
						whipLength = whipLengthFullMoon;
					}
				} else {
					if (ability.equals(Whip.Punch)) {
						whipLength = punchLengthNight;
					} else {
						whipLength = whipLengthNight;
					}
				}
			}
		}
	}

	private void createInstance() {
		if (WaterArms.instances.containsKey(player)) {
			waterArms = WaterArms.instances.get(player);
			waterArms.switchPreferredArm();
			arm = waterArms.getActiveArm();
			time = System.currentTimeMillis() + holdTime;
			playerHealth = player.getHealth();
			BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
			if (arm.equals(Arm.Left)) {
				if (waterArms.isLeftArmCooldown() || bPlayer.isOnCooldown("WaterArms_LEFT")) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_LEFT", usageCooldown);
					}
					waterArms.setLeftArmCooldown(true);
				}
			}
			if (arm.equals(Arm.Right)) {
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
		id = ID;
		instances.put(id, this);
		if (ID == Integer.MAX_VALUE)
			ID = Integer.MIN_VALUE;
		ID++;
	}

	private void progress() {
		if (!WaterArms.instances.containsKey(player)) {
			remove();
			return;
		}
		if (player.isDead() || !player.isOnline()) {
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
		grapplePlayer(end);
	}

	private boolean canPlaceBlock(Block block) {
		if (!EarthMethods.isTransparentToEarthbending(player, block) && !(WaterMethods.isWater(block) && TempBlock.isTempBlock(block))) {
			return false;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterArms", block.getLocation())) {
			return false;
		}
		return true;
	}

	private void useArm() {
		if (waterArms.canDisplayActiveArm()) {
			Location l1 = null;
			if (arm.equals(Arm.Left)) {
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
				WaterArms.revert.put(l2.getBlock(), 0L);

				if (i == activeLength) {
					Location l3 = null;
					if (arm.equals(Arm.Left)) {
						l3 = GeneralMethods.getRightSide(l2, 1);
					} else {
						l3 = GeneralMethods.getLeftSide(l2, 1);
					}
					end = l3.clone();
					if (canPlaceBlock(l3.getBlock())) {
						new TempBlock(l3.getBlock(), Material.STATIONARY_WATER, (byte) 3);
						WaterArms.revert.put(l3.getBlock(), 0L);
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
			case Pull:
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (entity instanceof Player && Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}
					Vector vector = endOfArm.toVector().subtract(entity.getLocation().toVector());
					entity.setVelocity(vector.multiply(pullMultiplier));
				}
				break;
			case Punch:
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (entity instanceof Player && Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}
					Vector vector = entity.getLocation().toVector().subtract(endOfArm.toVector());
					entity.setVelocity(vector.multiply(0.15));
					if (entity instanceof LivingEntity) {
						if (entity.getEntityId() != player.getEntityId()) {
							hasDamaged = true;
							GeneralMethods.damageEntity(player, entity, punchDamage, WaterMethods.getWaterColor() + "WaterArms Punch");
						}
					}
				}
				break;
			case Grapple:
				grapplePlayer(end);
				break;
			case Grab:
				if (grabbedEntity == null) {
					for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
						if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !grabbedEntities.containsKey(entity)) {
							grabbedEntities.put((LivingEntity) entity, id);
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
				grabbedEntities.remove(grabbedEntity);
				return;
			}
			Location newlocation = grabbedEntity.getLocation();
			double distance = location.distance(newlocation);
			double dx, dy, dz;
			dx = location.getX() - newlocation.getX();
			dy = location.getY() - newlocation.getY();
			dz = location.getZ() - newlocation.getZ();
			Vector vector = new Vector(dx, dy, dz);
			if (distance > .5) {
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
		if (reverting && grappled && player != null && end != null && ability.equals(Whip.Grapple)) {
			if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterArms", location) && grappleRespectRegions) {
				return;
			}
			Vector vector = player.getLocation().toVector().subtract(location.toVector());
			player.setVelocity(vector.multiply(-0.25));
			player.setFallDistance(0);
		}
	}

	public static Integer getId(Player player) {
		for (int id : instances.keySet()) {
			if (instances.get(id).player.equals(player)) {
				return id;
			}
		}
		return 0;
	}

	public static void checkValidEntities() {
		for (LivingEntity e : grabbedEntities.keySet()) {
			if (instances.containsKey(grabbedEntities.get(e))) {
				if (instances.get(grabbedEntities.get(e)).grabbedEntity == null) {
					grabbedEntities.remove(e);
				}
			} else {
				grabbedEntities.remove(e);
			}
		}
	}

	private void remove() {
		if (WaterArms.instances.containsKey(player)) {
			if (arm.equals(Arm.Left)) {
				waterArms.setLeftArmCooldown(false);
			} else {
				waterArms.setRightArmCooldown(false);
			}
			if (hasDamaged) {
				waterArms.setMaxPunches(waterArms.getMaxPunches() - 1);
			}
			waterArms.setMaxUses(waterArms.getMaxUses() - 1);
		}
		instances.remove(id);
	}

	public static void progressAll() {
		checkValidEntities();
		for (int ID : instances.keySet())
			instances.get(ID).progress();
	}

	public static void removeAll() {
		grabbedEntities.clear();
		instances.clear();
	}

	public Player getPlayer() {
		return player;
	}

	public Integer getWhipLength() {
		return whipLength;
	}

	public void setArmLength(int armLength) {
		this.whipLength = armLength;
	}

	public Double getPunchDamage() {
		return punchDamage;
	}

	public void setPunchDamage(double damage) {
		this.punchDamage = damage;
	}

	public long getHoldTime() {
		return holdTime;
	}

	public void setHoldTime(long holdTime) {
		this.holdTime = holdTime;
	}

	public boolean getReverting() {
		return reverting;
	}

	public void setReverting(boolean reverting) {
		this.reverting = reverting;
	}

	public boolean getGrappled() {
		return grappled;
	}

	public void setGrappled(boolean grappled) {
		this.grappled = grappled;
	}

	public boolean getGrabbed() {
		return grabbed;
	}

	public void setGrabbed(boolean grabbed) {
		this.grabbed = grabbed;
	}

	public LivingEntity getHeldEntity() {
		return grabbedEntity;
	}
}
