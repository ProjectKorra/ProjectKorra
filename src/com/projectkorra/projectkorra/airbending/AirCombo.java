package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.firebending.FireCombo;
import com.projectkorra.projectkorra.firebending.FireCombo.FireComboStream;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/*
 * TODO: Combo classes should eventually be rewritten so that each combo is treated
 * as an individual ability. In the mean time, we will just place "fake"
 * classes so that CoreAbility will register each ability. 
 */
public class AirCombo extends AirAbility implements ComboAbility {

	public static enum AbilityState {
		TWISTER_MOVING, TWISTER_STATIONARY
	}

	private int progressCounter;
	private long cooldown;
	private long time;
	private double damage;
	private double speed;
	private double range;
	private double knockback;
	private double airStreamMaxEntityHeight;
	private double airStreamEntityCarryDuration;
	private double twisterHeight;
	private double twisterRadius;
	private double twisterDegreeParticles;
	private double twisterHeightParticles;
	private double twisterRemoveDelay;
	private AbilityState state;
	private String abilityName;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;
	private ArrayList<Flight> flights;

	public AirCombo(Player player, String ability) {
		super(player);
		
		this.abilityName = ability;
		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();
		this.flights = new ArrayList<>();

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (bPlayer.isOnCooldown(ability)) {
			return;
		}

		if (ability.equalsIgnoreCase("Twister")) {
			this.range = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Range");
			this.speed = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Speed");
			this.cooldown = getConfig().getLong("Abilities.Air.AirCombo.Twister.Cooldown");
			this.twisterHeight = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Height");
			this.twisterRadius = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Radius");
			this.twisterDegreeParticles = getConfig().getDouble("Abilities.Air.AirCombo.Twister.DegreesPerParticle");
			this.twisterHeightParticles = getConfig().getDouble("Abilities.Air.AirCombo.Twister.HeightPerParticle");
			this.twisterRemoveDelay = getConfig().getLong("Abilities.Air.AirCombo.Twister.RemoveDelay");
		} else if (ability.equalsIgnoreCase("AirStream")) {
			this.range = getConfig().getDouble("Abilities.Air.AirCombo.AirStream.Range");
			this.speed = getConfig().getDouble("Abilities.Air.AirCombo.AirStream.Speed");
			this.cooldown = getConfig().getLong("Abilities.Air.AirCombo.AirStream.Cooldown");
			this.airStreamMaxEntityHeight = getConfig().getDouble("Abilities.Air.AirCombo.AirStream.EntityHeight");
			this.airStreamEntityCarryDuration = getConfig().getLong("Abilities.Air.AirCombo.AirStream.EntityDuration");
		} else if (ability.equalsIgnoreCase("AirSweep")) {
			this.damage = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Damage");
			this.range = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Range");
			this.speed = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Speed");
			this.knockback = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Knockback");
			this.cooldown = getConfig().getLong("Abilities.Air.AirCombo.AirSweep.Cooldown");
		}
		
		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
			this.knockback = knockback * 1.4;
			this.airStreamMaxEntityHeight = AvatarState.getValue(airStreamMaxEntityHeight);
			this.airStreamEntityCarryDuration = AvatarState.getValue(airStreamEntityCarryDuration);
		}
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public void progress() {
		progressCounter++;
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (currentLoc != null && GeneralMethods.isRegionProtectedFromBuild(this, currentLoc)) {
			remove();
			return;
		}

		if (abilityName.equalsIgnoreCase("Twister")) {
			if (destination == null) {
				state = AbilityState.TWISTER_MOVING;
				direction = player.getEyeLocation().getDirection().clone().normalize();
				direction.setY(0);
				origin = player.getLocation().add(direction.clone().multiply(2));
				destination = player.getLocation().add(direction.clone().multiply(range));
				currentLoc = origin.clone();
			}
			if (origin.distanceSquared(currentLoc) < origin.distanceSquared(destination) && state == AbilityState.TWISTER_MOVING) {
				currentLoc.add(direction.clone().multiply(speed));
			} else if (state == AbilityState.TWISTER_MOVING) {
				state = AbilityState.TWISTER_STATIONARY;
				time = System.currentTimeMillis();
			} else if (System.currentTimeMillis() - time >= twisterRemoveDelay) {
				remove();
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, currentLoc)) {
				remove();
				return;
			}

			Block topBlock = GeneralMethods.getTopBlock(currentLoc, 3, -3);
			if (topBlock == null) {
				remove();
				return;
			}
			currentLoc.setY(topBlock.getLocation().getY());

			double height = twisterHeight;
			double radius = twisterRadius;
			for (double y = 0; y < height; y += twisterHeightParticles) {
				double animRadius = ((radius / height) * y);
				for (double i = -180; i <= 180; i += twisterDegreeParticles) {
					Vector animDir = GeneralMethods.rotateXZ(new Vector(1, 0, 1), i);
					Location animLoc = currentLoc.clone().add(animDir.multiply(animRadius));
					animLoc.add(0, y, 0);
					playAirbendingParticles(animLoc, 1, 0, 0, 0);
				}
			}
			playAirbendingSound(currentLoc);

			for (int i = 0; i < height; i += 3) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(currentLoc.clone().add(0, i, 0), radius * 0.75)) {
					if (!affectedEntities.contains(entity) && !entity.equals(player)) {
						affectedEntities.add(entity);
					}
				}
			}

			for (Entity entity : affectedEntities) {
				Vector forceDir = GeneralMethods.getDirection(entity.getLocation(), currentLoc.clone().add(0, height, 0));
				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName())) {
						break;
					}
				}
				entity.setVelocity(forceDir.clone().normalize().multiply(0.3));
			}
		} else if (abilityName.equalsIgnoreCase("AirStream")) {
			if (destination == null) {
				origin = player.getEyeLocation();
				currentLoc = origin.clone();
			}
			Entity target = GeneralMethods.getTargetedEntity(player, range);
			if (target instanceof Player) {
				if (Commands.invincible.contains(((Player) target).getName())) {
					return;
				}
			}

			if (target != null && target.getLocation().distanceSquared(currentLoc) > 49) {
				destination = target.getLocation();
			} else {
				destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterial());
			}

			direction = GeneralMethods.getDirection(currentLoc, destination).normalize();
			currentLoc.add(direction.clone().multiply(speed));
			
			if (player.getWorld() != currentLoc.getWorld()) {
				remove();
				return;
			} else if (!player.isSneaking()) {
				remove();
				return;
			} else if (Math.abs(player.getLocation().distanceSquared(currentLoc)) > range * range) {
				remove();
				return;
			} else if (affectedEntities.size() > 0 && System.currentTimeMillis() - time >= airStreamEntityCarryDuration) {
				remove();
				return;
			} else if (!isTransparent(currentLoc.getBlock())) {
				remove();
				return;
			} else if (currentLoc.getY() - origin.getY() > airStreamMaxEntityHeight) {
				remove();
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, currentLoc)) {
				remove();
				return;
			} else if (FireAbility.isWithinFireShield(currentLoc)) {
				remove();
				return;
			} else if (isWithinAirShield(currentLoc)) {
				remove();
				return;
			} else if (!isTransparent(currentLoc.getBlock())) {
				currentLoc.subtract(direction.clone().multiply(speed));
			}

			for (int i = 0; i < 10; i++) {
				BukkitRunnable br = new BukkitRunnable() {
					final Location loc = currentLoc.clone();
					final Vector dir = direction.clone();

					@Override
					public void run() {
						for (int angle = -180; angle <= 180; angle += 45) {
							Vector orthog = GeneralMethods.getOrthogonalVector(dir.clone(), angle, 0.5);
							playAirbendingParticles(loc.clone().add(orthog), 1, 0F, 0F, 0F);
						}
					}
				};
				br.runTaskLater(ProjectKorra.plugin, i * 2);
				tasks.add(br);
			}

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(currentLoc, 2.8)) {
				if (affectedEntities.size() == 0) {
					// Set the timer to remove the ability
					time = System.currentTimeMillis();
				}
				if (!entity.equals(player) && !affectedEntities.contains(entity)) {
					affectedEntities.add(entity);
					if (entity instanceof Player) {
						flights.add(new Flight((Player) entity, player));
					}
				}
			}

			for (Entity entity : affectedEntities) {
				Vector force = GeneralMethods.getDirection(entity.getLocation(), currentLoc);
				entity.setVelocity(force.clone().normalize().multiply(speed));
				entity.setFallDistance(0F);
			}
		} else if (abilityName.equalsIgnoreCase("AirSweep")) {
			if (origin == null) {
				direction = player.getEyeLocation().getDirection().normalize();
				origin = player.getLocation().add(direction.clone().multiply(10));
			}
			if (progressCounter < 8) {
				return;
			}
			if (destination == null) {
				destination = player.getLocation().add(player.getEyeLocation().getDirection().normalize().multiply(10));
				Vector origToDest = GeneralMethods.getDirection(origin, destination);
				for (double i = 0; i < 30; i++) {
					Vector vec = GeneralMethods.getDirection(player.getLocation(),
							origin.clone().add(origToDest.clone().multiply(i / 30)));

					FireComboStream fs = new FireComboStream(null, vec, player.getLocation(), range, speed, "AirSweep");
					fs.setDensity(1);
					fs.setSpread(0F);
					fs.setUseNewParticles(true);
					fs.setParticleEffect(getAirbendingParticles());
					fs.setCollides(false);
					fs.runTaskTimer(ProjectKorra.plugin, (long) (i / 2.5), 1L);
					tasks.add(fs);
				}
			}
			manageAirVectors();
		}
	}

	public void manageAirVectors() {
		for (int i = 0; i < tasks.size(); i++) {
			if (((FireComboStream) tasks.get(i)).isCancelled()) {
				tasks.remove(i);
				i--;
			}
		}
		if (tasks.size() == 0) {
			remove();
			return;
		}
		for (int i = 0; i < tasks.size(); i++) {
			FireComboStream fstream = (FireComboStream) tasks.get(i);
			Location loc = fstream.getLocation();

			if (GeneralMethods.isRegionProtectedFromBuild(this, loc)) {
				fstream.remove();
				return;
			}
			
			if (!isTransparent(loc.getBlock())) {
				if (!isTransparent(loc.clone().add(0, 0.2, 0).getBlock())) {
					fstream.remove();
					return;
				}
			}
			if (i % 3 == 0) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 2.5)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
						remove();
						return;
					}
					if (!entity.equals(player) && !affectedEntities.contains(entity)) {
						affectedEntities.add(entity);
						if (knockback != 0) {
							Vector force = fstream.getDirection();
							entity.setVelocity(force.multiply(knockback));
						}
						if (damage != 0) {
							if (entity instanceof LivingEntity) {
								if (fstream.getAbility().equalsIgnoreCase("AirSweep")) {
									DamageHandler.damageEntity(entity, damage, this);
								} else {
									DamageHandler.damageEntity(entity, damage, this);
								}
							}
						}
					}
				}

				if (GeneralMethods.blockAbilities(player, FireCombo.getBlockableAbilities(), loc, 1)) {
					fstream.remove();
				} else AirAbility.removeAirSpouts(loc, player);
				WaterAbility.removeWaterSpouts(loc, player);
				EarthAbility.removeSandSpouts(loc, player);
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (BukkitRunnable task : tasks) {
			task.cancel();
		}
		for (Flight flight : flights) {
			flight.revert();
			flight.remove();
		}
	}

	public static boolean removeAroundPoint(Player player, String ability, Location loc, double radius) {
		boolean removed = false;
		for (AirCombo combo : getAbilities(AirCombo.class)) {
			if (combo.getPlayer().equals(player)) {
				continue;
			} else if (ability.equalsIgnoreCase("Twister") && combo.abilityName.equalsIgnoreCase("Twister")) {
				if (combo.currentLoc != null && Math.abs(combo.currentLoc.distance(loc)) <= radius) {
					combo.remove();
					removed = true;
				}
			} else if (ability.equalsIgnoreCase("AirStream") && combo.abilityName.equalsIgnoreCase("AirStream")) {
				if (combo.currentLoc != null && Math.abs(combo.currentLoc.distance(loc)) <= radius) {
					combo.remove();
					removed = true;
				}
			} else if (ability.equalsIgnoreCase("AirSweep") && combo.abilityName.equalsIgnoreCase("AirSweep")) {
				for (int j = 0; j < combo.tasks.size(); j++) {
					FireComboStream fs = (FireComboStream) combo.tasks.get(j);
					if (fs.getLocation() != null && fs.getLocation().getWorld().equals(loc.getWorld())
							&& Math.abs(fs.getLocation().distance(loc)) <= radius) {
						fs.remove();
						removed = true;
					}
				}
			}
		}
		return removed;
	}

	@Override
	public String getName() {
		return abilityName == null ? "AirCombo" : abilityName;
	}

	@Override
	public Location getLocation() {
		if (currentLoc != null) {
			return currentLoc;
		} else if (origin != null) {
			return origin;
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
	public boolean isHiddenAbility() {
		return true;
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

	
	public String getAbilityName() {
		return abilityName;
	}

	public void setAbilityName(String abilityName) {
		this.abilityName = abilityName;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Location getCurrentLoc() {
		return currentLoc;
	}

	public void setCurrentLoc(Location currentLoc) {
		this.currentLoc = currentLoc;
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

	public AbilityState getState() {
		return state;
	}

	public void setState(AbilityState state) {
		this.state = state;
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

	public double getAirStreamMaxEntityHeight() {
		return airStreamMaxEntityHeight;
	}

	public void setAirStreamMaxEntityHeight(double airStreamMaxEntityHeight) {
		this.airStreamMaxEntityHeight = airStreamMaxEntityHeight;
	}

	public double getAirStreamEntityCarryDuration() {
		return airStreamEntityCarryDuration;
	}

	public void setAirStreamEntityCarryDuration(double airStreamEntityCarryDuration) {
		this.airStreamEntityCarryDuration = airStreamEntityCarryDuration;
	}

	public double getTwisterHeight() {
		return twisterHeight;
	}

	public void setTwisterHeight(double twisterHeight) {
		this.twisterHeight = twisterHeight;
	}

	public double getTwisterRadius() {
		return twisterRadius;
	}

	public void setTwisterRadius(double twisterRadius) {
		this.twisterRadius = twisterRadius;
	}

	public double getTwisterDegreeParticles() {
		return twisterDegreeParticles;
	}

	public void setTwisterDegreeParticles(double twisterDegreeParticles) {
		this.twisterDegreeParticles = twisterDegreeParticles;
	}

	public double getTwisterHeightParticles() {
		return twisterHeightParticles;
	}

	public void setTwisterHeightParticles(double twisterHeightParticles) {
		this.twisterHeightParticles = twisterHeightParticles;
	}

	public double getTwisterRemoveDelay() {
		return twisterRemoveDelay;
	}

	public void setTwisterRemoveDelay(double twisterRemoveDelay) {
		this.twisterRemoveDelay = twisterRemoveDelay;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public ArrayList<Flight> getFlights() {
		return flights;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
	public class AirStream extends AirCombo {

		public AirStream(Player player, String name) {
			super(player, "AirStream");
		}
		
		@Override
		public String getName() {
			return "AirStream";
		}
		
	}
	
	public class AirSweep extends AirCombo {

		public AirSweep(Player player, String name) {
			super(player, "AirSweep");
		}
		
		@Override
		public String getName() {
			return "AirSweep";
		}
		
	}
	
	public class Twister extends AirCombo {

		public Twister(Player player, String name) {
			super(player, "Twister");
		}
		
		@Override
		public String getName() {
			return "Twister";
		}
		
	}
		
}
