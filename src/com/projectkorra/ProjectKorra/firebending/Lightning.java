package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingManager;
import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class Lightning {
	public static enum State {
		START, STRIKE, MAINBOLT
	}
	
	public static ArrayList<Lightning> instances = new ArrayList<Lightning>();
	public static boolean SELF_HIT_WATER = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Fire.Lightning.SelfHitWater");
	public static boolean SELF_HIT_CLOSE = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Fire.Lightning.SelfHitClose");
	public static boolean ARC_ON_ICE = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Fire.Lightning.ArcOnIce");
	public static double RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.Range");
	public static double DAMAGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.Damage");
	public static double MAX_ARC_ANGLE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.MaxArcAngle");
	public static double SUB_ARC_CHANCE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.SubArcChance");
	public static double CHAIN_ARC_RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.ChainArcRange");
	public static double CHAIN_ARC_CHANCE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.ChainArcChance");
	public static double WATER_ARC_RANGE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.WaterArcRange");
	public static double STUN_CHANCE = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.StunChance");
	public static double STUN_DURATION = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.StunDuration");
	public static int MAX_CHAIN_ARCS = (int) ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.MaxChainArcs");
	public static int WATER_ARCS = (int) ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.WaterArcs");
	public static long CHARGETIME = (long) ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.ChargeTime");
	public static long COOLDOWN = (long) ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.Lightning.Cooldown");
	
	private static final int POINT_GENERATION = 5; 

	private Player player;
	private BendingPlayer bplayer;
	private Location origin, destination;
	private double range, chargeTime, cooldown, subArcChance, damage, chainArcs, chainRange, waterRange;
	private double chainArcChance, stunChance, stunDuration;
	private long time;
	private boolean charged, hitWater, hitIce;
	private State state = State.START;
	private ArrayList<Entity> affectedEntities = new ArrayList<Entity>();
	private ArrayList<Arc> arcs = new ArrayList<Arc>();
	private ArrayList<BukkitRunnable> tasks = new ArrayList<BukkitRunnable>();

	public Lightning(Player player) {
		this.player = player;
		bplayer = Methods.getBendingPlayer(player.getName());
		charged = false;
		hitWater = false;
		hitIce = false;
		time = System.currentTimeMillis();
		range = Methods.getFirebendingDayAugment(RANGE, player.getWorld());
		subArcChance = Methods.getFirebendingDayAugment(SUB_ARC_CHANCE, player.getWorld());
		damage = Methods.getFirebendingDayAugment(DAMAGE, player.getWorld());
		chainArcs = Methods.getFirebendingDayAugment(MAX_CHAIN_ARCS, player.getWorld());
		chainArcChance = Methods.getFirebendingDayAugment(CHAIN_ARC_CHANCE, player.getWorld());
		chainRange = Methods.getFirebendingDayAugment(CHAIN_ARC_RANGE, player.getWorld());
		waterRange = Methods.getFirebendingDayAugment(WATER_ARC_RANGE, player.getWorld());
		stunChance = Methods.getFirebendingDayAugment(STUN_CHANCE, player.getWorld());
		stunDuration = Methods.getFirebendingDayAugment(STUN_DURATION, player.getWorld());
		chargeTime = CHARGETIME;
		cooldown = COOLDOWN;
		
		if(AvatarState.isAvatarState(player)) {
			/* Some variables aren't considered here because it makes AS too overpowered
			 * and causes crashing.
			 */
			chargeTime = 0;
			cooldown = 0;
			damage = AvatarState.getValue(damage);
			chainArcs = AvatarState.getValue(chainArcs);
			chainArcChance = AvatarState.getValue(chainArcChance);
			chainRange = AvatarState.getValue(chainRange);
			stunChance = AvatarState.getValue(stunChance);
		}
		else if(BendingManager.events.get(player.getWorld()).equalsIgnoreCase("SozinsComet")) {
			chargeTime = 0;
			cooldown = 0;
		}
		instances.add(this);
	}
	
	/** Progresses the instance of this ability by 1 tick.
	 * This is the heart of the ability, it checks if it needs to
	 * remove itself, and handles the initial Lightning Arc generation.
	 * 
	 * Once all of the arcs have been created then this ability instance
	 * gets removed, but the BukkitRunnables continue until they remove
	 * themselves. **/
	private void progress() {
		if (player.isDead() || !player.isOnline()) {
			removeWithTasks();
			return;
		}
		else if (Methods.getBoundAbility(player) == null || !Methods.getBoundAbility(player).equalsIgnoreCase("Lightning")) {
			remove();
			return;
		}
		if(state == State.START) {
			if(bplayer.isOnCooldown("Lightning")) {
				remove();
				return;
			}
			if (System.currentTimeMillis() - time > chargeTime)
				charged = true;
			if(charged) {
				if(player.isSneaking()) {
					Location loc = player.getEyeLocation().add(player.getEyeLocation()
							.getDirection().normalize().multiply(1.2));
					loc.add(0, 0.3, 0);
					Methods.playLightningbendingParticle(loc, 0.2F, 0.2F, 0.2F);
				}
				else {
					state = State.MAINBOLT;
					bplayer.addCooldown("Lightning", (long) cooldown);
					Entity target = Methods.getTargetedEntity(player, range, new ArrayList<Entity>());
					origin = player.getEyeLocation();
					if(target != null)
						destination = target.getLocation();
					else
						destination = player.getEyeLocation().add(
								player.getEyeLocation().getDirection().normalize().multiply(range));
				}
			}
			else if(!player.isSneaking()) {
				remove();
				return;
			}
		}
		else if(state == State.MAINBOLT) {
			Arc mainArc = new Arc(origin, destination);
			mainArc.generatePoints(POINT_GENERATION);
			arcs.add(mainArc);
			ArrayList<Arc> subArcs = mainArc.generateArcs(subArcChance, range / 2.0);
			arcs.addAll(subArcs);
			state = State.STRIKE;
		}
		else if(state == State.STRIKE) {			
			for(int i = 0; i < arcs.size(); i++) {
				Arc arc = arcs.get(i);
				for(int j = 0; j < arc.getAnimLocs().size() - 1; j++) {
					final Location iterLoc = arc.getAnimLocs().get(j).getLoc().clone();
					final Location dest = arc.getAnimLocs().get(j + 1).getLoc().clone();
					if(SELF_HIT_CLOSE 
							&& player.getLocation().distance(iterLoc) < 3  
							&& !isTransparent(player, iterLoc.getBlock())
							&& !affectedEntities.contains(player)) {
								affectedEntities.add(player);
								electrocute(player);
						}
					
					while(iterLoc.distance(dest) > 0.15) {
						BukkitRunnable task = new LightningParticle(arc, iterLoc.clone());
						double timer = arc.getAnimLocs().get(j).getAnimCounter() / 2;
						task.runTaskTimer(ProjectKorra.plugin, (long) timer, 1);
						tasks.add(task);						
						iterLoc.add(Methods.getDirection(iterLoc, dest).normalize().multiply(0.15));
					}
				}
				arcs.remove(i);
				i--;
			}
			if(tasks.size() == 0) {
				remove();
				return;
			}
		}
	}
	
	/** 
	 * Checks if a block is transparent, also considers the ARC_ON_ICE config option.
	 * @param player the player that is viewing the block
	 * @param block the block
	 * @return true if the block is transparent
	 */
	@SuppressWarnings("deprecation")
	public boolean isTransparent(Player player, Block block) {
		if (Arrays.asList(Methods.transparentToEarthbending).contains(block.getTypeId())) {
			if(Methods.isRegionProtectedFromBuild(player, "Lightning", block.getLocation()))
				return false;
			else if(isIce(block.getLocation()))
				return ARC_ON_ICE;
			else
				return true;
		}
		return false;		
	}
	
	/**
	 * Damages an entity, and may cause paralysis depending on the config.
	 * @param lent: the LivingEntity that is being damaged
	 */
	public void electrocute(LivingEntity lent) {
		lent.getWorld().playSound(lent.getLocation(), Sound.CREEPER_HISS, 1, 0);
		player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, 1, 0);
		Methods.damageEntity(player, lent, damage);
		if(Math.random() < stunChance) {
			final Location lentLoc = lent.getLocation();
			final LivingEntity flent = lent;
			new BukkitRunnable() {
				int count = 0;
				public void run() {
					if(flent.isDead() || (flent instanceof Player && !((Player) flent).isOnline())) {
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
					if(count > stunDuration)
						cancel();
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		}
	}

	/** 
	 * Removes the instance of this ability and cancels any current runnables 
	 */
	public void removeWithTasks() {
		for(int i = 0; i < tasks.size(); i++) {
			tasks.get(i).cancel();
			i--;
		}
		remove();
	}
	
	/** 
	 * Removes this ability instance 
	 */	public void remove() {
		instances.remove(this);
	}
	
	/** 
	 * Removes every instance of this ability
	 */
	public static void removeAll() {
		for (int i = 0; i < instances.size(); i++) {
			instances.get(i).remove();
			i--;
		}
	}
	
	/** 
	 * Progresses every instance of this ability by 1 tick 
	 */
	public static void progressAll() {
		for (int i = 0; i < instances.size(); i++)
			instances.get(i).progress();
	}
	
	/**
	 * Returns an instance of this ability if it was initialized by player 
	 * @param player that created the instance
	 * @return the ability
	 */
	public static Lightning getLightning(Player player) {
		for(Lightning light : instances) {
			if(light.player == player)
				return light;
		}
		return null;
	}
	
	/**
	 * Checks if a location contains an ice block
	 * @param loc the location to check
	 * @return true if it is ice
	 */
	public static boolean isIce(Location loc) {
		Material mat = loc.getBlock().getType();
		return mat == Material.ICE || mat == Material.PACKED_ICE;
	}

	/**
	 * Checks if a location contains a water block
	 * @param loc the location to check
	 * @return true if it is water
	 */
	public static boolean isWater(Location loc) {
		Material mat = loc.getBlock().getType();
		return mat == Material.WATER || mat == Material.STATIONARY_WATER;
	}
	
	/**
	 * Checks if a location is ice or water
	 * @param loc the location to check
	 * @return true if it is water or ice
	 */
	public static boolean isWaterOrIce(Location loc) {
		return isIce(loc) || isWater(loc);
	}
	
	/** An Arc represents a Lightning arc for the specific ability.
	 * These Arcs contain a list of Particles that are used to display
	 * the entire arc. Arcs can also generate a list of subarcs that
	 * chain off of their own instance.**/
	public class Arc {
		private ArrayList<Location> points;
		private ArrayList<AnimLocation> animLocs;
		private ArrayList<LightningParticle> particles;
		private ArrayList<Arc> subArcs;
		private Vector direction;
		private int animCounter;
		
		public Arc(Location startPoint, Location endPoint) {
			points = new ArrayList<Location>();
			points.add(startPoint.clone());
			points.add(endPoint.clone());
			direction = Methods.getDirection(startPoint, endPoint);
			particles = new ArrayList<LightningParticle>();
			subArcs = new ArrayList<Arc>();
			animLocs = new ArrayList<AnimLocation>();
			animCounter = 0;
		}
		
		/** Runs an arc generation algorithm by first creating two points, 
		 * the starting point and the ending point. Next, it creates a point
		 * in the middle that has an offset relative to the beginning and
		 * end points. Now that the arc is split into 3 points, we continue this
		 * processes by generating middle points in the two halfs of this arc.
		 * This process continues based on @times
		 * 
		 * @param times: The amount of times that the arc will be split in half
		 * @param times causes O(n^2) complexity
		 * **/
		public void generatePoints(int times) {
			for(int i = 0; i < times; i++) {
				for(int j = 0; j < points.size() - 1; j += 2) {
					Location loc1 = points.get(j);
					Location loc2 = points.get(j + 1);
					double adjac = loc1.distance(loc2) / 2;
					double angle = (Math.random() - 0.5) * MAX_ARC_ANGLE;
					angle += angle >= 0 ? 10 : -10;
					double radians = Math.toRadians(angle);
					double hypot = adjac / Math.cos(radians);
					Vector dir = Methods.rotateXZ(direction.clone(), angle);
					Location newLoc = loc1.clone().add(dir.normalize().multiply(hypot));
					newLoc.add(0, (Math.random() - 0.5) / 2.0, 0);
					points.add(j + 1, newLoc);
				}
			}
			for(int i = 0; i < points.size(); i++) {
				animLocs.add(new AnimLocation(points.get(i), animCounter));
				animCounter++;
			}
		}
		
		/** Randomly generates subarcs off of this arc.
		 * 	@param chance - The chance that an arc will be generated
		 * 	for each specific point in the arc.
		 * 	Note: if you generate a lot of points then chance will need to be lowered.
		 *  @param range: The length of each subarc.
		 *  **/
		public ArrayList<Arc> generateArcs(double chance, double range) {
			ArrayList<Arc> arcs = new ArrayList<Arc>();
			for(int i = 0; i < animLocs.size(); i++) {
				if(Math.random() < chance) {
					Location loc = animLocs.get(i).getLoc();
					double angle = (Math.random() - 0.5) * MAX_ARC_ANGLE * 2;
					Vector dir = Methods.rotateXZ(direction.clone(), angle);
					double randRange = (Math.random() * range) + (range / 3.0);
					Location loc2 = loc.clone().add(dir.normalize().multiply(randRange));
					Arc arc = new Arc(loc, loc2);
					subArcs.add(arc);
					arc.setAnimCounter(animLocs.get(i).getAnimCounter());
					arc.generatePoints(POINT_GENERATION);
					arcs.add(arc);
					arcs.addAll(arc.generateArcs(chance / 2.0, range / 2.0));
				}
			}
			return arcs;
		}
		
		/** 
		 * Stops this Arc from further animating or doing damage.
		 */
		public void cancel() {
			for(int i = 0; i < particles.size(); i++) {
				particles.get(i).cancel();
			}
			
			for(Arc subArc : subArcs) {
				subArc.cancel();
			}
		}
		
		public ArrayList<Location> getPoints() {
			return points;
		}
		
		public void setPoints(ArrayList<Location> points) {
			this.points = points;
		}

		public Vector getDirection() {
			return direction;
		}

		public void setDirection(Vector direction) {
			this.direction = direction;
		}

		public int getAnimCounter() {
			return animCounter;
		}

		public void setAnimCounter(int animCounter) {
			this.animCounter = animCounter;
		}

		public ArrayList<AnimLocation> getAnimLocs() {
			return animLocs;
		}

		public void setAnimLocs(ArrayList<AnimLocation> animLocs) {
			this.animLocs = animLocs;
		}

		public ArrayList<LightningParticle> getParticles() {
			return particles;
		}

		public void setParticles(ArrayList<LightningParticle> particles) {
			this.particles = particles;
		}
		
	}
	
	/** Represents a Lightning Arc Point particle animation.
	 * 	This basically just holds a location and counts the
	 * 	amount of times that a particle has been animated. 
	 * **/
	public class AnimLocation {
		private Location loc;
		private int animCounter;
		
		public AnimLocation(Location loc, int animCounter) {
			this.loc = loc;
			this.animCounter = animCounter;
		}

		public Location getLoc() {
			return loc;
		}

		public void setLoc(Location loc) {
			this.loc = loc;
		}

		public int getAnimCounter() {
			return animCounter;
		}

		public void setAnimCounter(int animCounter) {
			this.animCounter = animCounter;
		}
	}		
	
	/** A Runnable Particle that continuously displays itself
	 * until it reaches a certain time limit.
	 * 
	 * These LightningParticles do the actual checking for player collision
	 * and handle damaging any entities. These Runnables also
	 * check to see if they reach water, in which case they will generate
	 * subarcs to branch out.
	 * **/
	public class LightningParticle extends BukkitRunnable {
		private Arc arc;
		private Location loc;
		private int count = 0;
		
		public LightningParticle(Arc arc, Location loc) {
			this.arc = arc;
			this.loc = loc;
			arc.particles.add(this);
		}
		
		/** Cancels this Runnable**/
		public void cancel() {
			super.cancel();
			tasks.remove(this);
		}
		
		/** 
		 * Animates the Location, checks for water/player collision
		 * and also deals with any chain subarcs.
		 */
		public void run() {
			Methods.playLightningbendingParticle(loc, 0F, 0F, 0F);
			count++;
			if(count > 5)
				this.cancel();
			else if(count == 1) {
				if(!isTransparent(player, loc.getBlock())) {
					arc.cancel();
					return;
				}
					
				// Handle Water electrocution
				if(!hitWater && (isWater(loc) || (ARC_ON_ICE && isIce(loc)))) {
					hitWater = true;
					if(isIce(loc))
						hitIce = true;
					for(int i = 0; i < WATER_ARCS; i++) {
						Location origin = loc.clone();
						origin.add(new Vector((Math.random() - 0.5) * 2, 0, (Math.random() - 0.5) * 2));
						destination = origin.clone().add(
								new Vector((Math.random() - 0.5) * waterRange, Math.random() - 0.7, (Math.random() - 0.5) * waterRange));
						Arc newArc = new Arc(origin, destination);
						newArc.generatePoints(POINT_GENERATION);
						arcs.add(newArc);
					}
				}
				
				for(Entity entity : Methods.getEntitiesAroundPoint(loc, 2.5)) {
					/*
					 * If the player is in water we will electrocute them only if they are standing in water.
					 * If the lightning hit ice we can electrocute them all the time.
					 */
					if(entity.equals(player) 
							&& !(SELF_HIT_WATER && hitWater && isWater(player.getLocation()))
							&& !(SELF_HIT_WATER && hitIce))
						continue;
					
					if(entity instanceof LivingEntity && !affectedEntities.contains(entity)) {
						affectedEntities.add(entity);
						LivingEntity lent = (LivingEntity) entity;				
						if(lent instanceof Player) {
							lent.getWorld().playSound(lent.getLocation(), Sound.CREEPER_HISS, 1, 0);
							player.getWorld().playSound(player.getLocation(), Sound.CREEPER_HISS, 1, 0);
							Player p = (Player) lent;
							Lightning light = getLightning(p);
							if(light != null && light.state == State.START) {
								light.charged = true;
								remove();
								return;
							}
						}
						electrocute(lent);
						
						// Handle Chain Lightning
						if(chainArcs >= 1 && Math.random() <= chainArcChance) {
							chainArcs--;
							for(Entity ent : Methods.getEntitiesAroundPoint(lent.getLocation(), chainRange)) {
								if(!ent.equals(player) && !ent.equals(lent) && ent instanceof LivingEntity && !affectedEntities.contains(ent)) {
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
	}

	/** Below are all of the accessor/mutator methods **/
	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
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

	public double getCooldown() {
		return cooldown;
	}

	public void setCooldown(double cooldown) {
		this.cooldown = cooldown;
		if(player != null)
			bplayer.addCooldown("Lightning", (long) cooldown);
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

	public double getChainArcs() {
		return chainArcs;
	}

	public void setChainArcs(double chainArcs) {
		this.chainArcs = chainArcs;
	}

	public double getChainRange() {
		return chainRange;
	}

	public void setChainRange(double chainRange) {
		this.chainRange = chainRange;
	}

	public double getWaterRange() {
		return waterRange;
	}

	public void setWaterRange(double waterRange) {
		this.waterRange = waterRange;
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
	
	public ArrayList<Arc> getArcs() {
		return this.arcs;
	}
	
	public static ArrayList<Arc> getAllArcs() {
		ArrayList<Arc> a = new ArrayList<Arc>();
		for(Lightning l : Lightning.instances) {
			for(Arc arcs : l.getArcs()) {
				a.add(arcs);
			}
		}
		return a;
	}
}