package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

/**
 * Suffocate
 * 
 * Suffocate is an air ability that causes entities to be surrounded by a sphere
 * air that causes constant damage after a configurable delay. Suffocate also
 * causes Blinding and Slowing affects to entities depending on how the ability
 * is configured. While in AvatarState this ability can be used on multiple
 * entities within a large radius. If the user is damaged while performing this
 * ability then the ability is removed.
 */
public class Suffocate {
	public static enum SpiralType {
		HORIZONTAL1, HORIZONTAL2, VERTICAL1, VERTICAL2, DIAGONAL1, DIAGONAL2
	};

	private static ConcurrentHashMap<Player, Suffocate> instances = new ConcurrentHashMap<Player, Suffocate>();

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();
	private static final boolean CAN_SUFFOCATE_UNDEAD = config.getBoolean("Abilities.Air.Suffocate.CanBeUsedOnUndeadMobs");
	private static final boolean REQUIRE_CONSTANT_AIM = config.getBoolean("Abilities.Air.Suffocate.RequireConstantAim");
	private static final double ANIM_RADIUS = config.getDouble("Abilities.Air.Suffocate.AnimationRadius");
	private static final int ANIM_PARTICLE_AMOUNT = config.getInt("Abilities.Air.Suffocate.AnimationParticleAmount");
	private static final double ANIM_SPEED = config.getDouble("Abilities.Air.Suffocate.AnimationSpeed");

	private static final long CHARGE_TIME = config.getLong("Abilities.Air.Suffocate.ChargeTime");
	private static final long COOLDOWN = config.getLong("Abilities.Air.Suffocate.Cooldown");
	private static final double RANGE = config.getDouble("Abilities.Air.Suffocate.Range");
	private static double AIM_RADIUS = config.getDouble("Abilities.Air.Suffocate.RequireConstantAimRadius");
	private static double DAMAGE = config.getDouble("Abilities.Air.Suffocate.Damage");
	private static double DAMAGE_INITIAL_DELAY = config.getDouble("Abilities.Air.Suffocate.DamageInitialDelay");
	private static double DAMAGE_INTERVAL = config.getDouble("Abilities.Air.Suffocate.DamageInterval");
	private static int SLOW = config.getInt("Abilities.Air.Suffocate.SlowPotency");
	private static double SLOW_INTERVAL = config.getDouble("Abilities.Air.Suffocate.SlowInterval");
	private static double SLOW_DELAY = config.getDouble("Abilities.Air.Suffocate.SlowDelay");
	private static int BLIND = config.getInt("Abilities.Air.Suffocate.BlindPotentcy");
	private static double BLIND_DELAY = config.getDouble("Abilities.Air.Suffocate.BlindDelay");
	private static double BLIND_INTERVAL = config.getDouble("Abilities.Air.Suffocate.BlindInterval");

	private Player player;
	private BendingPlayer bplayer;
	private boolean started = false;
	private long time;
	private ArrayList<BukkitRunnable> tasks;
	private ArrayList<LivingEntity> targets;
	private boolean reqConstantAim;
	private boolean canSuffUndead;
	private long chargeTime, cooldown;
	private int particleScale;
	private double range, radius;
	private double speedFactor;
	private double aimRadius;
	private double damage, damageDelay, damageRepeat;
	private double slow, slowRepeat, slowDelay;
	private double blind, blindDelay, blindRepeat;

