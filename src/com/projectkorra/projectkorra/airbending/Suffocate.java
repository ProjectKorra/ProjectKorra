package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.DamageHandler;

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
public class Suffocate extends AirAbility {

	public static enum SpiralType {
		HORIZONTAL1, HORIZONTAL2, VERTICAL1, VERTICAL2, DIAGONAL1, DIAGONAL2
	};

	private boolean started;
	private boolean requireConstantAim;
	private boolean canSuffocateUndead;
	private int particleCount;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private double damageDelay;
	private double damageRepeat;
	private double slow;
	private double slowRepeat;
	private double slowDelay;
	private double constantAimRadius;
	private double blind;
	private double blindDelay;
	private double blindRepeat;
	private double animationSpeed;
	private Suffocate ability;
	private ArrayList<BukkitRunnable> tasks;
	private ArrayList<LivingEntity> targets;

	public Suffocate(Player player) {
		super(player);
		ability = this;
		if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (hasAbility(player, Suffocate.class)) {
			return;
		}

		this.started = false;
		this.requireConstantAim = getConfig().getBoolean("Abilities.Air.Suffocate.RequireConstantAim");
		this.canSuffocateUndead = getConfig().getBoolean("Abilities.Air.Suffocate.CanBeUsedOnUndeadMobs");
		this.particleCount = getConfig().getInt("Abilities.Air.Suffocate.AnimationParticleAmount");
		this.animationSpeed = getConfig().getDouble("Abilities.Air.Suffocate.AnimationSpeed");
		this.chargeTime = getConfig().getLong("Abilities.Air.Suffocate.ChargeTime");
		this.cooldown = getConfig().getLong("Abilities.Air.Suffocate.Cooldown");
		this.range = getConfig().getDouble("Abilities.Air.Suffocate.Range");
		this.radius = getConfig().getDouble("Abilities.Air.Suffocate.AnimationRadius");
		this.constantAimRadius = getConfig().getDouble("Abilities.Air.Suffocate.RequireConstantAimRadius");
		this.damage = getConfig().getDouble("Abilities.Air.Suffocate.Damage");
		this.damageDelay = getConfig().getDouble("Abilities.Air.Suffocate.DamageInitialDelay");
		this.damageRepeat = getConfig().getDouble("Abilities.Air.Suffocate.DamageInterval");
		this.slow = getConfig().getInt("Abilities.Air.Suffocate.SlowPotency");
		this.slowRepeat = getConfig().getDouble("Abilities.Air.Suffocate.SlowInterval");
		this.slowDelay = getConfig().getDouble("Abilities.Air.Suffocate.SlowDelay");
		this.blind = getConfig().getInt("Abilities.Air.Suffocate.BlindPotentcy");
		this.blindDelay = getConfig().getDouble("Abilities.Air.Suffocate.BlindDelay");
		this.blindRepeat = getConfig().getDouble("Abilities.Air.Suffocate.BlindInterval");
		this.targets = new ArrayList<>();
		this.tasks = new ArrayList<>();

		if (bPlayer.isAvatarState()) {
			cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Air.Suffocate.Cooldown");
			chargeTime = getConfig().getLong("Abilities.Avatar.AvatarState.Air.Suffocate.ChargeTime");
			damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.Suffocate.Damage");
			range = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.Suffocate.Range");

		}

		if (particleCount < 1) {
			particleCount = 1;
		} else if (particleCount > 2) {
			particleCount = 2;
		}

		if (bPlayer.isAvatarState()) {
			for (Entity ent : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), range)) {
				if (ent instanceof LivingEntity && !ent.equals(player)) {
					targets.add((LivingEntity) ent);
				}
			}
		} else {
			//Location location = GeneralMethods.getTargetedLocation(player, 6, getTransparentMaterial());
			//List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(location, 1.5);
			List<Entity> entities = new ArrayList<Entity>();
			for (int i = 0; i < 6; i++) {
				Location location = GeneralMethods.getTargetedLocation(player, i, getTransparentMaterial());
				entities = GeneralMethods.getEntitiesAroundPoint(location, 1.7);
				if (entities.contains(player))
					entities.remove(player);
				if (entities != null && !entities.isEmpty() && !entities.contains(player)) {
					break;
				}
			}
			if (entities == null || entities.isEmpty()) {
				return;
			}
			Entity target = entities.get(0);
			if (target != null && target instanceof LivingEntity) {
				targets.add((LivingEntity) target);
			}
		}

		if (!canSuffocateUndead) {
			for (int i = 0; i < targets.size(); i++) {
				LivingEntity target = targets.get(i);
				if (GeneralMethods.isUndead(target)) {
					targets.remove(i);
					i--;
				}
			}
		}

		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public void progress() {
		for (int i = 0; i < targets.size(); i++) {
			LivingEntity target = targets.get(i);
			if (target.isDead() || !target.getWorld().equals(player.getWorld()) || target.getLocation().distanceSquared(player.getEyeLocation()) > range * range || GeneralMethods.isRegionProtectedFromBuild(this, target.getLocation())) {
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
		if (targets.size() == 0 || !bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		if (requireConstantAim) {
			double dist = 0;
			if (player.getWorld().equals(targets.get(0).getWorld())) {
				dist = player.getEyeLocation().distance(targets.get(0).getEyeLocation());
			}
			Location targetLoc = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().normalize().multiply(dist));
			List<Entity> ents = GeneralMethods.getEntitiesAroundPoint(targetLoc, constantAimRadius);

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

		if (System.currentTimeMillis() - getStartTime() < chargeTime) {
			return;
		} else if (!started) {
			started = true;
			for (LivingEntity targ : targets) {
				final LivingEntity target = targ;
				BukkitRunnable br1 = new BukkitRunnable() {
					@Override
					public void run() {
						DamageHandler.damageEntity(target, damage, ability);
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

	/** Stops an entity from being suffocated **/
	public static void breakSuffocate(Entity entity) {
		for (Suffocate suffocate : getAbilities(Suffocate.class)) {
			if (suffocate.targets.contains(entity)) {
				suffocate.breakSuffocateLocal(entity);
			}
		}
	}

	/** Checks if an entity is being suffocated **/
	public static boolean isBreathbent(Entity entity) {
		for (Suffocate suffocate : getAbilities(Suffocate.class)) {
			if (suffocate.targets.contains(entity)) {
				return suffocate.started;
			}
		}
		return false;
	}

	/** Determines if a player is Suffocating entities **/
	public static boolean isChannelingSphere(Player player) {
		return hasAbility(player, Suffocate.class);
	}

	/**
	 * Removes an instance of Suffocate if player is the one suffocating
	 * entities
	 **/
	public static void remove(Player player) {
		Suffocate suff = getAbility(player, Suffocate.class);
		if (suff != null) {
			suff.remove();
		}
	}

	/**
	 * Removes all instances of Suffocate at loc within the radius threshold.
	 * The location of a Suffocate is defined at the benders location, not the
	 * location of the entities being suffocated.
	 * 
	 * @param causer The player causing this instance to be removed
	 **/
	public static boolean removeAtLocation(Player causer, Location loc, double radius) {
		if (causer == null) {
			return false;
		}
		for (Suffocate suff : getAbilities(Suffocate.class)) {
			if (!suff.player.equals(causer)) {
				Location playerLoc = suff.getPlayer().getLocation();
				if (playerLoc.getWorld().equals(loc.getWorld()) && playerLoc.distanceSquared(loc) <= radius * radius) {
					suff.remove();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Animates this instance of the Suffocate ability. Depending on the
	 * specific time (dt) the ability will create a different set of
	 * SuffocationSpirals.
	 */
	public void animate() {
		int steps = 8 * particleCount;
		long curTime = System.currentTimeMillis();
		long dt = curTime - getStartTime() - chargeTime;
		long delay = 2 / particleCount;
		long t1 = (long) (1500 * animationSpeed);
		long t2 = (long) (2500 * animationSpeed);
		long t3 = (long) (5000 * animationSpeed);
		long t4 = (long) (6000 * animationSpeed);
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

	/** Stops an entity from being suffocated **/
	public void breakSuffocateLocal(Entity entity) {
		if (targets.contains(entity)) {
			targets.remove(entity);
		}
	}

	/** Removes this instance of the ability **/
	@Override
	public void remove() {
		super.remove();
		for (int i = 0; i < tasks.size(); i++) {
			tasks.get(i).cancel();
			tasks.remove(i);
			i--;
		}
	}

	/**
	 * Animates a Spiral of air particles around a location or a targetted
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
		 * @param lent The entity to animate the spiral around
		 * @param totalSteps Amount of times it will be animated
		 * @param radius The radius of the spiral
		 * @param interval The speed of the animation
		 * @param dx x offset
		 * @param dy y offset
		 * @param dz z offset
		 * @param type Spiral animation direction
		 */
		public SuffocateSpiral(LivingEntity lent, int totalSteps, double radius, long interval, double dx, double dy, double dz, SpiralType type) {
			this.target = lent;
			this.totalSteps = totalSteps;
			this.radius = radius;
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.type = type;
			this.loc = target.getEyeLocation();
			this.i = 0;
			this.runTaskTimer(ProjectKorra.plugin, 0L, interval);
			tasks.add(this);
		}

		/**
		 * @param startLoc Initial location
		 * @param totalSteps Amount of times it will be animated
		 * @param radius The radius of the spiral
		 * @param interval The speed of the animation
		 * @param dx x offset
		 * @param dy y offset
		 * @param dz z offset
		 * @param type Spiral animation direction
		 */
		public SuffocateSpiral(Location startLoc, int totalSteps, double radius, long interval, double dx, double dy, double dz, SpiralType type) {
			this.startLoc = startLoc;
			this.totalSteps = totalSteps;
			this.radius = radius;
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.type = type;
			this.loc = startLoc.clone();
			this.i = 0;
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
			} else {
				tempLoc = startLoc.clone();
			}

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

			getAirbendingParticles().display(loc, (float) 0, (float) 0, (float) 0, 0, 1);
			if (i == totalSteps + 1) {
				this.cancel();
			}
			i++;
		}
	}

	@Override
	public String getName() {
		return "Suffocate";
	}

	@Override
	public Location getLocation() {
		if (targets.size() > 0) {
			return targets.get(0).getLocation();
		} else if (player != null) {
			return player.getLocation();
		}
		return null;
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
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		locations.add(player.getLocation());
		return locations;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public boolean isRequireConstantAim() {
		return requireConstantAim;
	}

	public void setRequireConstantAim(boolean requireConstantAim) {
		this.requireConstantAim = requireConstantAim;
	}

	public boolean isCanSuffocateUndead() {
		return canSuffocateUndead;
	}

	public void setCanSuffocateUndead(boolean canSuffocateUndead) {
		this.canSuffocateUndead = canSuffocateUndead;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}

	public long getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(long chargeTime) {
		this.chargeTime = chargeTime;
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

	public double getSlowRepeat() {
		return slowRepeat;
	}

	public void setSlowRepeat(double slowRepeat) {
		this.slowRepeat = slowRepeat;
	}

	public double getSlowDelay() {
		return slowDelay;
	}

	public void setSlowDelay(double slowDelay) {
		this.slowDelay = slowDelay;
	}

	public double getConstantAimRadius() {
		return constantAimRadius;
	}

	public void setConstantAimRadius(double constantAimRadius) {
		this.constantAimRadius = constantAimRadius;
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

	public double getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(double animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public ArrayList<LivingEntity> getTargets() {
		return targets;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
}
