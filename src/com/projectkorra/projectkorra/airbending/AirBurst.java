package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class AirBurst extends AirAbility {

	private boolean isCharged;
	private boolean isFallBurst;
	private int sneakParticles;
	private float playerFallDistance;
	private long chargeTime;
	private double fallThreshold;
	private double pushFactor;
	private double damage;
	private double blastAngleTheta;
	private double blastAnglePhi;
	private double particlePercentage;
	private ArrayList<AirBlast> blasts;
	private ArrayList<Entity> affectedEntities;
	
	public AirBurst(Player player, boolean isFallBurst) {
		super(player);
		if (bPlayer.isOnCooldown(this)) {
			remove();
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
		this.fallThreshold = getConfig().getDouble("Abilities.Air.AirBurst.FallThreshold");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirBurst.PushFactor");
		this.damage = getConfig().getDouble("Abilities.Air.AirBurst.Damage");
		this.blastAnglePhi = getConfig().getDouble("Abilities.Air.AirBurst.AnglePhi");
		this.blastAngleTheta = getConfig().getDouble("Abilities.Air.AirBurst.AngleTheta");
		this.sneakParticles = getConfig().getInt("Abilities.Air.AirBurst.SneakParticles");
		this.particlePercentage = getConfig().getDouble("Abilities.Air.AirBurst.ParticlePercentage");
		this.blasts = new ArrayList<>();
		this.affectedEntities = new ArrayList<>();

		if (bPlayer.isAvatarState()) {
			this.chargeTime = 0;
			this.damage = AvatarState.getValue(this.damage);
		}
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		if (isFallBurst) {
			if (playerFallDistance >= fallThreshold) {
				fallBurst();
			}
			remove();
			return;
		}

		if (System.currentTimeMillis() > startTime + chargeTime && !isCharged) {
			isCharged = true;
		}

		if (!player.isSneaking()) {
			if (isCharged) {
				sphereBurst();
				remove();
				return;
			} else {
				remove();
				return;
			}
		} else if (isCharged) {
			Location location = player.getEyeLocation();
			playAirbendingParticles(location, sneakParticles);
		}
	}

	private void fallBurst() {
		if (bPlayer.isOnCooldown("AirBurst")) {
			return;
		}
		
		Location location = player.getLocation();
		double x, y, z;
		double r = 1;

		for (double theta = 75; theta < 105; theta += blastAngleTheta) {
			double dphi = blastAnglePhi / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				
				Vector direction = new Vector(x, z, y);
				AirBlast blast = new AirBlast(player, location, direction.normalize(), pushFactor, this);
				blast.setDamage(damage);
			}
		}
	}

	public static void coneBurst(Player player) {
		if (hasAbility(player, AirBurst.class)) {
			AirBurst airBurst = getAbility(player, AirBurst.class);
			airBurst.startConeBurst();
			airBurst.remove();
		}
	}

	private void startConeBurst() {
		if (isCharged) {
			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			double angle = Math.toRadians(30);
			double x, y, z;
			double r = 1;
			
			for (double theta = 0; theta <= 180; theta += blastAngleTheta) {
				double dphi = blastAnglePhi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					
					Vector direction = new Vector(x, z, y);
					if (direction.angle(vector) <= angle) {
						AirBlast blast = new AirBlast(player, location, direction.normalize(), pushFactor, this);
						blast.setDamage(damage);
					}
				}
			}
		}
	}

	public void handleSmoothParticles() {
		for (int i = 0; i < blasts.size(); i++) {
			final AirBlast blast = blasts.get(i);
			int toggleTime = 0;

			if (i % 4 != 0) {
				toggleTime = (int) (i % (100 / particlePercentage)) + 3;
			}
			new BukkitRunnable() {
				public void run() {
					blast.setShowParticles(true);
				}
			}.runTaskLater(ProjectKorra.plugin, toggleTime);
		}
	}

	private void sphereBurst() {
		if (isCharged) {
			Location location = player.getEyeLocation();
			double x, y, z;
			double r = 1;

			for (double theta = 0; theta <= 180; theta += blastAngleTheta) {
				double dphi = blastAnglePhi / Math.sin(Math.toRadians(theta));

				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					
					Vector direction = new Vector(x, z, y);
					AirBlast blast = new AirBlast(player, location, direction.normalize(), pushFactor, this);
					
					blast.setDamage(damage);
					blast.setShowParticles(false);
					blasts.add(blast);
				}
			}
		}
		handleSmoothParticles();
	}

	@Override
	public String getName() {
		return "AirBurst";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}


	public void addAffectedEntity(Entity entity) {
		affectedEntities.add(entity);
	}

	public boolean isAffectedEntity(Entity entity) {
		return affectedEntities.contains(entity);
	}

	public long getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getFallThreshold() {
		return fallThreshold;
	}

	public void setFallThreshold(double fallThreshold) {
		this.fallThreshold = fallThreshold;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getBlastAngleTheta() {
		return blastAngleTheta;
	}

	public void setBlastAngleTheta(double blastAngleTheta) {
		this.blastAngleTheta = blastAngleTheta;
	}

	public double getBlastAnglePhi() {
		return blastAnglePhi;
	}

	public void setBlastAnglePhi(double blastAnglePhi) {
		this.blastAnglePhi = blastAnglePhi;
	}

	public boolean isCharged() {
		return isCharged;
	}

	public void setCharged(boolean isCharged) {
		this.isCharged = isCharged;
	}

	public boolean isFallBurst() {
		return isFallBurst;
	}

	public void setFallBurst(boolean isFallBurst) {
		this.isFallBurst = isFallBurst;
	}

	public ArrayList<AirBlast> getBlasts() {
		return blasts;
	}

	public ArrayList<Entity> getAffectedEntities() {
		return affectedEntities;
	}
}