	public Suffocate(Player player) {
		this.player = player;
		bplayer = GeneralMethods.getBendingPlayer(player.getName());
		targets = new ArrayList<LivingEntity>();
		tasks = new ArrayList<BukkitRunnable>();
		time = System.currentTimeMillis();

		reqConstantAim = REQUIRE_CONSTANT_AIM;
		canSuffUndead = CAN_SUFFOCATE_UNDEAD;
		chargeTime = CHARGE_TIME;
		cooldown = COOLDOWN;
		particleScale = ANIM_PARTICLE_AMOUNT;
		range = RANGE;
		radius = ANIM_RADIUS;
		speedFactor = ANIM_SPEED;
		aimRadius = AIM_RADIUS;
		damage = DAMAGE;
		damageDelay = DAMAGE_INITIAL_DELAY;
		damageRepeat = DAMAGE_INTERVAL;
		slow = SLOW;
		slowRepeat = SLOW_INTERVAL;
		slowDelay = SLOW_DELAY;
		blind = BLIND;
		blindDelay = BLIND_DELAY;
		blindRepeat = BLIND_INTERVAL;

		if (instances.containsKey(player)) return;

		if (AvatarState.isAvatarState(player)) {
			cooldown = 0;
			chargeTime = 0;
			reqConstantAim = false;
			damage = AvatarState.getValue(damage);
			range *= 2;
			slow = AvatarState.getValue(slow);
			slowRepeat = AvatarState.getValue(slowRepeat);
			blind = AvatarState.getValue(blind);
			blindRepeat = AvatarState.getValue(blindRepeat);
		}
		if (particleScale < 1) particleScale = 1;
		else if (particleScale > 2) particleScale = 2;

		if (AvatarState.isAvatarState(player)) {
			for (Entity ent : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range))
				if (ent instanceof LivingEntity && !ent.equals(player)) targets.add((LivingEntity) ent);
		} else {
			Entity ent = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
			if (ent != null && ent instanceof LivingEntity) targets.add((LivingEntity) ent);
		}

		if (!canSuffUndead) {
			for (int i = 0; i < targets.size(); i++) {
				LivingEntity target = targets.get(i);
				if (GeneralMethods.isUndead(target)) {
					targets.remove(i);
					i--;
				}
			}
		}

