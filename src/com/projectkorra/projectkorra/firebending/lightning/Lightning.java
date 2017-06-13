package com.projectkorra.projectkorra.firebending.lightning;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.LightningAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

public class Lightning extends LightningAbility {

	public static enum State {
		START, STRIKE, MAINBOLT
	}

	private static final int POINT_GENERATION = 5;

	private boolean charged;
	private boolean hitWater;
	private boolean hitIce;
	private boolean selfHitWater;
	private boolean selfHitClose;
	private boolean arcOnIce;
	private int waterArcs;
	private double range;
	private double chargeTime;
	private double subArcChance;
	private double damage;
	private double maxChainArcs;
	private double chainRange;
	private double waterArcRange;
	private double chainArcChance;
	private double stunChance;
	private double stunDuration;
	private double maxArcAngle;
	private double particleRotation;
	private long time;
	private long cooldown;
	private State state;
	private Location origin;
	private Location destination;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<Arc> arcs;
	private ArrayList<BukkitRunnable> tasks;
	private ArrayList<Location> locations;

	public Lightning(Player player) {
		super(player);

		if (!bPlayer.canBend(this)) {
			return;
		}
		if (hasAbility(player, Lightning.class)) {
			if (!getAbility(player, Lightning.class).isCharged()) {
				return;
			}
		}

		this.charged = false;
		this.hitWater = false;
		this.hitIce = false;
		this.time = System.currentTimeMillis();
		this.state = State.START;
		this.affectedEntities = new ArrayList<>();
		this.arcs = new ArrayList<>();
		this.tasks = new ArrayList<>();
		this.locations = new ArrayList<>();

		this.selfHitWater = getConfig().getBoolean("Abilities.Fire.Lightning.SelfHitWater");
		this.selfHitClose = getConfig().getBoolean("Abilities.Fire.Lightning.SelfHitClose");
		this.arcOnIce = getConfig().getBoolean("Abilities.Fire.Lightning.ArcOnIce");
		this.range = getConfig().getDouble("Abilities.Fire.Lightning.Range");
		this.damage = getConfig().getDouble("Abilities.Fire.Lightning.Damage");
		this.maxArcAngle = getConfig().getDouble("Abilities.Fire.Lightning.MaxArcAngle");
		this.subArcChance = getConfig().getDouble("Abilities.Fire.Lightning.SubArcChance");
		this.chainRange = getConfig().getDouble("Abilities.Fire.Lightning.ChainArcRange");
		this.chainArcChance = getConfig().getDouble("Abilities.Fire.Lightning.ChainArcChance");
		this.waterArcRange = getConfig().getDouble("Abilities.Fire.Lightning.WaterArcRange");
		this.stunChance = getConfig().getDouble("Abilities.Fire.Lightning.StunChance");
		this.stunDuration = getConfig().getDouble("Abilities.Fire.Lightning.StunDuration");
		this.maxChainArcs = getConfig().getInt("Abilities.Fire.Lightning.MaxChainArcs");
		this.waterArcs = getConfig().getInt("Abilities.Fire.Lightning.WaterArcs");
		this.chargeTime = getConfig().getLong("Abilities.Fire.Lightning.ChargeTime");
		this.cooldown = getConfig().getLong("Abilities.Fire.Lightning.Cooldown");

		this.range = getDayFactor(this.range);
		this.subArcChance = getDayFactor(this.subArcChance);
		this.damage = getDayFactor(this.damage);
		this.maxChainArcs = getDayFactor(this.maxChainArcs);
		this.chainArcChance = getDayFactor(this.chainArcChance);
		this.chainRange = getDayFactor(this.chainRange);
		this.waterArcRange = getDayFactor(this.waterArcRange);
		this.stunChance = getDayFactor(this.stunChance);
		this.stunDuration = getDayFactor(this.stunDuration);

		if (bPlayer.isAvatarState()) {
			this.chargeTime = getConfig().getLong("Abilities.Avatar.AvatarState.Fire.Lightning.ChargeTime");
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Fire.Lightning.Cooldown");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.Lightning.Damage");

		} else if (isSozinsComet(player.getWorld())) {
			this.chargeTime = 0;
			this.cooldown = 0;
		}
		start();
	}

	/**
	 * Damages an entity, and may cause paralysis depending on the config.
	 * 
	 * @param lent The LivingEntity that is being damaged
	 */
	public void electrocute(LivingEntity lent) {
		lent.getWorld().playSound(lent.getLocation(), Sound.ENTITY_CREEPER_HURT, 1, 0.01F);
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_HURT, 1, 0.01F);
		DamageHandler.damageEntity(lent, damage, this);

		if (Math.random() < stunChance) {
			final Location lentLoc = lent.getLocation();
			final LivingEntity flent = lent;

			new BukkitRunnable() {
				int count = 0;

				@Override
				public void run() {
					if (flent.isDead() || (flent instanceof Player && !((Player) flent).isOnline())) {
						cancel();
						return;
					}

					Location tempLoc = lentLoc.clone();
					Vector tempVel = flent.getVelocity();
					tempVel.setY(Math.min(0, tempVel.getY()));
					tempLoc.setY(flent.getLocation().getY());
					flent.teleport(tempLoc);
					flent.setVelocity(tempVel);
					count++;
					if (count > stunDuration) {
						cancel();
					}
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		}
	}

	/**
	 * Checks if a block is transparent, also considers the ARC_ON_ICE config
	 * option.
	 * 
	 * @param player the player that is viewing the block
	 * @param block the block
	 * @return true if the block is transparent
	 */
	private boolean isTransparentForLightning(Player player, Block block) {
		if (isTransparent(block)) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
				return false;
			} else if (isIce(block)) {
				return arcOnIce;
			} else {
				return true;
			}
		}
		return false;
	}

	/**
	 * Progresses the instance of this ability by 1 tick. This is the heart of
	 * the ability, it checks if it needs to remove itself, and handles the
	 * initial Lightning Arc generation.
	 * 
	 * Once all of the arcs have been created then this ability instance gets
	 * removed, but the BukkitRunnables continue until they remove themselves.
	 **/
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			removeWithTasks();
			return;
		} else if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		locations.clear();

		if (state == State.START) {
			if (bPlayer.isOnCooldown(this)) {
				remove();
				return;
			} else if (System.currentTimeMillis() - time > chargeTime) {
				charged = true;
			}

			if (charged) {
				if (player.isSneaking()) {
					Location loc = player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(1.2));
					loc.add(0, 0.3, 0);
					playLightningbendingParticle(loc, 0.2F, 0.2F, 0.2F);
				} else {
					state = State.MAINBOLT;
					bPlayer.addCooldown(this);
					Entity target = GeneralMethods.getTargetedEntity(player, range);
					origin = player.getEyeLocation();

					if (target != null) {
						destination = target.getLocation();
					} else {
						destination = player.getEyeLocation().add(player.getEyeLocation().getDirection().normalize().multiply(range));
					}
				}
			} else {
				if (!player.isSneaking()) {
					remove();
					return;
				}

				Location localLocation1 = player.getLocation();
				double d1 = 0.1570796326794897D;
				double d2 = 0.06283185307179587D;
				double d3 = 1.0D;
				double d4 = 1.0D;
				double d5 = d1 * particleRotation;
				double d6 = d2 * particleRotation;
				double d7 = localLocation1.getX() + d4 * Math.cos(d5);
				double d8 = localLocation1.getZ() + d4 * Math.sin(d5);
				double newY = (localLocation1.getY() + 1.0D + d4 * Math.cos(d6));
				Location localLocation2 = new Location(player.getWorld(), d7, newY, d8);
				playLightningbendingParticle(localLocation2);
				particleRotation += 1.0D / d3;
			}

		} else if (state == State.MAINBOLT) {
			Arc mainArc = new Arc(origin, destination);
			mainArc.generatePoints(POINT_GENERATION);
			arcs.add(mainArc);
			ArrayList<Arc> subArcs = mainArc.generateArcs(subArcChance, range / 2.0, maxArcAngle);
			arcs.addAll(subArcs);
			state = State.STRIKE;
		} else if (state == State.STRIKE) {
			for (int i = 0; i < arcs.size(); i++) {
				Arc arc = arcs.get(i);
				for (int j = 0; j < arc.getAnimationLocations().size() - 1; j++) {
					final Location iterLoc = arc.getAnimationLocations().get(j).getLocation().clone();
					final Location dest = arc.getAnimationLocations().get(j + 1).getLocation().clone();
					if (selfHitClose && player.getLocation().distanceSquared(iterLoc) < 9 && !isTransparentForLightning(player, iterLoc.getBlock()) && !affectedEntities.contains(player)) {
						affectedEntities.add(player);
						electrocute(player);
					}

					while (iterLoc.distanceSquared(dest) > 0.15 * 0.15) {
						BukkitRunnable task = new LightningParticle(arc, iterLoc.clone(), selfHitWater, waterArcs);
						double timer = arc.getAnimationLocations().get(j).getAnimCounter() / 2;
						task.runTaskTimer(ProjectKorra.plugin, (long) timer, 1);
						tasks.add(task);
						iterLoc.add(GeneralMethods.getDirection(iterLoc, dest).normalize().multiply(0.15));
					}
				}
				arcs.remove(i);
				i--;
			}
			if (tasks.size() == 0) {
				remove();
				return;
			}
		}
	}

	/**
	 * Removes the instance of this ability and cancels any current runnables
	 */
	public void removeWithTasks() {
		for (int i = 0; i < tasks.size(); i++) {
			tasks.get(i).cancel();
			i--;
		}
		remove();
	}

	/**
	 * Represents a Lightning Arc Point particle animation. This basically just
	 * holds a location and counts the amount of times that a particle has been
	 * animated.
	 **/
	public class AnimationLocation {
		private Location location;
		private int animationCounter;

		public AnimationLocation(Location loc, int animationCounter) {
			this.location = loc;
			this.animationCounter = animationCounter;
		}

		public int getAnimCounter() {
			return animationCounter;
		}

		public Location getLocation() {
			return location;
		}

		public void setAnimationCounter(int animationCounter) {
			this.animationCounter = animationCounter;
		}

		public void setLocation(Location location) {
			this.location = location;
		}
	}

	/**
	 * An Arc represents a Lightning arc for the specific ability. These Arcs
	 * contain a list of Particles that are used to display the entire arc. Arcs
	 * can also generate a list of subarcs that chain off of their own instance.
	 **/
	public class Arc {
		private int animationCounter;
		private Vector direction;
		private ArrayList<Location> points;
		private ArrayList<AnimationLocation> animationLocations;
		private ArrayList<LightningParticle> particles;
		private ArrayList<Arc> subArcs;

		public Arc(Location startPoint, Location endPoint) {
			points = new ArrayList<>();
			points.add(startPoint.clone());
			points.add(endPoint.clone());
			direction = GeneralMethods.getDirection(startPoint, endPoint);
			particles = new ArrayList<>();
			subArcs = new ArrayList<>();
			animationLocations = new ArrayList<>();
			animationCounter = 0;
		}

		/**
		 * Stops this Arc from further animating or doing damage.
		 */
		public void cancel() {
			for (int i = 0; i < particles.size(); i++) {
				particles.get(i).cancel();
			}

			for (Arc subArc : subArcs) {
				subArc.cancel();
			}
		}

		/**
		 * Randomly generates subarcs off of this arc.
		 * 
		 * @param chance The chance that an arc will be generated for each
		 *            specific point in the arc. Note: if you generate a lot of
		 *            points then chance will need to be lowered.
		 * @param range The length of each subarc.
		 * 
		 **/
		public ArrayList<Arc> generateArcs(double chance, double range, double maxArcAngle) {
			ArrayList<Arc> arcs = new ArrayList<>();

			for (int i = 0; i < animationLocations.size(); i++) {
				if (Math.random() < chance) {
					Location loc = animationLocations.get(i).getLocation();
					double angle = (Math.random() - 0.5) * maxArcAngle * 2;
					Vector dir = GeneralMethods.rotateXZ(direction.clone(), angle);
					double randRange = (Math.random() * range) + (range / 3.0);

					Location loc2 = loc.clone().add(dir.normalize().multiply(randRange));
					Arc arc = new Arc(loc, loc2);

					subArcs.add(arc);
					arc.setAnimationCounter(animationLocations.get(i).getAnimCounter());
					arc.generatePoints(POINT_GENERATION);
					arcs.add(arc);
					arcs.addAll(arc.generateArcs(chance / 2.0, range / 2.0, maxArcAngle));
				}
			}
			return arcs;
		}

		/**
		 * Runs an arc generation algorithm by first creating two points, the
		 * starting point and the ending point. Next, it creates a point in the
		 * middle that has an offset relative to the beginning and end points.
		 * Now that the arc is split into 3 points, we continue this processes
		 * by generating middle points in the two halfs of this arc. This
		 * process continues the amount of times specified.
		 * 
		 * @param times The amount of times that the arc will be split in half
		 *            causes O(n^2) complexity
		 **/
		public void generatePoints(int times) {
			for (int i = 0; i < times; i++) {
				for (int j = 0; j < points.size() - 1; j += 2) {
					Location loc1 = points.get(j);
					Location loc2 = points.get(j + 1);
					double adjac = 0;
					if (loc1.getWorld().equals(loc2.getWorld())) {
						adjac = loc1.distance(loc2) / 2;
					}

					double angle = (Math.random() - 0.5) * maxArcAngle;

					angle += angle >= 0 ? 10 : -10;

					double radians = Math.toRadians(angle);
					double hypot = adjac / Math.cos(radians);
					Vector dir = GeneralMethods.rotateXZ(direction.clone(), angle);
					Location newLoc = loc1.clone().add(dir.normalize().multiply(hypot));

					newLoc.add(0, (Math.random() - 0.5) / 2.0, 0);
					points.add(j + 1, newLoc);
				}
			}
			for (int i = 0; i < points.size(); i++) {
				animationLocations.add(new AnimationLocation(points.get(i), animationCounter));
				animationCounter++;
			}
		}

		public int getAnimationCounter() {
			return animationCounter;
		}

		public void setAnimationCounter(int animationCounter) {
			this.animationCounter = animationCounter;
		}

		public Vector getDirection() {
			return direction;
		}

		public void setDirection(Vector direction) {
			this.direction = direction;
		}

		public ArrayList<Location> getPoints() {
			return points;
		}

		public ArrayList<AnimationLocation> getAnimationLocations() {
			return animationLocations;
		}

		public ArrayList<LightningParticle> getParticles() {
			return particles;
		}

		public ArrayList<Arc> getSubArcs() {
			return subArcs;
		}

	}

	/**
	 * A Runnable Particle that continuously displays itself until it reaches a
	 * certain time limit.
	 * 
	 * These LightningParticles do the actual checking for player collision and
	 * handle damaging any entities. These Runnables also check to see if they
	 * reach water, in which case they will generate subarcs to branch out.
	 **/
	public class LightningParticle extends BukkitRunnable {
		private boolean selfHitWater;
		private int count = 0;
		private int waterArcs;
		private Arc arc;
		private Location location;

		public LightningParticle(Arc arc, Location location, boolean selfHitWater, int waterArcs) {
			this.arc = arc;
			this.location = location;
			this.selfHitWater = selfHitWater;
			this.waterArcs = waterArcs;
			arc.particles.add(this);
		}

		/** Cancels this Runnable **/
		@Override
		public void cancel() {
			super.cancel();
			tasks.remove(this);
		}

		/**
		 * Animates the Location, checks for water/player collision and also
		 * deals with any chain subarcs.
		 */
		@Override
		public void run() {
			playLightningbendingParticle(location, 0F, 0F, 0F);
			count++;
			if (count > 5) {
				this.cancel();
			} else if (count == 1) {
				if (!isTransparentForLightning(player, location.getBlock())) {
					arc.cancel();
					return;
				}
				Block block = location.getBlock();
				// We only want to consider this particle as part of the location
				// on the its first tick, when it actually does the electrocution.
				// The later ticks are just for visual purposes.
				locations.add(block.getLocation());

				// Handle Water electrocution
				if (!hitWater && (isWater(block) || (arcOnIce && isIce(block)))) {
					hitWater = true;
					if (isIce(block)) {
						hitIce = true;
					}

					for (int i = 0; i < waterArcs; i++) {
						Location origin = location.clone();
						origin.add(new Vector((Math.random() - 0.5) * 2, 0, (Math.random() - 0.5) * 2));
						destination = origin.clone().add(new Vector((Math.random() - 0.5) * waterArcRange, Math.random() - 0.7, (Math.random() - 0.5) * waterArcRange));
						Arc newArc = new Arc(origin, destination);
						newArc.generatePoints(POINT_GENERATION);
						arcs.add(newArc);
					}
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.5)) {
					/*
					 * If the player is in water we will electrocute them only
					 * if they are standing in water. If the lightning hit ice
					 * we can electrocute them all the time.
					 */
					if (entity.equals(player) && !(selfHitWater && hitWater && isWater(player.getLocation().getBlock())) && !(selfHitWater && hitIce)) {
						continue;
					}

					if (entity instanceof LivingEntity && !affectedEntities.contains(entity)) {
						affectedEntities.add(entity);
						LivingEntity lent = (LivingEntity) entity;
						if (lent instanceof Player) {
							lent.getWorld().playSound(lent.getLocation(), Sound.ENTITY_CREEPER_HURT, 1, 0.01F);
							player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_HURT, 1, 0.01F);
							Player p = (Player) lent;
							Lightning light = getAbility(p, Lightning.class);
							if (light != null && light.state == State.START) {
								light.charged = true;
								remove();
								return;
							}
						}

						electrocute(lent);

						// Handle Chain Lightning
						if (maxChainArcs >= 1 && Math.random() <= chainArcChance) {
							maxChainArcs--;
							for (Entity ent : GeneralMethods.getEntitiesAroundPoint(lent.getLocation(), chainRange)) {
								if (!ent.equals(player) && !ent.equals(lent) && ent instanceof LivingEntity && !affectedEntities.contains(ent)) {
									origin = lent.getLocation().add(0, 1, 0);
									destination = ent.getLocation().add(0, 1, 0);
									Arc newArc = new Arc(origin, destination);
									newArc.generatePoints(POINT_GENERATION);
									arcs.add(newArc);
									cancel();
									return;
								}
							}
						}
					}
				}
			}
		}

		public boolean isSelfHitWater() {
			return selfHitWater;
		}

		public void setSelfHitWater(boolean selfHitWater) {
			this.selfHitWater = selfHitWater;
		}

		public int getCount() {
			return count;
		}

		public void setCount(int count) {
			this.count = count;
		}

		public int getWaterArcs() {
			return waterArcs;
		}

		public void setWaterArcs(int waterArcs) {
			this.waterArcs = waterArcs;
		}

		public Arc getArc() {
			return arc;
		}

		public void setArc(Arc arc) {
			this.arc = arc;
		}

		public Location getLocation() {
			return location;
		}

		public void setLocation(Location location) {
			this.location = location;
		}
	}

	@Override
	public String getName() {
		return "Lightning";
	}

	@Override
	public Location getLocation() {
		return origin;
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

	@Override
	public boolean isCollidable() {
		return arcs.size() > 0;
	}

	@Override
	public List<Location> getLocations() {
		return locations;
	}

	public boolean isCharged() {
		return charged;
	}

	public void setCharged(boolean charged) {
		this.charged = charged;
	}

	public boolean isHitWater() {
		return hitWater;
	}

	public void setHitWater(boolean hitWater) {
		this.hitWater = hitWater;
	}

	public boolean isHitIce() {
		return hitIce;
	}

	public void setHitIce(boolean hitIce) {
		this.hitIce = hitIce;
	}

	public boolean isSelfHitWater() {
		return selfHitWater;
	}

	public void setSelfHitWater(boolean selfHitWater) {
		this.selfHitWater = selfHitWater;
	}

	public boolean isSelfHitClose() {
		return selfHitClose;
	}

	public void setSelfHitClose(boolean selfHitClose) {
		this.selfHitClose = selfHitClose;
	}

	public boolean isArcOnIce() {
		return arcOnIce;
	}

	public void setArcOnIce(boolean arcOnIce) {
		this.arcOnIce = arcOnIce;
	}

	public int getWaterArcs() {
		return waterArcs;
	}

	public void setWaterArcs(int waterArcs) {
		this.waterArcs = waterArcs;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(double chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getSubArcChance() {
		return subArcChance;
	}

	public void setSubArcChance(double subArcChance) {
		this.subArcChance = subArcChance;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getMaxChainArcs() {
		return maxChainArcs;
	}

	public void setMaxChainArcs(double maxChainArcs) {
		this.maxChainArcs = maxChainArcs;
	}

	public double getChainRange() {
		return chainRange;
	}

	public void setChainRange(double chainRange) {
		this.chainRange = chainRange;
	}

	public double getWaterArcRange() {
		return waterArcRange;
	}

	public void setWaterArcRange(double waterArcRange) {
		this.waterArcRange = waterArcRange;
	}

	public double getChainArcChance() {
		return chainArcChance;
	}

	public void setChainArcChance(double chainArcChance) {
		this.chainArcChance = chainArcChance;
	}

	public double getStunChance() {
		return stunChance;
	}

	public void setStunChance(double stunChance) {
		this.stunChance = stunChance;
	}

	public double getStunDuration() {
		return stunDuration;
	}

	public void setStunDuration(double stunDuration) {
		this.stunDuration = stunDuration;
	}

	public double getMaxArcAngle() {
		return maxArcAngle;
	}

	public void setMaxArcAngle(double maxArcAngle) {
		this.maxArcAngle = maxArcAngle;
	}

	public double getParticleRotation() {
		return particleRotation;
	}

	public void setParticleRotation(double particleRotation) {
		this.particleRotation = particleRotation;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Location getDestination() {
		return destination;
	}

	public void setDestination(Location destination) {
		this.destination = destination;
	}

	public static int getPointGeneration() {
		return POINT_GENERATION;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}

	public ArrayList<Arc> getArcs() {
		return arcs;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
