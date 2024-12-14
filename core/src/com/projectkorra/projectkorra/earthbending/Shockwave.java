package com.projectkorra.projectkorra.earthbending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class Shockwave extends EarthAbility {

	private boolean charged;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double angle;
	private double threshold;
	@Attribute(Attribute.RANGE)
	private double range;

	public Shockwave(final Player player, final boolean fall) {
		super(player);

		this.angle = Math.toRadians(getConfig().getDouble("Abilities.Earth.Shockwave.Angle"));
		this.cooldown = getConfig().getLong("Abilities.Earth.Shockwave.Cooldown");
		this.chargeTime = getConfig().getLong("Abilities.Earth.Shockwave.ChargeTime");
		this.threshold = getConfig().getDouble("Abilities.Earth.Shockwave.FallThreshold");
		this.range = getConfig().getDouble("Abilities.Earth.Shockwave.Range");

		if (!this.bPlayer.canBend(this) || hasAbility(player, Shockwave.class)) {
			return;
		}

		if (fall) {
			this.fallShockwave();
			return;
		}

		this.start();
	}

	public void fallShockwave() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			return;
		} else if (this.player.getFallDistance() < this.threshold || !this.isEarthbendable(this.player.getLocation().clone().subtract(0, 1, 0).getBlock())) {
			return;
		} else if (this.bPlayer.isOnCooldown("Shockwave")) {
			return;
		}

		this.areaShockwave();
		this.bPlayer.addCooldown(this);
		this.remove();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() > this.getStartTime() + this.chargeTime && !this.charged) {
			this.charged = true;
		}

		if (!this.player.isSneaking()) {
			if (this.charged) {
				this.areaShockwave();
				this.remove();
				return;
			} else {
				this.remove();
				return;
			}
		} else if (this.charged) {
			final Location location = this.player.getEyeLocation().add(this.player.getEyeLocation().getDirection());
			ParticleEffect.SMOKE_NORMAL.display(location, 1);
		}
	}

	public static void progressAll() {
		Ripple.progressAllCleanup();
	}

	public void areaShockwave() {
		final double dtheta = 360.0 / (2 * Math.PI * this.range) - 1;
		for (double theta = 0; theta < 360; theta += dtheta) {
			final double rtheta = Math.toRadians(theta);
			final Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
			new Ripple(this.player, vector.normalize());
		}
		this.bPlayer.addCooldown(this);
	}

	public static void coneShockwave(final Player player) {
		final Shockwave shockWave = getAbility(player, Shockwave.class);
		if (shockWave != null) {
			if (shockWave.charged) {
				final double dtheta = 360.0 / (2 * Math.PI * shockWave.range) - 1;

				for (double theta = 0; theta < 360; theta += dtheta) {
					final double rtheta = Math.toRadians(theta);
					final Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
					if (vector.angle(player.getEyeLocation().getDirection()) < shockWave.angle) {
						new Ripple(player, vector.normalize());
					}
				}
				shockWave.bPlayer.addCooldown(shockWave);
				shockWave.remove();
			}
		}
	}

	@Override
	public String getName() {
		return "Shockwave";
	}

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

	public boolean isCharged() {
		return this.charged;
	}

	public void setCharged(final boolean charged) {
		this.charged = charged;
	}

	public long getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getAngle() {
		return this.angle;
	}

	public void setAngle(final double angle) {
		this.angle = angle;
	}

	public double getThreshold() {
		return this.threshold;
	}

	public void setThreshold(final double threshold) {
		this.threshold = threshold;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final double range) {
		this.range = range;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
