package com.projectkorra.projectkorra.airbending.combo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.Manager;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.FlightHandler;

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
	private Set<Player> flights;

	public AirStream(final Player player) {
		super(player);

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();
		this.flights = new HashSet<>();

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.range = getConfig().getDouble("Abilities.Air.AirStream.Range");
		this.speed = getConfig().getDouble("Abilities.Air.AirStream.Speed");
		this.cooldown = getConfig().getLong("Abilities.Air.AirStream.Cooldown");
		this.airStreamMaxEntityHeight = getConfig().getDouble("Abilities.Air.AirStream.EntityHeight");
		this.airStreamEntityCarryDuration = getConfig().getLong("Abilities.Air.AirStream.EntityDuration");

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.Range");
			this.airStreamMaxEntityHeight = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.EntityHeight");
			this.airStreamEntityCarryDuration = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.EntityDuration");
		}

		this.bPlayer.addCooldown(this);
		this.start();
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
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		} else if (this.currentLoc != null && GeneralMethods.isRegionProtectedFromBuild(this, this.currentLoc)) {
			this.remove();
			return;
		}

		if (this.destination == null) {
			this.origin = this.player.getEyeLocation();
			this.currentLoc = this.origin.clone();
		}
		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range);
		if (target instanceof Player) {
			if (Commands.invincible.contains(((Player) target).getName())) {
				return;
			}
		}

		if (target != null && target.getLocation().distanceSquared(this.currentLoc) > 49) {
			this.destination = target.getLocation();
		} else {
			this.destination = GeneralMethods.getTargetedLocation(this.player, this.range, getTransparentMaterials());
		}

		this.direction = GeneralMethods.getDirection(this.currentLoc, this.destination).normalize();
		this.currentLoc.add(this.direction.clone().multiply(this.speed));

		if (this.player.getWorld() != this.currentLoc.getWorld()) {
			this.remove();
			return;
		} else if (!this.player.isSneaking()) {
			this.remove();
			return;
		} else if (this.player.getWorld().equals(this.currentLoc.getWorld()) && Math.abs(this.player.getLocation().distanceSquared(this.currentLoc)) > this.range * this.range) {
			this.remove();
			return;
		} else if (this.affectedEntities.size() > 0 && System.currentTimeMillis() - this.time >= this.airStreamEntityCarryDuration) {
			this.remove();
			return;
		} else if (!this.isTransparent(this.currentLoc.getBlock())) {
			this.remove();
			return;
		} else if (this.currentLoc.getY() - this.origin.getY() > this.airStreamMaxEntityHeight) {
			this.remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.currentLoc)) {
			this.remove();
			return;
		} else if (!this.isTransparent(this.currentLoc.getBlock())) {
			this.currentLoc.subtract(this.direction.clone().multiply(this.speed));
		}

		for (int i = 0; i < 10; i++) {
			final BukkitRunnable br = new BukkitRunnable() {
				final Location loc = AirStream.this.currentLoc.clone();
				final Vector dir = AirStream.this.direction.clone();

				@Override
				public void run() {
					for (int angle = -180; angle <= 180; angle += 45) {
						final Vector orthog = GeneralMethods.getOrthogonalVector(this.dir.clone(), angle, 0.5);
						playAirbendingParticles(this.loc.clone().add(orthog), 1, 0F, 0F, 0F);
					}
				}
			};
			br.runTaskLater(ProjectKorra.plugin, i * 2);
			this.tasks.add(br);
		}

		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.currentLoc, 2.8)) {
			if (this.affectedEntities.size() == 0) {
				// Set the timer to remove the ability.
				this.time = System.currentTimeMillis();
			}
			if (!entity.equals(this.player) && !this.affectedEntities.contains(entity)) {
				this.affectedEntities.add(entity);
				if (entity instanceof Player) {
					final Player ep = (Player) entity;
					Manager.getManager(FlightHandler.class).createInstance(ep, this.player, this.getName());
					this.flights.add(ep);
				}
			}
		}

		for (final Entity entity : this.affectedEntities) {
			final Vector force = GeneralMethods.getDirection(entity.getLocation(), this.currentLoc);
			entity.setVelocity(force.clone().normalize().multiply(this.speed));
			entity.setFallDistance(0F);
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (final BukkitRunnable task : this.tasks) {
			task.cancel();
		}
		for (final Player flyer : this.flights) {
			Manager.getManager(FlightHandler.class).removeInstance(flyer, this.getName());
		}
		this.flights.clear();
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
		return this.cooldown;
	}

	@Override
	public Location getLocation() {
		return this.currentLoc;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new AirStream(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> airStream = new ArrayList<>();
		airStream.add(new AbilityInformation("AirShield", ClickType.SHIFT_DOWN));
		airStream.add(new AbilityInformation("AirSuction", ClickType.LEFT_CLICK));
		airStream.add(new AbilityInformation("AirBlast", ClickType.LEFT_CLICK));
		return airStream;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Location getCurrentLoc() {
		return this.currentLoc;
	}

	public void setCurrentLoc(final Location currentLoc) {
		this.currentLoc = currentLoc;
	}

	public Location getDestination() {
		return this.destination;
	}

	public void setDestination(final Location destination) {
		this.destination = destination;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public double getAirStreamMaxEntityHeight() {
		return this.airStreamMaxEntityHeight;
	}

	public void setAirStreamMaxEntityHeight(final double airStreamMaxEntityHeight) {
		this.airStreamMaxEntityHeight = airStreamMaxEntityHeight;
	}

	public double getAirStreamEntityCarryDuration() {
		return this.airStreamEntityCarryDuration;
	}

	public void setAirStreamEntityCarryDuration(final double airStreamEntityCarryDuration) {
		this.airStreamEntityCarryDuration = airStreamEntityCarryDuration;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return this.affectedEntities;
	}

	public ArrayList<BukkitRunnable> getTasks() {
		return this.tasks;
	}

	public void setTasks(final ArrayList<BukkitRunnable> tasks) {
		this.tasks = tasks;
	}

	public Set<Player> getFlights() {
		return this.flights;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public String getInstructions() {
		return "AirShield (Hold Shift) > AirSuction (Left Click) > AirBlast (Left Click)";
	}
}
