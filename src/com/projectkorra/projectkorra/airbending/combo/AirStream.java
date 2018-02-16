package com.projectkorra.projectkorra.airbending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.Flight;

public class AirStream extends AirAbility implements ComboAbility {
	private long cooldown;
	private long time;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	private double airStreamMaxEntityHeight;
	private double airStreamEntityCarryDuration;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;
	private ArrayList<Flight> flights;
	
	public AirStream(Player player) {
		super(player);

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();
		this.flights = new ArrayList<>();

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		this.range = getConfig().getDouble("Abilities.Air.AirStream.Range");
		this.speed = getConfig().getDouble("Abilities.Air.AirStream.Speed");
		this.cooldown = getConfig().getLong("Abilities.Air.AirStream.Cooldown");
		this.airStreamMaxEntityHeight = getConfig().getDouble("Abilities.Air.AirStream.EntityHeight");
		this.airStreamEntityCarryDuration = getConfig().getLong("Abilities.Air.AirStream.EntityDuration");

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.Range");
			this.airStreamMaxEntityHeight = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.EntityHeight");
			this.airStreamEntityCarryDuration = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.EntityDuration");
		}
		
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public String getName() {
		return "AirStream";
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (currentLoc != null && GeneralMethods.isRegionProtectedFromBuild(this, currentLoc)) {
			remove();
			return;
		}
		
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
			destination = GeneralMethods.getTargetedLocation(player, range, getTransparentMaterials());
		}

		direction = GeneralMethods.getDirection(currentLoc, destination).normalize();
		currentLoc.add(direction.clone().multiply(speed));

		if (player.getWorld() != currentLoc.getWorld()) {
			remove();
			return;
		} else if (!player.isSneaking()) {
			remove();
			return;
		} else if (player.getWorld().equals(currentLoc.getWorld()) && Math.abs(player.getLocation().distanceSquared(currentLoc)) > range * range) {
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
			if (!entity.equals(player) && !affectedEntities.contains(entity) && !GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
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

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return currentLoc;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new AirStream(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> airStream = new ArrayList<>();
		airStream.add(new AbilityInformation("AirShield", ClickType.SHIFT_DOWN));
		airStream.add(new AbilityInformation("AirSuction", ClickType.LEFT_CLICK));
		airStream.add(new AbilityInformation("AirBlast", ClickType.LEFT_CLICK));
		return airStream;
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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
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

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return tasks;
	}

	public void setTasks(ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}

	public ArrayList<Flight> getFlights() {
		return flights;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
	@Override
	public String getInstructions() {
		return "AirShield (Hold Shift) > AirSuction (Left Click) > AirBlast (Left Click)";
	}
}
