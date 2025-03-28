package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
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

	public Suffocate(final Player player) {
		super(player);
		this.ability = this;
		if (this.bPlayer.isOnCooldown(this)) {
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

		if (this.particleCount < 1) {
			this.particleCount = 1;
		} else if (this.particleCount > 2) {
			this.particleCount = 2;
		}

		if (this.bPlayer.isAvatarState()) {
			for (final Entity ent : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), this.range)) {
				if (ent instanceof LivingEntity && !ent.equals(player)) {
					this.targets.add((LivingEntity) ent);
				}
			}
		} else {
			List<Entity> entities = new ArrayList<Entity>();
			for (int i = 0; i < range; i++) {
				final Location location = GeneralMethods.getTargetedLocation(player, i, getTransparentMaterials());
				entities = GeneralMethods.getEntitiesAroundPoint(location, .5);

				entities.remove(player);

				if (!entities.isEmpty()) {
					break;
				}
			}
			if (entities.isEmpty()) {
				return;
			}
			final Entity target = entities.get(0);
			if (target instanceof LivingEntity) {
				this.targets.add((LivingEntity) target);
			}
		}

		if (!this.canSuffocateUndead) {
			for (int i = 0; i < this.targets.size(); i++) {
				final LivingEntity target = this.targets.get(i);
				if (GeneralMethods.isUndead(target)) {
					this.targets.remove(i);
					i--;
				}
			}
		}

		this.start();
	}

	@Override
	public void progress() {
		for (int i = 0; i < this.targets.size(); i++) {
			final LivingEntity target = this.targets.get(i);
			if (target.isDead() || !target.getWorld().equals(this.player.getWorld()) || target.getLocation().distanceSquared(this.player.getEyeLocation()) > this.range * this.range || GeneralMethods.isRegionProtectedFromBuild(this, target.getLocation()) || target instanceof ArmorStand) {
				this.breakSuffocateLocal(target);
				i--;
			} else if (target instanceof Player) {
				final Player targPlayer = (Player) target;
				if (!targPlayer.isOnline()) {
					this.breakSuffocateLocal(target);
					i--;
				}
			}
		}
		if (this.targets.size() == 0 || !this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.requireConstantAim) {
			double dist = 0;
			if (this.player.getWorld().equals(this.targets.get(0).getWorld())) {
				dist = this.player.getEyeLocation().distance(this.targets.get(0).getEyeLocation());
			}
			final Location targetLoc = GeneralMethods.getTargetedLocation(player, dist, false, getTransparentMaterials());
			final List<Entity> ents = GeneralMethods.getEntitiesAroundPoint(targetLoc, this.constantAimRadius);

			for (int i = 0; i < this.targets.size(); i++) {
				final LivingEntity target = this.targets.get(i);
				if (!ents.contains(target)) {
					this.breakSuffocateLocal(target);
					i--;
				}
			}
			if (this.targets.size() == 0) {
				this.remove();
				return;
			}
		}

		if (System.currentTimeMillis() - this.getStartTime() < this.chargeTime) {
			return;
		} else if (!this.started) {
			this.started = true;
			for (final LivingEntity target : this.targets) {
				final BukkitRunnable br1 = new BukkitRunnable() {
					@Override
					public void run() {
						DamageHandler.damageEntity(target, Suffocate.this.damage, Suffocate.this.ability);
					}
				};
				final BukkitRunnable br2 = new BukkitRunnable() {
					@Override
					public void run() {
						target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (Suffocate.this.slowRepeat * 20), (int) Suffocate.this.slow));
					}
				};
				final BukkitRunnable br3 = new BukkitRunnable() {
					@Override
					public void run() {
						target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (Suffocate.this.blindRepeat * 20), (int) Suffocate.this.blind));
					}
				};

				this.tasks.add(br1);
				this.tasks.add(br2);
				this.tasks.add(br3);
				br1.runTaskTimer(ProjectKorra.plugin, (long) (this.damageDelay * 20), (long) (this.damageRepeat * 20));
				br2.runTaskTimer(ProjectKorra.plugin, (long) (this.slowDelay * 20), (long) (this.slowRepeat * 20 / 0.25));
				br3.runTaskTimer(ProjectKorra.plugin, (long) (this.blindDelay * 20), (long) (this.blindRepeat * 20));
			}
		}

		for (final LivingEntity target : this.targets) {
			this.animate(target);
		}

		if (!this.player.isSneaking()) {
			this.remove();
			return;
		}
	}

	/** Stops an entity from being suffocated **/
	public static void breakSuffocate(final Entity entity) {
		for (final Suffocate suffocate : getAbilities(Suffocate.class)) {
			if (suffocate.targets.contains(entity)) {
				suffocate.breakSuffocateLocal(entity);
			}
		}
	}

	/** Checks if an entity is being suffocated **/
	public static boolean isBreathbent(final Entity entity) {
		for (final Suffocate suffocate : getAbilities(Suffocate.class)) {
			if (suffocate.targets.contains(entity)) {
				return suffocate.started;
			}
		}
		return false;
	}

	/** Determines if a player is Suffocating entities **/
	public static boolean isChannelingSphere(final Player player) {
		return hasAbility(player, Suffocate.class);
	}

	/**
	 * Removes an instance of Suffocate if player is the one suffocating
	 * entities
	 **/
	public static void remove(final Player player) {
		final Suffocate suff = getAbility(player, Suffocate.class);
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
	public static boolean removeAtLocation(final Player causer, final Location loc, final double radius) {
		if (causer == null) {
			return false;
		}
		for (final Suffocate suff : getAbilities(Suffocate.class)) {
			if (!suff.player.equals(causer)) {
				final Location playerLoc = suff.getPlayer().getLocation();
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
	public void animate(final LivingEntity target) {
		final int steps = 8 * this.particleCount;
		final long curTime = System.currentTimeMillis();
		final long dt = curTime - this.getStartTime() - this.chargeTime;
		final long delay = 2 / this.particleCount;
		final long t1 = (long) (1500 * this.animationSpeed);
		final long t2 = (long) (2500 * this.animationSpeed);
		final long t3 = (long) (5000 * this.animationSpeed);
		final long t4 = (long) (6000 * this.animationSpeed);
		if (dt < t1) {
			new SuffocateSpiral(target, steps, this.radius, delay, 0, 0.25 - (0.25 * dt / t1), 0, SpiralType.HORIZONTAL1);
			new SuffocateSpiral(target, steps, this.radius, delay, 0, 0.25 - (0.25 * dt / t1), 0, SpiralType.HORIZONTAL2);
		} else if (dt < t2) {
			new SuffocateSpiral(target, steps, this.radius, delay, 0, 0, 0, SpiralType.HORIZONTAL1);
			new SuffocateSpiral(target, steps * 2, this.radius, delay, 0, 0, 0, SpiralType.VERTICAL1);
			new SuffocateSpiral(target, steps * 2, this.radius, delay, 0, 0, 0, SpiralType.VERTICAL2);
		} else if (dt < t3) {
			new SuffocateSpiral(target, steps, this.radius, delay, 0, 0, 0, SpiralType.HORIZONTAL1);
			new SuffocateSpiral(target, steps, this.radius, delay, 0, 0, 0, SpiralType.VERTICAL1);
			new SuffocateSpiral(target, steps, this.radius, delay, 0, 0, 0, SpiralType.VERTICAL2);
		} else if (dt < t4) {
			new SuffocateSpiral(target, steps, this.radius - Math.min(this.radius * 3 / 4, (this.radius * 3.0 / 4 * ((double) (dt - t3) / (double) (t4 - t3)))), delay, 0, 0, 0, SpiralType.HORIZONTAL1);
			new SuffocateSpiral(target, steps, this.radius - Math.min(this.radius * 3 / 4, (this.radius * 3.0 / 4 * ((double) (dt - t3) / (double) (t4 - t3)))), delay, 0, 0, 0, SpiralType.VERTICAL1);
			new SuffocateSpiral(target, steps, this.radius - Math.min(this.radius * 3 / 4, (this.radius * 3.0 / 4 * ((double) (dt - t3) / (double) (t4 - t3)))), delay, 0, 0, 0, SpiralType.VERTICAL2);
		} else {
			new SuffocateSpiral(target, steps, this.radius - (this.radius * 3.0 / 4.0), delay, 0, 0, 0, SpiralType.HORIZONTAL1);
			new SuffocateSpiral(target, steps, this.radius - (this.radius * 3.0 / 4.0), delay, 0, 0, 0, SpiralType.VERTICAL1);
			new SuffocateSpiral(target, steps, this.radius - (this.radius * 3.0 / 4.0), delay, 0, 0, 0, SpiralType.VERTICAL2);
		}
	}

	/** Stops an entity from being suffocated **/
	public void breakSuffocateLocal(final Entity entity) {
		if (this.targets.contains(entity)) {
			this.targets.remove(entity);
		}
	}

	/** Removes this instance of the ability **/
	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
		for (int i = 0; i < this.tasks.size(); i++) {
			this.tasks.get(i).cancel();
			this.tasks.remove(i);
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
		private final Location loc;
		private LivingEntity target;
		private final int totalSteps;
		private final double radius;
		private final double dx, dy, dz;
		private final SpiralType type;
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
		public SuffocateSpiral(final LivingEntity lent, final int totalSteps, final double radius, final long interval, final double dx, final double dy, final double dz, final SpiralType type) {
			this.target = lent;
			this.totalSteps = totalSteps;
			this.radius = radius;
			this.dx = dx;
			this.dy = dy;
			this.dz = dz;
			this.type = type;
			this.loc = this.target.getEyeLocation();
			this.i = 0;
			this.runTaskTimer(ProjectKorra.plugin, 0L, interval);
			Suffocate.this.tasks.add(this);
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
		public SuffocateSpiral(final Location startLoc, final int totalSteps, final double radius, final long interval, final double dx, final double dy, final double dz, final SpiralType type) {
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
			Suffocate.this.tasks.add(this);
		}

		/**
		 * Starts the initial animation, and removes itself when it is finished.
		 */
		@Override
		public void run() {
			Location tempLoc;
			if (this.target != null) {
				tempLoc = this.target.getEyeLocation();
				tempLoc.setY(tempLoc.getY() - 0.5);
			} else {
				tempLoc = this.startLoc.clone();
			}

			if (this.type == SpiralType.HORIZONTAL1) {
				this.loc.setX(tempLoc.getX() + this.radius * Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setY(tempLoc.getY() + this.dy * this.i);
				this.loc.setZ(tempLoc.getZ() + this.radius * Math.sin(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
			} else if (this.type == SpiralType.HORIZONTAL2) {
				this.loc.setX(tempLoc.getX() + this.radius * -Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setY(tempLoc.getY() + this.dy * this.i);
				this.loc.setZ(tempLoc.getZ() + this.radius * -Math.sin(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
			} else if (this.type == SpiralType.VERTICAL1) {
				this.loc.setX(tempLoc.getX() + this.radius * Math.sin(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setY(tempLoc.getY() + this.radius * Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setZ(tempLoc.getZ() + this.dz * this.i);
			} else if (this.type == SpiralType.VERTICAL2) {
				this.loc.setX(tempLoc.getX() + this.dx * this.i);
				this.loc.setY(tempLoc.getY() + this.radius * Math.sin(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setZ(tempLoc.getZ() + this.radius * Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
			} else if (this.type == SpiralType.DIAGONAL1) {
				this.loc.setX(tempLoc.getX() + this.radius * Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setY(tempLoc.getY() + this.radius * Math.sin(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setZ(tempLoc.getZ() + this.radius * -Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
			} else if (this.type == SpiralType.DIAGONAL2) {
				this.loc.setX(tempLoc.getX() + this.radius * Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setY(tempLoc.getY() + this.radius * Math.sin(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
				this.loc.setZ(tempLoc.getZ() + this.radius * Math.cos(Math.toRadians((double) this.i / (double) this.totalSteps * 360)));
			}

			getAirbendingParticles().display(this.loc, 0, 0, 0, 0, 1);
			if (this.i == this.totalSteps + 1) {
				this.cancel();
			}
			this.i++;
		}
	}

	@Override
	public String getName() {
		return "Suffocate";
	}

	@Override
	public Location getLocation() {
		if (this.targets.size() > 0) {
			return this.targets.get(0).getLocation();
		} else if (this.player != null) {
			return this.player.getLocation();
		}
		return null;
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
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		locations.add(this.player.getLocation());
		return locations;
	}

	@Override
	public boolean isStarted() {
		return this.started;
	}

	public void setStarted(final boolean started) {
		this.started = started;
	}

	public boolean isRequireConstantAim() {
		return this.requireConstantAim;
	}

	public void setRequireConstantAim(final boolean requireConstantAim) {
		this.requireConstantAim = requireConstantAim;
	}

	public boolean isCanSuffocateUndead() {
		return this.canSuffocateUndead;
	}

	public void setCanSuffocateUndead(final boolean canSuffocateUndead) {
		this.canSuffocateUndead = canSuffocateUndead;
	}

	public int getParticleCount() {
		return this.particleCount;
	}

	public void setParticleCount(final int particleCount) {
		this.particleCount = particleCount;
	}

	public long getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final long chargeTime) {
		this.chargeTime = chargeTime;
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

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getDamageDelay() {
		return this.damageDelay;
	}

	public void setDamageDelay(final double damageDelay) {
		this.damageDelay = damageDelay;
	}

	public double getDamageRepeat() {
		return this.damageRepeat;
	}

	public void setDamageRepeat(final double damageRepeat) {
		this.damageRepeat = damageRepeat;
	}

	public double getSlow() {
		return this.slow;
	}

	public void setSlow(final double slow) {
		this.slow = slow;
	}

	public double getSlowRepeat() {
		return this.slowRepeat;
	}

	public void setSlowRepeat(final double slowRepeat) {
		this.slowRepeat = slowRepeat;
	}

	public double getSlowDelay() {
		return this.slowDelay;
	}

	public void setSlowDelay(final double slowDelay) {
		this.slowDelay = slowDelay;
	}

	public double getConstantAimRadius() {
		return this.constantAimRadius;
	}

	public void setConstantAimRadius(final double constantAimRadius) {
		this.constantAimRadius = constantAimRadius;
	}

	public double getBlind() {
		return this.blind;
	}

	public void setBlind(final double blind) {
		this.blind = blind;
	}

	public double getBlindDelay() {
		return this.blindDelay;
	}

	public void setBlindDelay(final double blindDelay) {
		this.blindDelay = blindDelay;
	}

	public double getBlindRepeat() {
		return this.blindRepeat;
	}

	public void setBlindRepeat(final double blindRepeat) {
		this.blindRepeat = blindRepeat;
	}

	public double getAnimationSpeed() {
		return this.animationSpeed;
	}

	public void setAnimationSpeed(final double animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}

	public ArrayList<LivingEntity> getTargets() {
		return this.targets;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
}
