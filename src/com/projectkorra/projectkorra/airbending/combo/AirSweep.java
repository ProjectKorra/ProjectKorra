package com.projectkorra.projectkorra.airbending.combo;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.combo.FireCombo.FireComboStream;
import com.projectkorra.projectkorra.util.DamageHandler;

public class AirSweep extends AirAbility implements ComboAbility {

	private int progressCounter;
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.POWER)
	private double knockback;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;
	
	public AirSweep(Player player) {
		super(player);

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();


		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		this.damage = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Damage");
		this.range = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Range");
		this.speed = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Speed");
		this.knockback = getConfig().getDouble("Abilities.Air.AirCombo.AirSweep.Knockback");
		this.cooldown = getConfig().getLong("Abilities.Air.AirCombo.AirSweep.Cooldown");

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
			this.knockback = knockback * 1.4;
		}
		
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public String getName() {
		return "AirSweep";
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public void handleCollision(Collision collision) {
		if (collision.isRemovingFirst()) {
			ArrayList<BukkitRunnable> newTasks = new ArrayList<>();
			double collisionDistanceSquared = Math.pow(getCollisionRadius() + collision.getAbilitySecond().getCollisionRadius(), 2);
			// Remove all of the streams that are by this specific ourLocation.
			// Don't just do a single stream at a time or this algorithm becomes O(n^2) with
			// Collision's detection algorithm.
			for (BukkitRunnable task : getTasks()) {
				if (task instanceof FireComboStream) {
					FireComboStream stream = (FireComboStream) task;
					if (stream.getLocation().distanceSquared(collision.getLocationSecond()) > collisionDistanceSquared) {
						newTasks.add(stream);
					} else {
						stream.cancel();
					}
				} else {
					newTasks.add(task);
				}
			}
			setTasks(newTasks);
		}
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (BukkitRunnable task : getTasks()) {
			if (task instanceof FireComboStream) {
				FireComboStream stream = (FireComboStream) task;
				locations.add(stream.getLocation());
			}
		}
		return locations;
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
				Vector vec = GeneralMethods.getDirection(player.getLocation(), origin.clone().add(origToDest.clone().multiply(i / 30)));

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
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		for (BukkitRunnable task : tasks) {
			task.cancel();
		}
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
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return origin;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
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

	public int getProgressCounter() {
		return progressCounter;
	}

	public void setProgressCounter(int progressCounter) {
		this.progressCounter = progressCounter;
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

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}
}
