package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.firebending.Combustion;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.Illumination;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class AirSwipe extends AirAbility {

	// Limiting the entities reduces the risk of crashing
	private static final int MAX_AFFECTABLE_ENTITIES = 10;
	private static final Integer[] BREAKABLES = {6, 31, 32, 37, 38, 39, 40, 59, 81, 83, 106, 175};
	
	private boolean charging;
	private int arc;
	private int particles;
	private int stepSize;
	private long maxChargeTime;
	private long cooldown;
	private double damage;
	private double pushFactor;
	private double speed;
	private double range;
	private double radius;
	private double maxChargeFactor;
	private Location origin;
	private Random random;
	private Map<Vector, Location> elements;
	private ArrayList<Entity> affectedEntities;
	
	public AirSwipe(Player player) {
		this(player, false);
	}

	public AirSwipe(Player player, boolean charging) {
		super(player);
		
		if (CoreAbility.hasAbility(player, AirSwipe.class)) {
			for (AirSwipe ability : CoreAbility.getAbilities(player, AirSwipe.class)) {
				if (ability.charging) {
					ability.launch();
					ability.charging = false;
					return;
				}
			}
		}
		
		this.charging = charging;
		this.origin = player.getEyeLocation();
		this.particles = getConfig().getInt("Abilities.Air.AirSwipe.Particles");
		this.arc = getConfig().getInt("Abilities.Air.AirSwipe.Arc");
		this.stepSize = getConfig().getInt("Abilities.Air.AirSwipe.StepSize");
		this.maxChargeTime = getConfig().getLong("Abilities.Air.AirSwipe.MaxChargeTime");
		this.cooldown = getConfig().getLong("Abilities.Air.AirSwipe.Cooldown");
		this.damage = getConfig().getDouble("Abilities.Air.AirSwipe.Damage");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirSwipe.Push");
		this.speed = getConfig().getDouble("Abilities.Air.AirSwipe.Speed") * (ProjectKorra.time_step / 1000.0);
		this.range = getConfig().getDouble("Abilities.Air.AirSwipe.Range");
		this.radius = getConfig().getDouble("Abilities.Air.AirSwipe.Radius");
		this.maxChargeFactor = getConfig().getDouble("Abilities.Air.AirSwipe.ChargeFactor");
		this.random = new Random();
		this.elements = new ConcurrentHashMap<>();
		this.affectedEntities = new ArrayList<>();
		
		if (bPlayer.isOnCooldown(this) || player.getEyeLocation().getBlock().isLiquid()) {
			remove();
			return;
		}
		
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}
		
		if (!charging) {
			launch();
		}
		start();
	}

	public static boolean removeSwipesAroundPoint(Location loc, double radius) {
		boolean removed = false;
		for (AirSwipe aswipe : getAbilities(AirSwipe.class)) {
			for (Vector vec : aswipe.elements.keySet()) {
				Location vectorLoc = aswipe.elements.get(vec);
				if (vectorLoc != null && vectorLoc.getWorld().equals(loc.getWorld())) {
					if (vectorLoc.distanceSquared(loc) <= radius * radius) {
						aswipe.remove();
						removed = true;
					}
				}
			}
		}
		return removed;
	}

	@SuppressWarnings("deprecation")
	private void advanceSwipe() {
		affectedEntities.clear();
		for (Vector direction : elements.keySet()) {
			Location location = elements.get(direction);
			if (direction != null && location != null) {
				location = location.clone().add(direction.clone().multiply(speed));
				elements.put(direction, location);

				if (location.distanceSquared(origin) > range * range
						|| GeneralMethods.isRegionProtectedFromBuild(this, location)) {
					elements.remove(direction);
				} else {
					removeAirSpouts(location, player);
					WaterAbility.removeWaterSpouts(location, player);
					EarthAbility.removeSandSpouts(location, player);
					
					
					if (EarthBlast.annihilateBlasts(location, radius, player)
							|| WaterManipulation.annihilateBlasts(location, radius, player)
							|| FireBlast.annihilateBlasts(location, radius, player)
							|| Combustion.removeAroundPoint(location, radius)) {
						elements.remove(direction);
						damage = 0;
						remove();
						continue;
					}

					Block block = location.getBlock();
					if (!EarthAbility.isTransparent(player, block)) {
						remove();
						return;
					}
					
					for (Block testblock : GeneralMethods.getBlocksAroundPoint(location, radius)) {
						if (testblock.getType() == Material.FIRE) {
							testblock.setType(Material.AIR);
						}
						if (isBlockBreakable(testblock)) {
							GeneralMethods.breakBlock(testblock);
						}
					}

					if (block.getType() != Material.AIR) {
						if (isBlockBreakable(block)) {
							GeneralMethods.breakBlock(block);
						} else {
							elements.remove(direction);
						}
						if (isLava(block)) {
							if (block.getData() == 0x0) {
								block.setType(Material.OBSIDIAN);
							} else {
								block.setType(Material.COBBLESTONE);
							}
						}
					} else {
						playAirbendingParticles(location, particles, 0.2F, 0.2F, 0);
						if (random.nextInt(4) == 0) {
							playAirbendingSound(location);
						}
						affectPeople(location, direction);
					}
				}
			}
		}
		if (elements.isEmpty()) {
			remove();
		}
	}

	private void affectPeople(Location location, Vector direction) {
		WaterAbility.removeWaterSpouts(location, player);
		removeAirSpouts(location, player);
		removeAirSpouts(location, player);
		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(location, radius);
		final Vector fDirection = direction;

		for (int i = 0; i < entities.size(); i++) {
			final Entity entity = entities.get(i);
			final AirSwipe abil = this;
			new BukkitRunnable() {
				public void run() {
					if (GeneralMethods.isRegionProtectedFromBuild(AirSwipe.this, entity.getLocation())) {
						return;
					}
					if (entity.getEntityId() != player.getEntityId() && entity instanceof LivingEntity) {
						if (entity instanceof Player) {
							if (Commands.invincible.contains(((Player) entity).getName())) {
								return;
							}
						}
						if (entities.size() < MAX_AFFECTABLE_ENTITIES) {
							if (bPlayer.isAvatarState()) {
								GeneralMethods.setVelocity(entity, fDirection.multiply(AvatarState.getValue(pushFactor)));
							} else {
								GeneralMethods.setVelocity(entity, fDirection.multiply(pushFactor));
							}
						}
						if (entity instanceof LivingEntity && !affectedEntities.contains(entity)) {
							if (damage != 0) {
								DamageHandler.damageEntity(entity, damage, abil);
							}
							affectedEntities.add(entity);
						}
						if (entity instanceof Player) {
							new Flight((Player) entity, player);
						}
						breakBreathbendingHold(entity);
						if (elements.containsKey(fDirection)) {
							elements.remove(fDirection);
						}
					} else if (entity.getEntityId() != player.getEntityId() && !(entity instanceof LivingEntity)) {
						if (bPlayer.isAvatarState()) {
							GeneralMethods.setVelocity(entity, fDirection.multiply(AvatarState.getValue(pushFactor)));
						} else {
							GeneralMethods.setVelocity(entity, fDirection.multiply(pushFactor));
						}
					}
				}
			}.runTaskLater(ProjectKorra.plugin, i / MAX_AFFECTABLE_ENTITIES);
		}
	}

	@SuppressWarnings("deprecation")
	private boolean isBlockBreakable(Block block) {
		Integer id = block.getTypeId();
		if (Arrays.asList(BREAKABLES).contains(id) && !Illumination.getBlocks().containsKey(block)) {
			return true;
		}
		return false;
	}

	private void launch() {
		bPlayer.addCooldown("AirSwipe", cooldown);
		origin = player.getEyeLocation();
		for (double i = -arc; i <= arc; i += stepSize) {
			double angle = Math.toRadians((double) i);
			Vector direction = player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			elements.put(direction, origin);
		}
	}

	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		
		if (!charging) {
			if (elements.isEmpty()) {
				remove();
				return;
			}
			advanceSwipe();
		} else {
			if (!player.isSneaking()) {
				double factor = 1;
				if (System.currentTimeMillis() >= startTime + maxChargeTime) {
					factor = maxChargeFactor;
				} else if (bPlayer.isAvatarState()) {
					factor = AvatarState.getValue(factor);
				} else {
					factor = maxChargeFactor * (double) (System.currentTimeMillis() - startTime) / (double) maxChargeTime;
				}

				charging = false;
				launch();
				factor = Math.max(1, factor);
				damage *= factor;
				pushFactor *= factor;
			} else if (System.currentTimeMillis() >= startTime + maxChargeTime) {
				playAirbendingParticles(player.getEyeLocation(), particles);
			}
		}
	}

	@Override
	public String getName() {
		return "AirSwipe";
	}

	@Override
	public Location getLocation() {
		return elements.size() != 0 ? elements.values().iterator().next() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public boolean isCharging() {
		return charging;
	}

	public void setCharging(boolean charging) {
		this.charging = charging;
	}

	public int getArc() {
		return arc;
	}

	public void setArc(int arc) {
		this.arc = arc;
	}

	public int getParticles() {
		return particles;
	}

	public void setParticles(int particles) {
		this.particles = particles;
	}

	public static int getMaxAffectableEntities() {
		return MAX_AFFECTABLE_ENTITIES;
	}

	public static Integer[] getBreakables() {
		return BREAKABLES;
	}

	public long getMaxChargeTime() {
		return maxChargeTime;
	}

	public void setMaxChargeTime(long maxChargeTime) {
		this.maxChargeTime = maxChargeTime;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getMaxChargeFactor() {
		return maxChargeFactor;
	}

	public void setMaxChargeFactor(double maxChargeFactor) {
		this.maxChargeFactor = maxChargeFactor;
	}

	public Map<Vector, Location> getElements() {
		return elements;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public int getStepSize() {
		return stepSize;
	}

	public void setStepSize(int stepSize) {
		this.stepSize = stepSize;
	}
	
}
