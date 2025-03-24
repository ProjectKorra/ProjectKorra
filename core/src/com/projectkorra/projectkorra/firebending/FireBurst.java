package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.List;

import com.projectkorra.projectkorra.attribute.markers.DayNightFactor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class FireBurst extends FireAbility {

	@Attribute("Charged")
	private boolean charged;
	@Attribute(Attribute.DAMAGE) @DayNightFactor
	private double damage;
	@Attribute(Attribute.CHARGE_DURATION) @DayNightFactor(invert = true)
	private long chargeTime;
	@Attribute(Attribute.RANGE) @DayNightFactor
	private double range;
	@Attribute(Attribute.COOLDOWN) @DayNightFactor(invert = true)
	private long cooldown;
	private double angleTheta;
	private double anglePhi;
	private double particlesPercentage;
	private ArrayList<FireBlast> blasts;

	public FireBurst(final Player player) {
		super(player);

		this.charged = false;
		this.damage = getConfig().getDouble("Abilities.Fire.FireBurst.Damage");
		this.chargeTime = (long) getConfig().getLong("Abilities.Fire.FireBurst.ChargeTime");
		this.range = getConfig().getDouble("Abilities.Fire.FireBurst.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireBurst.Cooldown");
		this.angleTheta = getConfig().getDouble("Abilities.Fire.FireBurst.AngleTheta");
		this.anglePhi = getConfig().getDouble("Abilities.Fire.FireBurst.AnglePhi");
		this.particlesPercentage = getConfig().getDouble("Abilities.Fire.FireBurst.ParticlesPercentage");
		this.blasts = new ArrayList<>();

		if (!this.bPlayer.canBend(this) || hasAbility(player, FireBurst.class)) {
			return;
		}

		this.start();
	}

	public static void coneBurst(final Player player) {
		final FireBurst burst = getAbility(player, FireBurst.class);
		if (burst != null && burst.charged) {
			burst.coneBurst();
		}
	}

	private void coneBurst() {
		if (this.charged) {
			final Location location = this.player.getEyeLocation();
			final List<Block> safeBlocks = GeneralMethods.getBlocksAroundPoint(this.player.getLocation(), 2);
			final Vector vector = location.getDirection();

			final double angle = Math.toRadians(30);
			double x, y, z;
			final double r = 1;

			for (double theta = 0; theta <= 180; theta += this.angleTheta) {
				final double dphi = this.anglePhi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					final double rphi = Math.toRadians(phi);
					final double rtheta = Math.toRadians(theta);

					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					final Vector direction = new Vector(x, z, y);

					if (direction.angle(vector) <= angle) {
						final FireBlast fblast = new FireBlast(location, direction.normalize(), this.player, this.damage, safeBlocks);
						fblast.setRange(this.range);
						fblast.setFireBurst(true);
					}
				}
			}
			this.bPlayer.addCooldown(this);
		}
		this.remove();
	}

	/**
	 * To combat the sphere FireBurst lag we are only going to show a certain
	 * percentage of FireBurst particles at a time per tick. As the bursts
	 * spread out then we can show more at a time.
	 */
	public void handleSmoothParticles() {
		for (int i = 0; i < this.blasts.size(); i++) {
			final FireBlast fblast = this.blasts.get(i);
			final int toggleTime = (int) (i % (100.0 / this.particlesPercentage));
			new BukkitRunnable() {
				@Override
				public void run() {
					fblast.setShowParticles(true);
				}
			}.runTaskLater(ProjectKorra.plugin, toggleTime);
		}
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
				this.sphereBurst();
			} else {
				this.remove();
			}
		} else if (this.charged) {
			final Location location = this.player.getEyeLocation();
			location.add(location.getDirection());
			playFirebendingParticles(location, 1, .01, .01, .01);
			emitFirebendingLight(location);
		}
	}

	private void sphereBurst() {
		if (this.charged) {
			final Location location = this.player.getEyeLocation();
			final List<Block> safeblocks = GeneralMethods.getBlocksAroundPoint(this.player.getLocation(), 2);
			double x, y, z;
			final double r = 1;

			for (double theta = 0; theta <= 180; theta += this.angleTheta) {
				final double dphi = this.anglePhi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					final double rphi = Math.toRadians(phi);
					final double rtheta = Math.toRadians(theta);

					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);

					final Vector direction = new Vector(x, z, y);
					final FireBlast fblast = new FireBlast(location, direction.normalize(), this.player, this.damage, safeblocks);

					fblast.setRange(this.range);
					fblast.setShowParticles(false);
					fblast.setFireBurst(true);
					this.blasts.add(fblast);
				}
			}
			this.bPlayer.addCooldown(this);
		}
		this.remove();
		this.handleSmoothParticles();
	}

	@Override
	public String getName() {
		return "FireBurst";
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

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final int damage) {
		this.damage = damage;
	}

	public long getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getRange() {
		return this.range;
	}

	public void setRange(final long range) {
		this.range = range;
	}

	public double getAngleTheta() {
		return this.angleTheta;
	}

	public void setAngleTheta(final double angleTheta) {
		this.angleTheta = angleTheta;
	}

	public double getAnglePhi() {
		return this.anglePhi;
	}

	public void setAnglePhi(final double anglePhi) {
		this.anglePhi = anglePhi;
	}

	public double getParticlesPercentage() {
		return this.particlesPercentage;
	}

	public void setParticlesPercentage(final double particlesPercentage) {
		this.particlesPercentage = particlesPercentage;
	}

	public ArrayList<FireBlast> getBlasts() {
		return this.blasts;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
