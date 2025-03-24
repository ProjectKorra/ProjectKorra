package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class AirBurst extends AirAbility {

	private boolean isCharged;
	private boolean isFallBurst;
	private int sneakParticles;
	private float playerFallDistance;
	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double fallThreshold;
	@Attribute(Attribute.KNOCKBACK)
	private double pushFactor;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private double blastAngleTheta;
	private double blastAnglePhi;
	private double particlePercentage;
	private ArrayList<AirBlast> blasts;
	private ArrayList<Entity> affectedEntities;

	public AirBurst(final Player player, final boolean isFallBurst) {
		super(player);
		if (this.bPlayer.isOnCooldown(this)) {
			this.remove();
			return;
		}
		if (hasAbility(player, AirBurst.class)) {
			if (!getAbility(player, AirBurst.class).isCharged()) {
				return;
			}
		}

		this.isFallBurst = isFallBurst;
		this.isCharged = false;
		this.playerFallDistance = player.getFallDistance();
		this.chargeTime = getConfig().getLong("Abilities.Air.AirBurst.ChargeTime");
		this.cooldown = getConfig().getLong("Abilities.Air.AirBurst.Cooldown");
		this.fallThreshold = getConfig().getDouble("Abilities.Air.AirBurst.FallThreshold");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirBurst.PushFactor");
		this.damage = getConfig().getDouble("Abilities.Air.AirBurst.Damage");
		this.blastAnglePhi = getConfig().getDouble("Abilities.Air.AirBurst.AnglePhi");
		this.blastAngleTheta = getConfig().getDouble("Abilities.Air.AirBurst.AngleTheta");
		this.sneakParticles = getConfig().getInt("Abilities.Air.AirBurst.SneakParticles");
		this.particlePercentage = getConfig().getDouble("Abilities.Air.AirBurst.ParticlePercentage");
		this.blasts = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();

		this.start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBend(this)) {
			this.remove();
			return;
		}

		if (this.isFallBurst) {
			if (this.playerFallDistance >= this.fallThreshold) {
				this.fallBurst();
				this.bPlayer.addCooldown(this);
			}
			this.remove();
			return;
		}

		if (System.currentTimeMillis() > this.getStartTime() + this.chargeTime && !this.isCharged) {
			this.isCharged = true;
		}

		if (!this.player.isSneaking()) {
			if (this.isCharged) {
				this.bPlayer.addCooldown(this);
				this.sphereBurst();
				this.remove();
				return;
			} else {
				this.remove();
				return;
			}
		} else if (this.isCharged) {
			final Location location = this.player.getEyeLocation();
			playAirbendingParticles(location, this.sneakParticles);
		}
	}

	private void fallBurst() {
		if (this.bPlayer.isOnCooldown("AirBurst")) {
			return;
		}

		final Location location = this.player.getLocation();
		double x, y, z;
		final double r = 1;

		for (double theta = 75; theta < 105; theta += this.blastAngleTheta) {
			final double dphi = this.blastAnglePhi / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				final double rphi = Math.toRadians(phi);
				final double rtheta = Math.toRadians(theta);

				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);

				final Vector direction = new Vector(x, z, y);
				final AirBlast blast = new AirBlast(this.player, location, direction.normalize(), this.pushFactor, this);
				blast.setDamage(this.damage);
			}
		}
	}

	public static void coneBurst(final Player player) {
		if (hasAbility(player, AirBurst.class)) {
			final AirBurst airBurst = getAbility(player, AirBurst.class);
			if (airBurst.isCharged) {
				airBurst.bPlayer.addCooldown(airBurst);
				airBurst.startConeBurst();
				airBurst.remove();
			}
		}
	}

	private void startConeBurst() {
		if (this.isCharged) {
			final Location location = this.player.getEyeLocation();
			final Vector vector = location.getDirection();
			final double angle = Math.toRadians(30);
			double x, y, z;
			final double r = 1;

			for (double theta = 0; theta <= 180; theta += this.blastAngleTheta) {
				final double dphi = this.blastAnglePhi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					final double rphi = Math.toRadians(phi);
					final double rtheta = Math.toRadians(theta);

					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);

					final Vector direction = new Vector(x, z, y);
					if (direction.angle(vector) <= angle) {
						final AirBlast blast = new AirBlast(this.player, location, direction.normalize(), this.pushFactor, this);
						blast.setDamage(this.damage);
					}
				}
			}
		}
	}

	public void handleSmoothParticles() {
		for (int i = 0; i < this.blasts.size(); i++) {
			final AirBlast blast = this.blasts.get(i);
			int toggleTime = 0;

			if (i % 4 != 0) {
				toggleTime = (int) (i % (100 / this.particlePercentage)) + 3;
			}
			new BukkitRunnable() {
				@Override
				public void run() {
					blast.setShowParticles(true);
				}
			}.runTaskLater(ProjectKorra.plugin, toggleTime);
		}
	}

	private void sphereBurst() {
		if (this.isCharged) {
			final Location location = this.player.getEyeLocation();
			double x, y, z;
			final double r = 1;

			for (double theta = 0; theta <= 180; theta += this.blastAngleTheta) {
				final double dphi = this.blastAnglePhi / Math.sin(Math.toRadians(theta));

				for (double phi = 0; phi < 360; phi += dphi) {
					final double rphi = Math.toRadians(phi);
					final double rtheta = Math.toRadians(theta);

					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);

					final Vector direction = new Vector(x, z, y);
					final AirBlast blast = new AirBlast(this.player, location, direction.normalize(), this.pushFactor, this);

					blast.setDamage(this.damage);
					blast.setShowParticles(false);
					this.blasts.add(blast);
				}
			}
		}
		this.handleSmoothParticles();
	}

	@Override
	public String getName() {
		return "AirBurst";
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

	@Override
	public List<Location> getLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (final AirBlast blast : this.blasts) {
			locations.add(blast.getLocation());
		}
		return locations;
	}

	public void addAffectedEntity(final Entity entity) {
		this.affectedEntities.add(entity);
	}

	public boolean isAffectedEntity(final Entity entity) {
		return this.affectedEntities.contains(entity);
	}

	public long getChargeTime() {
		return this.chargeTime;
	}

	public void setChargeTime(final long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getFallThreshold() {
		return this.fallThreshold;
	}

	public void setFallThreshold(final double fallThreshold) {
		this.fallThreshold = fallThreshold;
	}

	public double getPushFactor() {
		return this.pushFactor;
	}

	public void setPushFactor(final double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public double getBlastAngleTheta() {
		return this.blastAngleTheta;
	}

	public void setBlastAngleTheta(final double blastAngleTheta) {
		this.blastAngleTheta = blastAngleTheta;
	}

	public double getBlastAnglePhi() {
		return this.blastAnglePhi;
	}

	public void setBlastAnglePhi(final double blastAnglePhi) {
		this.blastAnglePhi = blastAnglePhi;
	}

	public boolean isCharged() {
		return this.isCharged;
	}

	public void setCharged(final boolean isCharged) {
		this.isCharged = isCharged;
	}

	public boolean isFallBurst() {
		return this.isFallBurst;
	}

	public void setFallBurst(final boolean isFallBurst) {
		this.isFallBurst = isFallBurst;
	}

	public ArrayList<AirBlast> getBlasts() {
		return this.blasts;
	}
}
