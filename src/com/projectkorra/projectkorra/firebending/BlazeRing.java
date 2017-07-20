package com.projectkorra.projectkorra.firebending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.FireAbility;

public class BlazeRing extends FireAbility {

	private int range;
	private long cooldown;
	private double angleIncrement;
	private Location location;

	public BlazeRing(Player player) {
		super(player);

		this.range = getConfig().getInt("Abilities.Fire.Blaze.Ring.Range");
		this.angleIncrement = getConfig().getDouble("Abilities.Fire.Blaze.Ring.Angle");
		this.cooldown = getConfig().getLong("Abilities.Fire.Blaze.Ring.Cooldown");
		this.location = player.getLocation();

		if (bPlayer.isAvatarState()) {
			range = getConfig().getInt("Abilities.Avatar.AvatarState.Fire.Blaze.Ring.Range");
		}
		if (!bPlayer.canBend(this) || bPlayer.isOnCooldown("BlazeRing")) {
			return;
		}

		for (double degrees = 0; degrees < 360; degrees += angleIncrement) {
			double angle = Math.toRadians(degrees);
			Vector direction = player.getEyeLocation().getDirection().clone();
			double x, z, vx, vz;

			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			new BlazeArc(player, location, direction, range);
		}

		start();
		bPlayer.addCooldown("BlazeRing", cooldown);
		remove();
	}

	@Override
	public String getName() {
		return "Blaze";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return location;
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

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public double getAngleIncrement() {
		return angleIncrement;
	}

	public void setAngleIncrement(double angleIncrement) {
		this.angleIncrement = angleIncrement;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}
