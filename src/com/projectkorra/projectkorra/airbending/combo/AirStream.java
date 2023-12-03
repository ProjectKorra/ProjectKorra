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
import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.region.RegionProtection;
import com.projectkorra.projectkorra.command.Commands;

public class AirStream extends AirAbility implements ComboAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long time;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute("EntityCarry" + Attribute.HEIGHT)
	private double airStreamMaxEntityHeight;
	@Attribute("EntityCarry" + Attribute.DURATION)
	private double airStreamEntityCarryDuration;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private ArrayList<Entity> affectedEntities;
	private ArrayList<BukkitRunnable> tasks;

	public AirStream(final Player player) {
		super(player);

		this.affectedEntities = new ArrayList<>();
		this.tasks = new ArrayList<>();

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.range = getConfig().getDouble("Abilities.Air.AirStream.Range");
		this.speed = getConfig().getDouble("Abilities.Air.AirStream.Speed");
		this.cooldown = getConfig().getLong("Abilities.Air.AirStream.Cooldown");
		this.airStreamMaxEntityHeight = getConfig().getDouble("Abilities.Air.AirStream.EntityCarry.Height");
		this.airStreamEntityCarryDuration = getConfig().getLong("Abilities.Air.AirStream.EntityCarry.Duration");

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.Range");
			this.airStreamMaxEntityHeight = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.EntityCarry.Height");
			this.airStreamEntityCarryDuration = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirStream.EntityCarry.Duration");
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
		} else if (this.currentLoc != null && RegionProtection.isRegionProtected(this, this.currentLoc)) {
			this.remove();
			return;
		}

		if (this.destination == null) {
			this.origin = this.player.getEyeLocation();
			this.currentLoc = this.origin.clone();
		}
		final Entity target = GeneralMethods.getTargetedEntity(this.player, this.range);

		if (target != null && target.getLocation().distanceSquared(this.currentLoc) > 49) {
			this.destination = target.getLocation();
		} else {
			this.destination = GeneralMethods.getTargetedLocation(this.player, this.range, getTransparentMaterials());
		}

		if (GeneralMethods.locationEqualsIgnoreDirection(this.currentLoc, this.destination)) {
			this.remove();
			return;
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
		} else if (RegionProtection.isRegionProtected(this, this.currentLoc)) {
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
			}
		}

		for (final Entity entity : this.affectedEntities) {
			if (RegionProtection.isRegionProtected(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
				continue;
			}
			final Vector force = GeneralMethods.getDirection(entity.getLocation(), this.currentLoc);
			GeneralMethods.setVelocity(this, entity, force.clone().normalize().multiply(this.speed));
			entity.setFallDistance(0F);
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (final BukkitRunnable task : this.tasks) {
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
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Air.AirStream.Combination"));
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

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
}
