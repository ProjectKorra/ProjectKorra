package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class AirBurst extends AirAbility {

	private static final double CONE_BURST_ANGLE = Math.toRadians(30);

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
		AirBurst existing = getAbility(player, getClass());
		if (existing != null && !existing.isCharged) {
			return;
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
            }
            this.remove();
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
		for (Vector direction : burstDirections(75, 104)) {
			new AirBlast(this.player, location, direction.normalize(), this.pushFactor, this).setDamage(this.damage);
		}
	}

	public static void coneBurst(final Player player) {
		final AirBurst airBurst = getAbility(player, AirBurst.class);
		if (airBurst != null && airBurst.isCharged) {
			airBurst.bPlayer.addCooldown(airBurst);
			airBurst.startConeBurst();
			airBurst.remove();
		}
	}

	private void startConeBurst() {
		final Location location = this.player.getEyeLocation();
		final Vector vector = location.getDirection();
		for (Vector direction : burstDirections(0, 180)) {
			if (direction.angle(vector) <= CONE_BURST_ANGLE) {
				final AirBlast blast = new AirBlast(this.player, location, direction.normalize(), this.pushFactor, this);
				blast.setDamage(this.damage);
			}
		}
	}

	private void sphereBurst() {
		final Location location = this.player.getEyeLocation();
		for (Vector direction : burstDirections(0, 180)) {
			final AirBlast blast = new AirBlast(this.player, location, direction.normalize(), this.pushFactor, this);
			blast.setDamage(this.damage);
			blast.setShowParticles(false);
			this.blasts.add(blast);
		}
		this.handleSmoothParticles();
	}

	public void handleSmoothParticles() {
		for (int i = 0; i < this.blasts.size(); i++) {
			final AirBlast blast = this.blasts.get(i);
			int toggleTime = i % 4 == 0 ? 0 : (int) (i % (100 / this.particlePercentage)) + 3;
			Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> blast.setShowParticles(true), toggleTime);
		}
	}

	private List<Vector> burstDirections(double startTheta, double maxTheta) {
		// TODO: Make a cache for this maybe? Might be overkill
		List<Vector> directions = new ArrayList<>();
		for (double theta = startTheta; theta <= maxTheta; theta += this.blastAngleTheta) {
			final double dphi = this.blastAnglePhi / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				final double rphi = Math.toRadians(phi);
				final double rtheta = Math.toRadians(theta);

				double x = Math.cos(rphi) * Math.sin(rtheta);
				double y = Math.sin(rphi) * Math.sin(rtheta);
				double z = Math.cos(rtheta);
				directions.add(new Vector(x, z, y));
			}
		}
		return directions;
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
		final List<Location> locations = new ArrayList<>();
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
