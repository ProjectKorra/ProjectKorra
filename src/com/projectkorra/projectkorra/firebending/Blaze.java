package com.projectkorra.projectkorra.firebending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;

public class Blaze extends FireAbility {

	@Attribute("Arc")
	private int arc;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.SPEED)
	private double speed;

	public Blaze(final Player player) {
		super(player);

		this.speed = 2;
		this.cooldown = getConfig().getLong("Abilities.Fire.Blaze.Cooldown");
		this.arc = getConfig().getInt("Abilities.Fire.Blaze.Arc");
		this.range = getConfig().getDouble("Abilities.Fire.Blaze.Range");

		if (!this.bPlayer.canBend(this) || this.bPlayer.isOnCooldown("BlazeArc")) {
			return;
		}

		this.range = this.getDayFactor(this.range);
		this.range = AvatarState.getValue(this.range, player);
		this.arc = (int) this.getDayFactor(this.arc);
		final Location location = player.getLocation();

		for (int i = -this.arc; i <= this.arc; i += this.speed) {
			final double angle = Math.toRadians(i);
			final Vector direction = player.getEyeLocation().getDirection().clone();
			double x, z, vx, vz;

			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			new BlazeArc(player, location, direction, this.range);
		}

		this.start();
		this.bPlayer.addCooldown("BlazeArc", this.cooldown);
		this.remove();
	}

	@Override
	public String getName() {
		return "Blaze";
	}

	@Override
	public void progress() {}

	@Override
	public Location getLocation() {
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

}
