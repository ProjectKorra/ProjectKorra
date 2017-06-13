package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

/*
 * TODO: Combo classes should eventually be rewritten so that each combo is
 * treated as an individual ability. In the mean time, we will just place "fake"
 * classes so that CoreAbility will register each ability.
 */
public class FireComboLegacy extends FireAbility implements ComboAbility {

	private boolean firstTime;
	private int progressCounter;
	private long time;
	private long cooldown;
	private double damage;
	private double speed;
	private double range;
	private double knockback;
	private double fireTicks;
	private double height;
	private double radius;
	private ClickType clickType;
	private String ability;
	private Location origin;
	private Location location;
	private Location destination;
	private Vector direction;
	private ArrayList<LivingEntity> affectedEntities;
	private ArrayList<FireComboStream> tasks;

	public FireComboLegacy(Player player, String ability) {
		super(player);
		this.ability = ability;

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		this.firstTime = true;
		this.time = System.currentTimeMillis();
		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		if (ability.equalsIgnoreCase("FireKick")) {
			this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireKick.Damage");
			this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireKick.Range");
			this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireKick.Cooldown");
			this.speed = 1;
		} else if (ability.equalsIgnoreCase("FireSpin")) {
			this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Damage");
			this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Range");
			this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireSpin.Cooldown");
			this.knockback = getConfig().getDouble("Abilities.Fire.FireCombo.FireSpin.Knockback");
			this.speed = 0.3;
		} else if (ability.equalsIgnoreCase("FireWheel")) {
			this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.Damage");
			this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.Range");
			this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.Speed");
			this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireWheel.Cooldown");
			this.fireTicks = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.FireTicks");
			this.height = 2;
			this.radius = 1;
		} else if (ability.equalsIgnoreCase("JetBlast")) {
			this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlast.Speed");
			this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.JetBlast.Cooldown");
		} else if (ability.equalsIgnoreCase("JetBlaze")) {
			this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlaze.Damage");
			this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlaze.Speed");
			this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.JetBlaze.Cooldown");
			this.fireTicks = getConfig().getDouble("Abilities.Fire.FireCombo.JetBlaze.FireTicks");
		}

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
		}
		start();
	}

	/**
	 * Returns all of the FireCombos created by a specific player but filters
	 * the abilities based on shift or click.
	 */
	public static ArrayList<FireComboLegacy> getFireCombo(Player player, ClickType type) {
		ArrayList<FireComboLegacy> list = new ArrayList<FireComboLegacy>();
		for (FireComboLegacy lf : getAbilities(player, FireComboLegacy.class)) {
			if (lf.clickType == type) {
				list.add(lf);
			}
		}
		return list;
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeAroundPoint(Player player, String ability, Location loc, double radius) {
		boolean removed = false;
		for (FireComboLegacy combo : getAbilities(FireComboLegacy.class)) {
			if (combo.getPlayer().equals(player)) {
				continue;
			}

			if (ability.equalsIgnoreCase("FireKick") && combo.ability.equalsIgnoreCase("FireKick")) {
				for (FireComboStream fs : combo.tasks) {
					if (fs.getLocation() != null && fs.getLocation().getWorld() == loc.getWorld() && Math.abs(fs.getLocation().distanceSquared(loc)) <= radius * radius) {
						fs.remove();
						removed = true;
					}
				}
			} else if (ability.equalsIgnoreCase("FireSpin") && combo.ability.equalsIgnoreCase("FireSpin")) {
				for (FireComboStream fs : combo.tasks) {
					if (fs.getLocation() != null && fs.getLocation().getWorld().equals(loc.getWorld())) {
						if (Math.abs(fs.getLocation().distanceSquared(loc)) <= radius * radius) {
							fs.remove();
							removed = true;
						}
					}
				}
			} else if (ability.equalsIgnoreCase("FireWheel") && combo.ability.equalsIgnoreCase("FireWheel")) {
				if (combo.location != null && Math.abs(combo.location.distanceSquared(loc)) <= radius * radius) {
					combo.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	public void collision(LivingEntity entity, Vector direction, FireComboStream fstream) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "Blaze", entity.getLocation())) {
			return;
		}
		entity.getLocation().getWorld().playSound(entity.getLocation(), Sound.ENTITY_VILLAGER_HURT, 0.3f, 0.3f);

		if (ability.equalsIgnoreCase("FireKick")) {
			if (!affectedEntities.contains(entity)) {
				affectedEntities.add(entity);
				DamageHandler.damageEntity(entity, damage, this);
				fstream.remove();
			}
		} else if (ability.equalsIgnoreCase("FireSpin")) {
			if (entity instanceof Player) {
				if (Commands.invincible.contains(((Player) entity).getName())) {
					return;
				}
			}
			if (!affectedEntities.contains(entity)) {
				affectedEntities.add(entity);
				double newKnockback = bPlayer.isAvatarState() ? knockback + 0.5 : knockback;
				DamageHandler.damageEntity(entity, damage, this);
				entity.setVelocity(direction.normalize().multiply(newKnockback));
				fstream.remove();
			}
		} else if (ability.equalsIgnoreCase("JetBlaze")) {
			if (!affectedEntities.contains(entity)) {
				affectedEntities.add(entity);
				DamageHandler.damageEntity(entity, damage, this);
				entity.setFireTicks((int) (fireTicks * 20));
				new FireDamageTimer(entity, player);
			}
		} else if (ability.equalsIgnoreCase("FireWheel")) {
			if (!affectedEntities.contains(entity)) {
				affectedEntities.add(entity);
				DamageHandler.damageEntity(entity, damage, this);
				entity.setFireTicks((int) (fireTicks * 20));
				new FireDamageTimer(entity, player);
				this.remove();
			}
		}
	}

	@Override
	public void progress() {
		progressCounter++;
		for (int i = 0; i < tasks.size(); i++) {
			BukkitRunnable br = tasks.get(i);
			if (br instanceof FireComboStream) {
				FireComboStream fs = (FireComboStream) br;
				if (fs.isCancelled()) {
					tasks.remove(fs);
				}
			}
		}

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (ability.equalsIgnoreCase("FireKick")) {
			if (destination == null) {
				if (bPlayer.isOnCooldown("FireKick") && !bPlayer.isAvatarState()) {
					remove();
					return;
				}

				bPlayer.addCooldown("FireKick", cooldown);
				Vector eyeDir = player.getEyeLocation().getDirection().normalize().multiply(range);
				destination = player.getEyeLocation().add(eyeDir);

				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_JUMP, 0.5f, 0f);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 1f);
				for (int i = -30; i <= 30; i += 5) {
					Vector vec = GeneralMethods.getDirection(player.getLocation(), destination.clone());
					vec = GeneralMethods.rotateXZ(vec, i);

					FireComboStream fs = new FireComboStream(this, vec, player.getLocation(), range, speed, "FireKick");
					fs.setSpread(0.2F);
					fs.setDensity(5);
					fs.setUseNewParticles(true);
					if (tasks.size() % 3 != 0) {
						fs.setCollides(false);
					}
					fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
					tasks.add(fs);
					player.getWorld().playSound(player.getLocation(), Sound.ITEM_FLINTANDSTEEL_USE, 0.5f, 1f);
				}
				location = tasks.get(0).getLocation();
			} else if (tasks.size() == 0) {
				remove();
				return;
			}
		} else if (ability.equalsIgnoreCase("FireSpin")) {
			if (destination == null) {
				if (bPlayer.isOnCooldown("FireSpin") && !bPlayer.isAvatarState()) {
					remove();
					return;
				}
				bPlayer.addCooldown("FireSpin", cooldown);
				destination = player.getEyeLocation().add(range, 0, range);
				player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 0.5f, 0.5f);

				for (int i = 0; i <= 360; i += 5) {
					Vector vec = GeneralMethods.getDirection(player.getLocation(), destination.clone());
					vec = GeneralMethods.rotateXZ(vec, i - 180);
					vec.setY(0);

					FireComboStream fs = new FireComboStream(this, vec, player.getLocation().clone().add(0, 1, 0), range, speed, "FireSpin");
					fs.setSpread(0.0F);
					fs.setDensity(1);
					fs.setUseNewParticles(true);
					if (tasks.size() % 10 != 0) {
						fs.setCollides(false);
					}
					fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
					tasks.add(fs);
				}
			}

			if (tasks.size() == 0) {
				remove();
				return;
			}
		} else if (ability.equalsIgnoreCase("JetBlast")) {
			if (System.currentTimeMillis() - time > 5000) {
				remove();
				return;
			} else if (hasAbility(player, FireJet.class)) {
				if (firstTime) {
					if (bPlayer.isOnCooldown("JetBlast") && !bPlayer.isAvatarState()) {
						remove();
						return;
					}

					bPlayer.addCooldown("JetBlast", cooldown);
					firstTime = false;
					float spread = 0F;
					ParticleEffect.LARGE_EXPLODE.display(player.getLocation(), spread, spread, spread, 0, 1);
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 15, 0F);
				}
				FireJet fj = getAbility(player, FireJet.class);
				fj.setSpeed(speed);
				FireComboStream fs = new FireComboStream(this, player.getVelocity().clone().multiply(-1), player.getLocation(), 3, 0.5, "JetBlast");

				fs.setDensity(1);
				fs.setSpread(0.9F);
				fs.setUseNewParticles(true);
				fs.setCollides(false);
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
				tasks.add(fs);
			}
		} else if (ability.equalsIgnoreCase("JetBlaze")) {
			if (firstTime) {
				if (bPlayer.isOnCooldown("JetBlaze") && !bPlayer.isAvatarState()) {
					remove();
					return;
				}
				bPlayer.addCooldown("JetBlaze", cooldown);
				firstTime = false;
			} else if (System.currentTimeMillis() - time > 5000) {
				remove();
				return;
			} else if (hasAbility(player, FireJet.class)) {
				direction = player.getVelocity().clone().multiply(-1);
				FireJet fj = getAbility(player, FireJet.class);
				fj.setSpeed(speed);

				FireComboStream fs = new FireComboStream(this, direction, player.getLocation(), 5, 1, "JetBlaze");
				fs.setDensity(8);
				fs.setSpread(1.0F);
				fs.setUseNewParticles(true);
				fs.setCollisionRadius(3);
				fs.setParticleEffect(ParticleEffect.LARGE_SMOKE);
				if (progressCounter % 5 != 0) {
					fs.setCollides(false);
				}
				fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
				tasks.add(fs);
				if (progressCounter % 4 == 0) {
					player.getWorld().playSound(player.getLocation(), Sound.ENTITY_CREEPER_PRIMED, 1, 0F);
				}
			}
		} else if (ability.equalsIgnoreCase("FireWheel")) {
			if (location == null) {
				if (bPlayer.isOnCooldown("FireWheel") && !bPlayer.isAvatarState()) {
					remove();
					return;
				}
				bPlayer.addCooldown("FireWheel", cooldown);
				origin = player.getLocation();

				if (GeneralMethods.getTopBlock(player.getLocation(), 3, 3) == null) {
					remove();
					return;
				}

				location = player.getLocation();
				direction = player.getEyeLocation().getDirection().clone().normalize();
				direction.setY(0);
			} else if (location.distanceSquared(origin) > range * range) {
				remove();
				return;
			}

			Block topBlock = GeneralMethods.getTopBlock(location, 2, -4);
			if (topBlock.getType().equals(Material.SNOW)) {

				topBlock = topBlock.getLocation().add(0, -1, 0).getBlock();
			}
			if (topBlock == null || (WaterAbility.isWaterbendable(player, ability, topBlock) && !isPlant(topBlock))) {

				remove();
				return;
			} else if (topBlock.getType() == Material.FIRE || ElementalAbility.isPlant(topBlock)) {
				topBlock = topBlock.getLocation().add(0, -1, 0).getBlock();
			}
			location.setY(topBlock.getY() + height);
			FireComboStream fs = new FireComboStream(this, direction, location.clone().add(0, -1, 0), 5, 1, "FireWheel");

			fs.setDensity(0);
			fs.setSinglePoint(true);
			fs.setCollisionRadius(1.5);
			fs.setCollides(true);
			fs.runTaskTimer(ProjectKorra.plugin, 0, 1L);
			tasks.add(fs);

			for (double i = -180; i <= 180; i += 3) {
				Location tempLoc = location.clone();
				Vector newDir = direction.clone().multiply(radius * Math.cos(Math.toRadians(i)));
				tempLoc.add(newDir);
				tempLoc.setY(tempLoc.getY() + (radius * Math.sin(Math.toRadians(i))));
				ParticleEffect.FLAME.display(tempLoc, 0, 0, 0, 0, 1);
			}

			location = location.add(direction.clone().multiply(speed));
			location.getWorld().playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1, 1);
		}
	}

	/**
	 * Removes this instance of FireCombo, cleans up any blocks that are
	 * remaining in totalBlocks, and cancels any remaining tasks.
	 */
	@Override
	public void remove() {
		super.remove();
		for (BukkitRunnable task : tasks) {
			task.cancel();
		}
	}

	public static class FireComboStream extends BukkitRunnable {
		private boolean useNewParticles;
		private boolean cancelled;
		private boolean collides;
		private boolean singlePoint;
		private int density;
		private int checkCollisionDelay;
		private int checkCollisionCounter;
		private float spread;
		private double collisionRadius;
		private double speed;
		private double distance;
		ParticleEffect particleEffect;
		private FireComboLegacy fireCombo;
		private Vector direction;
		private Location initialLocation;
		private Location location;
		private String ability;

		public FireComboStream(FireComboLegacy fireCombo, Vector direction, Location location, double distance, double speed, String ability) {
			this.useNewParticles = false;
			this.cancelled = false;
			this.collides = true;
			this.singlePoint = false;
			this.density = 1;
			this.checkCollisionDelay = 1;
			this.checkCollisionCounter = 0;
			this.spread = 0;
			this.collisionRadius = 2;
			this.particleEffect = ParticleEffect.FLAME;
			this.fireCombo = fireCombo;
			this.direction = direction;
			this.speed = speed;
			this.initialLocation = location.clone();
			this.location = location.clone();
			this.distance = distance;
			this.ability = ability;
		}

		@Override
		public void run() {
			Block block = location.getBlock();
			if (block.getRelative(BlockFace.UP).getType() != Material.AIR && !ElementalAbility.isPlant(block)) {
				remove();
				return;
			}
			for (int i = 0; i < density; i++) {
				if (useNewParticles) {
					particleEffect.display(location, spread, spread, spread, 0, 1);
				} else {
					location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0, 15);
				}
			}

			location.add(direction.normalize().multiply(speed));
			if (initialLocation.distanceSquared(location) > distance * distance) {
				remove();
				return;
			} else if (collides && checkCollisionCounter % checkCollisionDelay == 0) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, collisionRadius)) {
					if (entity instanceof LivingEntity && !entity.equals(fireCombo.getPlayer())) {
						fireCombo.collision((LivingEntity) entity, direction, this);
					}
				}
			}

			checkCollisionCounter++;
			if (singlePoint) {
				remove();
			}
		}

		@Override
		public void cancel() {
			remove();
		}

		public Vector getDirection() {
			return this.direction.clone();
		}

		public Location getLocation() {
			return this.location;
		}

		public String getAbility() {
			return this.ability;
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public void remove() {
			super.cancel();
			this.cancelled = true;
		}

		public void setCheckCollisionDelay(int delay) {
			this.checkCollisionDelay = delay;
		}

		public void setCollides(boolean b) {
			this.collides = b;
		}

		public void setCollisionRadius(double radius) {
			this.collisionRadius = radius;
		}

		public void setDensity(int density) {
			this.density = density;
		}

		public void setParticleEffect(ParticleEffect effect) {
			this.particleEffect = effect;
		}

		public void setSinglePoint(boolean b) {
			this.singlePoint = b;
		}

		public void setSpread(float spread) {
			this.spread = spread;
		}

		public void setUseNewParticles(boolean b) {
			useNewParticles = b;
		}
	}

	@Override
	public String getName() {
		return ability != null ? ability : "FireCombo";
	}

	@Override
	public Location getLocation() {
		return location != null ? location : origin;
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
		// Override in subclasses
		return false;
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (FireComboStream stream : tasks) {
			locations.add(stream.getLocation());
		}
		return locations;
	}

	@Override
	public String getInstructions() {
		return null;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}

	public void handleCollisionFireStreams(Collision collision) {
		if (collision.isRemovingFirst()) {
			ArrayList<FireComboStream> newTasks = new ArrayList<>();
			double collisionDistanceSquared = Math.pow(getCollisionRadius() + collision.getAbilitySecond().getCollisionRadius(), 2);
			// Remove all of the streams that are by this specific ourLocation.
			// Don't just do a single stream at a time or this algorithm becomes O(n^2) with
			// Collision's detection algorithm.
			for (FireComboStream stream : tasks) {
				if (stream.getLocation().distanceSquared(collision.getLocationSecond()) > collisionDistanceSquared) {
					newTasks.add(stream);
				} else {
					stream.cancel();
				}
			}
			tasks = newTasks;
		}
	}

	public boolean isHiddenAbility() {
		return true;
	}

	public boolean isFirstTime() {
		return firstTime;
	}

	public void setFirstTime(boolean firstTime) {
		this.firstTime = firstTime;
	}

	public int getProgressCounter() {
		return progressCounter;
	}

	public void setProgressCounter(int progressCounter) {
		this.progressCounter = progressCounter;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
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

	public double getKnockback() {
		return knockback;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
	}

	public double getFireTicks() {
		return fireTicks;
	}

	public void setFireTicks(double fireTicks) {
		this.fireTicks = fireTicks;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public ClickType getClickType() {
		return clickType;
	}

	public void setClickType(ClickType clickType) {
		this.clickType = clickType;
	}

	public String getAbility() {
		return ability;
	}

	public void setAbility(String ability) {
		this.ability = ability;
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

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public ArrayList<LivingEntity> getAffectedEntities() {
		return affectedEntities;
	}

	public ArrayList<FireComboStream> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<FireComboStream> tasks) {
		this.tasks = tasks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	// Combo subclasses need to be static to be reflectively called in ComboManager
	public static class FireKick extends FireComboLegacy {

		public FireKick(Player player) {
			super(player, "FireKick");
		}

		@Override
		public String getName() {
			return "FireKick";
		}

		@Override
		public boolean isCollidable() {
			return true;
		}

		@Override
		public void handleCollision(Collision collision) {
			handleCollisionFireStreams(collision);
		}

	}

	public static class FireSpin extends FireComboLegacy {

		public FireSpin(Player player) {
			super(player, "FireSpin");
		}

		@Override
		public String getName() {
			return "FireSpin";
		}

		@Override
		public boolean isCollidable() {
			return true;
		}

		@Override
		public void handleCollision(Collision collision) {
			handleCollisionFireStreams(collision);
		}

	}

	public static class FireWheel extends FireComboLegacy {

		public FireWheel(Player player) {
			super(player, "FireWheel");
		}

		@Override
		public String getName() {
			return "FireWheel";
		}

		@Override
		public boolean isCollidable() {
			return true;
		}

	}

	public static class JetBlast extends FireComboLegacy {

		public JetBlast(Player player) {
			super(player, "JetBlast");
		}

		@Override
		public String getName() {
			return "JetBlast";
		}

	}

	public static class JetBlaze extends FireComboLegacy {

		public JetBlaze(Player player) {
			super(player, "JetBlaze");
		}

		@Override
		public String getName() {
			return "JetBlaze";
		}

	}

}
