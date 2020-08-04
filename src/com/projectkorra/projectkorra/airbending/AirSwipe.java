package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.earthbending.lava.LavaFlow;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;

public class AirSwipe extends AirAbility {

	// Limiting the entities reduces the risk of crashing.
	private static final int MAX_AFFECTABLE_ENTITIES = 10;

	private boolean charging;
	@Attribute("Arc")
	private int arc;
	private int particles;
	@Attribute("ArcIncrement")
	private int arcIncrement;
	@Attribute(Attribute.CHARGE_DURATION)
	private long maxChargeTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double pushFactor;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	private double maxChargeFactor;
	private Location origin;
	private Random random;
	private Map<Vector, Location> streams;
	private ArrayList<Entity> affectedEntities;

	public AirSwipe(final Player player) {
		this(player, false);
	}

	public AirSwipe(final Player player, final boolean charging) {
		super(player);

		if (CoreAbility.hasAbility(player, AirSwipe.class)) {
			for (final AirSwipe ability : CoreAbility.getAbilities(player, AirSwipe.class)) {
				if (ability.charging) {
					ability.launch();
					ability.charging = false;
					return;
				}
			}
		}

		this.charging = charging;
		this.origin = GeneralMethods.getMainHandLocation(player);
		this.particles = getConfig().getInt("Abilities.Air.AirSwipe.Particles");
		this.arc = getConfig().getInt("Abilities.Air.AirSwipe.Arc");
		this.arcIncrement = getConfig().getInt("Abilities.Air.AirSwipe.StepSize");
		this.maxChargeTime = getConfig().getLong("Abilities.Air.AirSwipe.MaxChargeTime");
		this.cooldown = getConfig().getLong("Abilities.Air.AirSwipe.Cooldown");
		this.damage = getConfig().getDouble("Abilities.Air.AirSwipe.Damage");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirSwipe.Push");
		this.speed = getConfig().getDouble("Abilities.Air.AirSwipe.Speed") * (ProjectKorra.time_step / 1000.0);
		this.range = getConfig().getDouble("Abilities.Air.AirSwipe.Range");
		this.radius = getConfig().getDouble("Abilities.Air.AirSwipe.Radius");
		this.maxChargeFactor = getConfig().getDouble("Abilities.Air.AirSwipe.ChargeFactor");
		this.random = new Random();
		this.streams = new ConcurrentHashMap<>();
		this.affectedEntities = new ArrayList<>();

		if (this.bPlayer.isOnCooldown(this) || player.getEyeLocation().getBlock().isLiquid()) {
			this.remove();
			return;
		}

		if (!this.bPlayer.canBend(this)) {
			this.remove();
			return;
		}

		if (!charging) {
			this.launch();
		}

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Air.AirSwipe.Cooldown");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSwipe.Damage");
			this.pushFactor = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSwipe.Push");
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSwipe.Range");
			this.radius = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSwipe.Radius");
		}

		this.start();
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeSwipesAroundPoint(final Location loc, final double radius) {
		boolean removed = false;
		for (final AirSwipe aswipe : getAbilities(AirSwipe.class)) {
			for (final Vector vec : aswipe.streams.keySet()) {
				final Location vectorLoc = aswipe.streams.get(vec);
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

	private void advanceSwipe() {
		this.affectedEntities.clear();
		for (final Vector direction : this.streams.keySet()) {
			Location location = this.streams.get(direction);
			if (direction != null && location != null) {

				BlockIterator blocks = new BlockIterator(this.getLocation().getWorld(), location.toVector(), direction, 0, (int) Math.ceil(direction.clone().multiply(speed).length()));

				while (blocks.hasNext()) {
					if(!checkLocation(blocks.next(), direction)) {
						this.streams.remove(direction);
						break;
					}
				}
				
				if(!this.streams.containsKey(direction)) {
					continue;
				}

				location = location.clone().add(direction.clone().multiply(this.speed));
				this.streams.put(direction, location);
				playAirbendingParticles(location, this.particles, 0.2F, 0.2F, 0);
				if (this.random.nextInt(4) == 0) {
					playAirbendingSound(location);
				}
				this.affectPeople(location, direction);
			}
		}
		if (this.streams.isEmpty()) {
			this.remove();
		}
	}
	public boolean checkLocation(Block block, Vector direction) {
		if (GeneralMethods.checkDiagonalWall(block.getLocation(), direction) || !block.isPassable()) {
			return false;
		}  else {
			if (block.getLocation().distanceSquared(this.origin) > this.range * this.range || GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				this.streams.clear();
			} else {
				if (!ElementalAbility.isTransparent(this.player, block) || !block.isPassable()) {
					return false;
				}

				for (final Block testblock : GeneralMethods.getBlocksAroundPoint(block.getLocation(), this.radius)) {
					if (FireAbility.isFire(testblock.getType())) {
						testblock.setType(Material.AIR);
					}
				}

				if (!isAir(block.getType())) {
					if (block.getType().equals(Material.SNOW)) {
						return true;
					} else if (isPlant(block.getType())) {
						block.breakNaturally();
						return false;
					} else if (isLava(block)) {
						if (LavaFlow.isLavaFlowBlock(block)) {
							LavaFlow.removeBlock(block);
							return false;// TODO: Make more generic for future lava generating moves.
						} else if (block.getBlockData() instanceof Levelled && ((Levelled) block.getBlockData()).getLevel() == 0) {
							new TempBlock(block, Material.OBSIDIAN);
							return false;
						} else {
							new TempBlock(block, Material.COBBLESTONE);
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
	private void affectPeople(final Location location, final Vector direction) {
		final List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(location, this.radius);
		final Vector fDirection = direction.clone();

		for (int i = 0; i < entities.size(); i++) {
			Location entityLocation = entities.get(i).getLocation();
			Vector dir = new Vector(entityLocation.getX() - location.getX(), entityLocation.getY() - location.getY(), entityLocation.getZ() - location.getZ());
			if (GeneralMethods.checkDiagonalWall(location, dir)) {
				entities.remove(entities.get(i--));
			}
		}

		for (int i = 0; i < entities.size(); i++) {
			final Entity entity = entities.get(i);
			final AirSwipe abil = this;
			new BukkitRunnable() {
				@Override
				public void run() {
					if (GeneralMethods.isRegionProtectedFromBuild(AirSwipe.this, entity.getLocation())) {
						return;
					}
					if (entity.getEntityId() != AirSwipe.this.player.getEntityId() && entity instanceof LivingEntity) {
						if (entity instanceof Player) {
							if (Commands.invincible.contains(((Player) entity).getName())) {
								return;
							}
						}
						if (entities.size() < MAX_AFFECTABLE_ENTITIES) {

							GeneralMethods.setVelocity(entity, fDirection.multiply(AirSwipe.this.pushFactor));

						}
						if (!AirSwipe.this.affectedEntities.contains(entity)) {
							if (AirSwipe.this.damage != 0) {
								DamageHandler.damageEntity(entity, AirSwipe.this.damage, abil);
							}
							AirSwipe.this.affectedEntities.add(entity);
						}
						breakBreathbendingHold(entity);
						AirSwipe.this.streams.remove(direction);
					} else if (entity.getEntityId() != AirSwipe.this.player.getEntityId() && !(entity instanceof LivingEntity)) {

						GeneralMethods.setVelocity(entity, fDirection.multiply(AirSwipe.this.pushFactor));

					}
				}
			}.runTaskLater(ProjectKorra.plugin, i / MAX_AFFECTABLE_ENTITIES);
		}
	}

	private void launch() {
		this.bPlayer.addCooldown("AirSwipe", this.cooldown);
		this.origin = this.player.getEyeLocation();
		for (double i = -this.arc; i <= this.arc; i += this.arcIncrement) {
			final double angle = Math.toRadians(i);
			final Vector direction = this.player.getEyeLocation().getDirection().clone();

			double x, z, vx, vz;
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			this.streams.put(direction, this.origin);
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		}

		if (!this.charging) {
			if (this.streams.isEmpty()) {
				this.remove();
				return;
			}
			this.advanceSwipe();
		} else {
			if (!this.player.isSneaking()) {
				double factor = 1;
				if (System.currentTimeMillis() >= this.getStartTime() + this.maxChargeTime) {
					factor = this.maxChargeFactor;
				} else {
					factor = this.maxChargeFactor * (System.currentTimeMillis() - this.getStartTime()) / this.maxChargeTime;
				}

				this.charging = false;
				this.launch();
				factor = Math.max(1, factor);
				this.damage *= factor;
				this.pushFactor *= factor;
			} else if (System.currentTimeMillis() >= this.getStartTime() + this.maxChargeTime) {
				playAirbendingParticles(this.player.getEyeLocation(), this.particles);
			}
		}
	}

	@Override
	public String getName() {
		return "AirSwipe";
	}

	@Override
	public Location getLocation() {
		return this.streams.size() != 0 ? this.streams.values().iterator().next() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isCollidable() {
		return this.origin != null;
	}

	@Override
	public double getCollisionRadius() {
		return this.getRadius();
	}

	@Override
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (final Location swipeLoc : this.streams.values()) {
			locations.add(swipeLoc);
		}
		return locations;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public boolean isCharging() {
		return this.charging;
	}

	public void setCharging(final boolean charging) {
		this.charging = charging;
	}

	public int getArc() {
		return this.arc;
	}

	public void setArc(final int arc) {
		this.arc = arc;
	}

	public int getParticles() {
		return this.particles;
	}

	public void setParticles(final int particles) {
		this.particles = particles;
	}

	public static int getMaxAffectableEntities() {
		return MAX_AFFECTABLE_ENTITIES;
	}

	public long getMaxChargeTime() {
		return this.maxChargeTime;
	}

	public void setMaxChargeTime(final long maxChargeTime) {
		this.maxChargeTime = maxChargeTime;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getPushFactor() {
		return this.pushFactor;
	}

	public void setPushFactor(final double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getMaxChargeFactor() {
		return this.maxChargeFactor;
	}

	public void setMaxChargeFactor(final double maxChargeFactor) {
		this.maxChargeFactor = maxChargeFactor;
	}

	public Map<Vector, Location> getElements() {
		return this.streams;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public int getArcIncrement() {
		return this.arcIncrement;
	}

	public void setArcIncrement(final int arcIncrement) {
		this.arcIncrement = arcIncrement;
	}

}
