package com.projectkorra.projectkorra.waterbending.blood;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.BloodAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempPotionEffect;

public class Bloodbending extends BloodAbility {

	private static final Map<Entity, Player> TARGETED_ENTITIES = new ConcurrentHashMap<Entity, Player>();
	private static final Set<EntityType> BLOODLESS_ENTITIES = loadBloodlessFromConfig();

	private boolean canOnlyBeUsedAtNight;
	@Attribute("CanBeUsedOnUndeadMobs")
	private boolean canBeUsedOnUndeadMobs;
	private boolean onlyUsableDuringMoon;
	@Attribute("CanBloodbendOtherBloodbenders")
	private boolean canBloodbendOtherBloodbenders;
	@Attribute(Attribute.RANGE)
	private double range;
	private long time;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;
	private Entity target;
	private Vector vector;

	public Bloodbending(final Player player) {
		super(player);

		final Bloodbending ability = getAbility(player, Bloodbending.class);
		if (ability != null) {
			ability.remove();
			return;
		}

		this.canOnlyBeUsedAtNight = getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedAtNight");
		this.canBeUsedOnUndeadMobs = getConfig().getBoolean("Abilities.Water.Bloodbending.CanBeUsedOnUndeadMobs");
		this.onlyUsableDuringMoon = getConfig().getBoolean("Abilities.Water.Bloodbending.CanOnlyBeUsedDuringFullMoon");
		this.canBloodbendOtherBloodbenders = getConfig().getBoolean("Abilities.Water.Bloodbending.CanBloodbendOtherBloodbenders");
		this.range = applyModifiers(getConfig().getDouble("Abilities.Water.Bloodbending.Range"));
		this.duration = getConfig().getInt("Abilities.Water.Bloodbending.Duration");
		this.cooldown = applyInverseModifiers(getConfig().getLong("Abilities.Water.Bloodbending.Cooldown"));
		this.knockback = applyModifiers(getConfig().getDouble("Abilities.Water.Bloodbending.Knockback"));
		this.vector = new Vector(0, 0, 0);

		if (this.canOnlyBeUsedAtNight && !isNight(player.getWorld()) && !this.bPlayer.canBloodbendAtAnytime()) {
			return;
		} else if (this.onlyUsableDuringMoon && !isFullMoon(player.getWorld()) && !this.bPlayer.canBloodbendAtAnytime()) {
			return;
		} else if (!this.bPlayer.canBend(this) && !this.bPlayer.isAvatarState()) {
			return;
		}

		if (this.bPlayer.isAvatarState()) {
			this.range += AvatarState.getValue(1.5);
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), this.range)) {
				if (entity instanceof LivingEntity) {
					if (BLOODLESS_ENTITIES.contains(entity.getType())) {
						continue;
					}
					if (entity instanceof Player) {
						final Player enemyPlayer = (Player) entity;
						final BendingPlayer enemyBPlayer = BendingPlayer.getBendingPlayer(enemyPlayer);
						if (enemyBPlayer == null || GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || enemyBPlayer.isAvatarState() || entity.getEntityId() == player.getEntityId() || enemyBPlayer.canBendIgnoreBindsCooldowns(this)) {
							continue;
						}
					}
					DamageHandler.damageEntity(entity, 0, this);
					AirAbility.breakBreathbendingHold(entity);
					TARGETED_ENTITIES.put(entity, player);
				}
			}
		} else {
			List<Entity> entities = new CopyOnWriteArrayList<Entity>();
			for (int i = 0; i < this.range; i++) {
				final Location location = GeneralMethods.getTargetedLocation(player, i, getTransparentMaterials());
				entities = GeneralMethods.getEntitiesAroundPoint(location, 1.7);

				entities.remove(player);
				entities.removeIf(e -> !(e instanceof LivingEntity) || e instanceof ArmorStand);

				if (!entities.isEmpty() && !entities.contains(player)) {
					break;
				}
			}
			if (entities.isEmpty()) {
				return;
			}
			this.target = entities.get(0);

			if (this.target == null || !(this.target instanceof LivingEntity) || GeneralMethods.isRegionProtectedFromBuild(this, this.target.getLocation()) || this.target.getEntityId() == player.getEntityId() || BLOODLESS_ENTITIES.contains(this.target.getType())) {
				return;
			} else if (this.target instanceof Player) {
				final BendingPlayer targetBPlayer = BendingPlayer.getBendingPlayer((Player) this.target);
				if (targetBPlayer != null) {
					if (targetBPlayer.canBloodbend() && !this.canBloodbendOtherBloodbenders) {
						return;
					} else if (targetBPlayer.isAvatarState()) {
						return;
					} else if (targetBPlayer.canBloodbendAtAnytime() && !this.canBloodbendOtherBloodbenders) {
						return;
					}
				}
			} else if (!this.canBeUsedOnUndeadMobs && GeneralMethods.isUndead(this.target)) {
				return;
			}

			DamageHandler.damageEntity(this.target, 0, this);
			HorizontalVelocityTracker.remove(this.target);
			AirAbility.breakBreathbendingHold(this.target);
			TARGETED_ENTITIES.put(this.target, player);
		}

		this.time = System.currentTimeMillis();
		this.start();
	}

	@Override
	public double applyModifiers(double value) {
		return canOnlyBeUsedAtNight ? value : super.applyModifiers(value);
	}

	@Override
	public long applyInverseModifiers(long value) {
		return canOnlyBeUsedAtNight ? value : super.applyInverseModifiers(value);
	}

	public static void launch(final Player player) {
		final Bloodbending bloodbending = getAbility(player, Bloodbending.class);
		if (bloodbending != null) {
			bloodbending.launch();
		}
	}

	private void launch() {
		final Location location = this.player.getLocation();
		for (final Entity entity : TARGETED_ENTITIES.keySet()) {
			final Location target = entity.getLocation().clone();
			Vector vector = new Vector(0, 0, 0);
			if (location.getWorld().equals(target.getWorld())) {
				vector = GeneralMethods.getDirection(location, GeneralMethods.getTargetedLocation(this.player, location.distance(target)));
			}
			vector.normalize();
			GeneralMethods.setVelocity(this, entity, vector.multiply(this.knockback));
			new HorizontalVelocityTracker(entity, this.player, 200, this);
		}
		this.remove();
		this.bPlayer.addCooldown(this);
	}

	@Override
	public void progress() {
		final PotionEffect effect = new PotionEffect(PotionEffectType.SLOW, 60, 1);

		if (!this.player.isSneaking()) {
			bPlayer.addCooldown(this);
			this.remove();
			return;
		} else if (this.duration > 0 && System.currentTimeMillis() - this.time > this.duration) {
			this.remove();
			this.bPlayer.addCooldown(this);
			return;
		}

		if (!this.canBeUsedOnUndeadMobs) {
			for (final Entity entity : TARGETED_ENTITIES.keySet()) {
				if (GeneralMethods.isUndead(entity)) {
					TARGETED_ENTITIES.remove(entity);
				}
			}
		}

		if (this.onlyUsableDuringMoon && !isFullMoon(this.player.getWorld()) && !this.bPlayer.canBloodbendAtAnytime()) {
			this.remove();
			return;
		} else if (this.canOnlyBeUsedAtNight && !isNight(this.player.getWorld()) && !this.bPlayer.canBloodbendAtAnytime()) {
			this.remove();
			return;
		} else if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		}

		if (this.bPlayer.isAvatarState()) {
			final ArrayList<Entity> entities = new ArrayList<>();

			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.player.getLocation(), this.range)) {
				if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
					continue;
				} else if (!(entity instanceof LivingEntity)) {
					continue;
				} else if (entity instanceof Player) {
					final BendingPlayer targetBPlayer = BendingPlayer.getBendingPlayer((Player) entity);
					if (targetBPlayer != null) {
						if (!targetBPlayer.canBeBloodbent() || entity.getEntityId() == this.player.getEntityId()) {
							continue;
						}
					}
				}

				entities.add(entity);
				if (!TARGETED_ENTITIES.containsKey(entity) && entity instanceof LivingEntity && !BLOODLESS_ENTITIES.contains(entity.getType())) {
					DamageHandler.damageEntity(entity, 0, this);
					TARGETED_ENTITIES.put(entity, this.player);
				}

				if (this.player.getWorld() != entity.getLocation().getWorld()) {
					TARGETED_ENTITIES.remove(entity);
					continue;
				}
				if (entity instanceof LivingEntity) {
					GeneralMethods.setVelocity(this, entity, this.vector);
					new TempPotionEffect((LivingEntity) entity, effect);
					entity.setFallDistance(0);
					if (entity instanceof Creature) {
						((Creature) entity).setTarget(null);
					}
					AirAbility.breakBreathbendingHold(entity);
				}
			}

			for (final Entity entity : TARGETED_ENTITIES.keySet()) {
				if (!entities.contains(entity) && TARGETED_ENTITIES.get(entity) == this.player) {
					TARGETED_ENTITIES.remove(entity);
				}
			}
		} else {
			for (final Entity entity : TARGETED_ENTITIES.keySet()) {
				if (entity instanceof Player) {
					final BendingPlayer targetBPlayer = BendingPlayer.getBendingPlayer((Player) entity);
					if (targetBPlayer != null && !targetBPlayer.canBeBloodbent()) {
						TARGETED_ENTITIES.remove(entity);
						continue;
					} else if (targetBPlayer.isAvatarState()) {
						TARGETED_ENTITIES.remove(entity);
						continue;
					}
				}

				final Location newLocation = entity.getLocation();
				if (this.player.getWorld() != newLocation.getWorld()) {
					TARGETED_ENTITIES.remove(entity);
					continue;
				}
			}

			if (!TARGETED_ENTITIES.containsKey(this.target)) {
				this.remove();
				return;
			}

			if (TARGETED_ENTITIES.get(this.target) != this.player) {
				this.remove();
				return;
			}

			final Location location = GeneralMethods.getTargetedLocation(this.player, 6, getTransparentMaterials());
			double distance = 0;
			if (location.getWorld().equals(this.target.getWorld())) {
				distance = location.distance(this.target.getLocation());
			}
			double dx, dy, dz;
			dx = location.getX() - this.target.getLocation().getX();
			dy = location.getY() - this.target.getLocation().getY();
			dz = location.getZ() - this.target.getLocation().getZ();
			this.vector = new Vector(dx, dy, dz);
			this.vector.normalize().multiply(.5);

			if (distance < .6) {
				this.vector = new Vector(0, 0, 0);
			}

			GeneralMethods.setVelocity(this, this.target, this.vector);

			new TempPotionEffect((LivingEntity) this.target, effect);
			this.target.setFallDistance(0);
			if (this.target instanceof Creature) {
				((Creature) this.target).setTarget(null);
			}
			AirAbility.breakBreathbendingHold(this.target);
		}
	}
	
	public static Set<EntityType> loadBloodlessFromConfig() {
		Set<EntityType> set = new HashSet<>();
		if (BLOODLESS_ENTITIES != null) {
			BLOODLESS_ENTITIES.clear();
		}
		for (String s : getConfig().getStringList("Abilities.Water.Bloodbending.Bloodless")) {
			try {
				EntityType type = EntityType.valueOf(s);
				if (type != null) {
					set.add(type);
					if (BLOODLESS_ENTITIES != null) {
						BLOODLESS_ENTITIES.add(type);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return set;
	}

	@Override
	public void remove() {
		if (!this.bPlayer.isAvatarState() && this.target != null) {
			if (System.currentTimeMillis() < this.getStartTime() + 1200) {
				this.bPlayer.addCooldown(this); // Prevents spamming.
			}
		}
		for (final Entity e : TARGETED_ENTITIES.keySet()) {
			if (TARGETED_ENTITIES.get(e) == this.player) {
				TARGETED_ENTITIES.remove(e);
			}
		}

		super.remove();
	}

	public static boolean isBloodbent(final Entity entity) {
		return entity != null ? TARGETED_ENTITIES.containsKey(entity) : null;
	}

	public static Location getBloodbendingLocation(final Entity entity) {
		return entity != null ? TARGETED_ENTITIES.get(entity).getLocation() : null;
	}

	public static Vector getBloodbendingVector(final Entity entity) {
		if (!TARGETED_ENTITIES.containsKey(entity)) {
			return null;
		}

		final Bloodbending bb = getAbility(TARGETED_ENTITIES.get(entity), Bloodbending.class);
		return bb.vector;
	}

	public static BendingPlayer getBloodbender(final Entity entity) {
		if (!TARGETED_ENTITIES.containsKey(entity)) {
			return null;
		}

		final Bloodbending bb = getAbility(TARGETED_ENTITIES.get(entity), Bloodbending.class);
		return bb.getBendingPlayer();
	}
	
	public static Iterable<EntityType> getBloodlessEntities() {
		return BLOODLESS_ENTITIES;
	}

	@Override
	public String getName() {
		return "Bloodbending";
	}

	@Override
	public Location getLocation() {
		if (this.target != null) {
			return this.target.getLocation();
		}
		return this.player != null ? this.player.getLocation() : null;
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
		// for collision purposes we only care about the player's location.
		final ArrayList<Location> locations = new ArrayList<>();
		if (this.player != null) {
			locations.add(this.player.getLocation());
		}
		return locations;
	}

	public boolean isCanOnlyBeUsedAtNight() {
		return this.canOnlyBeUsedAtNight;
	}

	public void setCanOnlyBeUsedAtNight(final boolean canOnlyBeUsedAtNight) {
		this.canOnlyBeUsedAtNight = canOnlyBeUsedAtNight;
	}

	public boolean isCanBeUsedOnUndeadMobs() {
		return this.canBeUsedOnUndeadMobs;
	}

	public void setCanBeUsedOnUndeadMobs(final boolean canBeUsedOnUndeadMobs) {
		this.canBeUsedOnUndeadMobs = canBeUsedOnUndeadMobs;
	}

	public boolean isOnlyUsableDuringMoon() {
		return this.onlyUsableDuringMoon;
	}

	public void setOnlyUsableDuringMoon(final boolean onlyUsableDuringMoon) {
		this.onlyUsableDuringMoon = onlyUsableDuringMoon;
	}

	public boolean isCanBloodbendOtherBloodbenders() {
		return this.canBloodbendOtherBloodbenders;
	}

	public void setCanBloodbendOtherBloodbenders(final boolean canBloodbendOtherBloodbenders) {
		this.canBloodbendOtherBloodbenders = canBloodbendOtherBloodbenders;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public double getKnockback() {
		return this.knockback;
	}

	public void setKnockback(final double knockback) {
		this.knockback = knockback;
	}

	public Entity getTarget() {
		return this.target;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