		if (targets.size() == 0) return;
		else if (bplayer.isOnCooldown("suffocate")) return;
		bplayer.addCooldown("suffocate", cooldown);
		instances.put(player, this);
	}

	/** Progresses this instance of Suffocate by 1 tick. **/
	public void progress() {
		if (targets.size() == 0) {
			remove();
			return;
		}
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		String ability = GeneralMethods.getBoundAbility(player);
		if (ability == null || !ability.equalsIgnoreCase("Suffocate") || !GeneralMethods.canBend(player.getName(), "Suffocate")) {
			remove();
			return;
		}

		for (int i = 0; i < targets.size(); i++) {
			LivingEntity target = targets.get(i);
			if (target.isDead() || !target.getWorld().equals(player.getWorld()) || target.getLocation().distance(player.getEyeLocation()) > range) {
				breakSuffocateLocal(target);
				i--;
			} else if (target instanceof Player) {
				Player targPlayer = (Player) target;
				if (!targPlayer.isOnline()) {
					breakSuffocateLocal(target);
					i--;
				}
			}
		}
		if (targets.size() == 0) {
			remove();
			return;
		}

		if (reqConstantAim) {
			double dist = player.getEyeLocation().distance(targets.get(0).getEyeLocation());
			Location targetLoc = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().normalize().multiply(dist));
			List<Entity> ents = GeneralMethods.getEntitiesAroundPoint(targetLoc, aimRadius);

			for (int i = 0; i < targets.size(); i++) {
				LivingEntity target = targets.get(i);
				if (!ents.contains(target)) {
					breakSuffocateLocal(target);
					i--;
				}
			}
			if (targets.size() == 0) {
				remove();
				return;
			}
		}

		if (System.currentTimeMillis() - time < chargeTime) {
			return;
		} else if (!started) {
			started = true;
			final Player fplayer = player;
			for (LivingEntity targ : targets) {
				final LivingEntity target = targ;
				BukkitRunnable br1 = new BukkitRunnable() {
					@Override
					public void run() {
						GeneralMethods.damageEntity(fplayer, target, damage);
					}
				};
				BukkitRunnable br2 = new BukkitRunnable() {
					@Override
					public void run() {
						target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowRepeat * 20), (int) slow));
					}
				};
				BukkitRunnable br3 = new BukkitRunnable() {
					@Override
					public void run() {
						target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (blindRepeat * 20), (int) blind));
					}
				};

				tasks.add(br1);
				tasks.add(br2);
				tasks.add(br3);
				br1.runTaskTimer(ProjectKorra.plugin, (long) (damageDelay * 20), (long) (damageRepeat * 20));
				br2.runTaskTimer(ProjectKorra.plugin, (long) (slowDelay * 20), (long) (slowRepeat * 20 / 0.25));
				br3.runTaskTimer(ProjectKorra.plugin, (long) (blindDelay * 20), (long) (blindRepeat * 20));
			}
		}

		animate();
		if (!player.isSneaking()) {
			remove();
			return;
		}
	}

	/**
	 * Animates this instance of the Suffocate ability. Depending on the
	 * specific time (dt) the ability will create a different set of
	 * SuffocationSpirals.
	 */
	public void animate() {
		long curTime = System.currentTimeMillis();
		long dt = curTime - time - chargeTime;
		int steps = 8 * particleScale;
		long delay = 2 / particleScale;
		long t1 = (long) (1500 * speedFactor);
		long t2 = (long) (2500 * speedFactor);
		long t3 = (long) (5000 * speedFactor);
		long t4 = (long) (6000 * speedFactor);
		for (LivingEntity lent : targets) {
			final LivingEntity target = lent;
			if (dt < t1) {
				new SuffocateSpiral(target, steps, radius, delay, 0, 0.25 - (0.25 * (double) dt / (double) t1), 0, SpiralType.HORIZONTAL1);
				new SuffocateSpiral(target, steps, radius, delay, 0, 0.25 - (0.25 * (double) dt / (double) t1), 0, SpiralType.HORIZONTAL2);
			} else if (dt < t2) {
				new SuffocateSpiral(target, steps, radius, delay, 0, 0, 0, SpiralType.HORIZONTAL1);
				new SuffocateSpiral(target, steps * 2, radius, delay, 0, 0, 0, SpiralType.VERTICAL1);
				new SuffocateSpiral(target, steps * 2, radius, delay, 0, 0, 0, SpiralType.VERTICAL2);
			} else if (dt < t3) {
				new SuffocateSpiral(target, steps, radius, delay, 0, 0, 0, SpiralType.HORIZONTAL1);
				new SuffocateSpiral(target, steps, radius, delay, 0, 0, 0, SpiralType.VERTICAL1);
				new SuffocateSpiral(target, steps, radius, delay, 0, 0, 0, SpiralType.VERTICAL2);
			} else if (dt < t4) {
				new SuffocateSpiral(target, steps, radius - Math.min(radius * 3 / 4, (radius * 3.0 / 4 * ((double) (dt - t3) / (double) (t4 - t3)))), delay, 0, 0, 0, SpiralType.HORIZONTAL1);
				new SuffocateSpiral(target, steps, radius - Math.min(radius * 3 / 4, (radius * 3.0 / 4 * ((double) (dt - t3) / (double) (t4 - t3)))), delay, 0, 0, 0, SpiralType.VERTICAL1);
				new SuffocateSpiral(target, steps, radius - Math.min(radius * 3 / 4, (radius * 3.0 / 4 * ((double) (dt - t3) / (double) (t4 - t3)))), delay, 0, 0, 0, SpiralType.VERTICAL2);
			} else {
				new SuffocateSpiral(target, steps, radius - (radius * 3.0 / 4.0), delay, 0, 0, 0, SpiralType.HORIZONTAL1);
				new SuffocateSpiral(target, steps, radius - (radius * 3.0 / 4.0), delay, 0, 0, 0, SpiralType.VERTICAL1);
				new SuffocateSpiral(target, steps, radius - (radius * 3.0 / 4.0), delay, 0, 0, 0, SpiralType.VERTICAL2);
			}
		}
	}

	/**
	 * ** Animates a Spiral of air particles around a location or a targetted
	 * entity. The direction of the spiral is determined by SpiralType, and each
	 * type is calculated independently from one another.
	 */
	public class SuffocateSpiral extends BukkitRunnable {
		private Location startLoc;
		private Location loc;
		private LivingEntity target;
		private int totalSteps;
		private double radius;
		private double dx, dy, dz;
		private SpiralType type;
		private int i;

		/**
		 * @param startLoc
		 *            : initial location
		 * @param totalSteps
		 *            : amount of times it will be animated
		 * @param radius
		 *            : the radius of the spiral
		 * @param interval
		 *            : the speed of the animation
		 * @param dx
		 *            : x offset
		 * @param dy
		 *            : y offset
		 * @param dz
		 *            : z offset
		 * @param type
		 *            : spiral animation direction
		 */
		public SuffocateSpiral(Location startLoc, int totalSteps, double radius, long interval, double dx, double dy, double dz, SpiralType type) {
			this.startLoc = startLoc;
			this.totalSteps = totalSteps;
			this.radius = radius;
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.type = type;

			loc = startLoc.clone();
			i = 0;
			this.runTaskTimer(ProjectKorra.plugin, 0L, interval);
			tasks.add(this);
		}

		/**
		 * @param lent
		 *            : the entity to animate the spiral around
		 * @param totalSteps
		 *            : amount of times it will be animated
		 * @param radius
		 *            : the radius of the spiral
		 * @param interval
		 *            : the speed of the animation
		 * @param dx
		 *            : x offset
		 * @param dy
		 *            : y offset
		 * @param dz
		 *            : z offset
		 * @param type
		 *            : spiral animation direction
		 */
		public SuffocateSpiral(LivingEntity lent, int totalSteps, double radius, long interval, double dx, double dy, double dz, SpiralType type) {
			this.target = lent;
			this.totalSteps = totalSteps;
			this.radius = radius;
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.type = type;

			loc = target.getEyeLocation();
			i = 0;
			this.runTaskTimer(ProjectKorra.plugin, 0L, interval);
			tasks.add(this);
		}

		/**
		 * Starts the initial animation, and removes itself when it is finished.
		 */
		public void run() {
			Location tempLoc;
			if (target != null) {
				tempLoc = target.getEyeLocation();
				tempLoc.setY(tempLoc.getY() - 0.5);
			} else tempLoc = startLoc.clone();

			if (type == SpiralType.HORIZONTAL1) {
				loc.setX(tempLoc.getX() + radius * Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setY(tempLoc.getY() + dy * i);
				loc.setZ(tempLoc.getZ() + radius * Math.sin(Math.toRadians((double) i / (double) totalSteps * 360)));
			} else if (type == SpiralType.HORIZONTAL2) {
				loc.setX(tempLoc.getX() + radius * -Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setY(tempLoc.getY() + dy * i);
				loc.setZ(tempLoc.getZ() + radius * -Math.sin(Math.toRadians((double) i / (double) totalSteps * 360)));
			} else if (type == SpiralType.VERTICAL1) {
				loc.setX(tempLoc.getX() + radius * Math.sin(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setY(tempLoc.getY() + radius * Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setZ(tempLoc.getZ() + dz * i);
			} else if (type == SpiralType.VERTICAL2) {
				loc.setX(tempLoc.getX() + dx * i);
				loc.setY(tempLoc.getY() + radius * Math.sin(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setZ(tempLoc.getZ() + radius * Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
			} else if (type == SpiralType.DIAGONAL1) {
				loc.setX(tempLoc.getX() + radius * Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setY(tempLoc.getY() + radius * Math.sin(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setZ(tempLoc.getZ() + radius * -Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
			} else if (type == SpiralType.DIAGONAL2) {
				loc.setX(tempLoc.getX() + radius * Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setY(tempLoc.getY() + radius * Math.sin(Math.toRadians((double) i / (double) totalSteps * 360)));
				loc.setZ(tempLoc.getZ() + radius * Math.cos(Math.toRadians((double) i / (double) totalSteps * 360)));
			}

			AirMethods.getAirbendingParticles().display(loc, (float) 0, (float) 0, (float) 0, 0, 1);
			if (i == totalSteps + 1) this.cancel();
			i++;
		}
	}

	/** Progresses every instance of Suffocate by 1 tick **/
	public static void progressAll() {
		for (Suffocate suff : instances.values())
			suff.progress();
	}

	/** Removes this instance of the ability **/
	public void remove() {
		instances.remove(player);
		for (int i = 0; i < tasks.size(); i++) {
			tasks.get(i).cancel();
			tasks.remove(i);
			i--;
		}
	}

	/**
	 * Removes all instances of Suffocate at loc within the radius threshold.
	 * The location of a Suffocate is defined at the benders location, not the
	 * location of the entities being suffocated.
	 * 
	 * @param causer
	 *            : the player causing this instance to be removed
	 * **/
	public static boolean removeAtLocation(Player causer, Location loc, double radius) {
		Iterator<Player> it = instances.keySet().iterator();
		while (it.hasNext()) {
			Player key = it.next();
			Suffocate val = instances.get(key);

			if (causer == null || !key.equals(causer)) {
				Location playerLoc = val.getPlayer().getLocation();
				if (playerLoc.getWorld().equals(loc.getWorld()) && playerLoc.distance(loc) <= radius) {
					it.remove();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Removes an instance of Suffocate if player is the one suffocating
	 * entities
	 **/
	public static void remove(Player player) {
		if (instances.containsKey(player)) instances.get(player).remove();
	}

	/** Removes every instance of Suffocate **/
	public static void removeAll() {
		Enumeration<Player> keys = instances.keys();
		while (keys.hasMoreElements()) {
			instances.get(keys.nextElement()).remove();
		}
	}

	/** Determines if a player is Suffocating entities **/
	public static boolean isChannelingSphere(Player player) {
		if (instances.containsKey(player)) return true;
		return false;
	}

	/** Stops an entity from being suffocated **/
	public void breakSuffocateLocal(Entity entity) {
		if (targets.contains(entity)) targets.remove(entity);
	}

	/** Stops an entity from being suffocated **/
	public static void breakSuffocate(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targets.contains(entity)) {
				instances.get(player).breakSuffocateLocal(entity);
			}
		}
	}

	/** Checks if an entity is being suffocated **/
	public static boolean isBreathbent(Entity entity) {
		for (Player player : instances.keySet()) {
			if (instances.get(player).targets.contains(entity)) {
				return instances.get(player).started;
			}
		}
		return false;
	}

	public static ConcurrentHashMap<Player, Suffocate> getInstances() {
		return instances;
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}

	public ArrayList<LivingEntity> getTargets() {
		return targets;
	}

	public void setTargets(ArrayList<LivingEntity> targets) {
		this.targets = targets;
	}

	public boolean isReqConstantAim() {
		return reqConstantAim;
	}

	public void setReqConstantAim(boolean reqConstantAim) {
		this.reqConstantAim = reqConstantAim;
	}

	public boolean isCanSuffUndead() {
		return canSuffUndead;
	}

	public void setCanSuffUndead(boolean canSuffUndead) {
		this.canSuffUndead = canSuffUndead;
	}

	public long getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public long getCooldown() {
		return cooldown;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public int getParticleScale() {
		return particleScale;
	}

	public void setParticleScale(int particleScale) {
		this.particleScale = particleScale;
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

	public double getSpeedFactor() {
		return speedFactor;
	}

	public void setSpeedFactor(double speedFactor) {
		this.speedFactor = speedFactor;
	}

	public double getAimRadius() {
		return aimRadius;
	}

	public void setAimRadius(double aimRadius) {
		this.aimRadius = aimRadius;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getDamageDelay() {
		return damageDelay;
	}

	public void setDamageDelay(double damageDelay) {
		this.damageDelay = damageDelay;
	}

	public double getDamageRepeat() {
		return damageRepeat;
	}

	public void setDamageRepeat(double damageRepeat) {
		this.damageRepeat = damageRepeat;
	}

	public double getSlow() {
		return slow;
	}

	public void setSlow(double slow) {
		this.slow = slow;
	}

	public double getslowRepeat() {
		return slowRepeat;
	}

	public void setslowRepeat(double slowRepeat) {
		this.slowRepeat = slowRepeat;
	}

	public double getSlowDelay() {
		return slowDelay;
	}

	public void setSlowDelay(double slowDelay) {
		this.slowDelay = slowDelay;
	}

	public double getBlind() {
		return blind;
	}

	public void setBlind(double blind) {
		this.blind = blind;
	}

	public double getBlindDelay() {
		return blindDelay;
	}

	public void setBlindDelay(double blindDelay) {
		this.blindDelay = blindDelay;
	}

	public double getBlindRepeat() {
		return blindRepeat;
	}

	public void setBlindRepeat(double blindRepeat) {
		this.blindRepeat = blindRepeat;
	}
}
