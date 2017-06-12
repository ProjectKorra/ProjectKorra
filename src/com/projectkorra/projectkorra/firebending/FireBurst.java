package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.FireAbility;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class FireBurst extends FireAbility {

	private boolean charged;
	private int damage;
	private long chargeTime;
	private long range;
	private long cooldown;
	private double angleTheta;
	private double anglePhi;
	private double particlesPercentage;
	private ArrayList<FireBlast> blasts;

	public FireBurst(Player player) {
		super(player);

		this.charged = false;
		this.damage = getConfig().getInt("Abilities.Fire.FireBurst.Damage");
		this.chargeTime = getConfig().getLong("Abilities.Fire.FireBurst.ChargeTime");
		this.range = getConfig().getLong("Abilities.Fire.FireBurst.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireBurst.Cooldown");
		this.angleTheta = getConfig().getDouble("Abilities.Fire.FireBurst.AngleTheta");
		this.anglePhi = getConfig().getDouble("Abilities.Fire.FireBurst.AnglePhi");
		this.particlesPercentage = getConfig().getDouble("Abilities.Fire.FireBurst.ParticlesPercentage");
		this.blasts = new ArrayList<>();

		if (!bPlayer.canBend(this) || hasAbility(player, FireBurst.class)) {
			return;
		}

		if (isDay(player.getWorld())) {
			chargeTime /= getDayFactor();
		}
		if (bPlayer.isAvatarState() || isSozinsComet(player.getWorld())) {
			chargeTime = getConfig().getLong("Abilities.Avatar.AvatarState.Fire.FireBurst.Damage");
			damage = getConfig().getInt("Abilities.Avatar.AvatarState.Fire.FireBurst.Damage");
			cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Fire.FireBurst.Cooldown");
		}

		start();
	}

	public static void coneBurst(Player player) {
		FireBurst burst = getAbility(player, FireBurst.class);
		if (burst != null) {
			burst.coneBurst();
		}
	}

	private void coneBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			List<Block> safeBlocks = GeneralMethods.getBlocksAroundPoint(player.getLocation(), 2);
			Vector vector = location.getDirection();

			double angle = Math.toRadians(30);
			double x, y, z;
			double r = 1;

			for (double theta = 0; theta <= 180; theta += angleTheta) {
				double dphi = anglePhi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);

					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);

					if (direction.angle(vector) <= angle) {
						FireBlast fblast = new FireBlast(location, direction.normalize(), player, damage, safeBlocks);
						fblast.setRange(this.range);
					}
				}
			}
			bPlayer.addCooldown(this);
		}
		remove();
	}

	/**
	 * To combat the sphere FireBurst lag we are only going to show a certain
	 * percentage of FireBurst particles at a time per tick. As the bursts
	 * spread out then we can show more at a time.
	 */
	public void handleSmoothParticles() {
		for (int i = 0; i < blasts.size(); i++) {
			final FireBlast fblast = blasts.get(i);
			int toggleTime = (int) (i % (100.0 / particlesPercentage));
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
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > getStartTime() + chargeTime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				sphereBurst();
			} else {
				remove();
			}
		} else if (charged) {
			Location location = player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4, 3);
		}
	}

	private void sphereBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = GeneralMethods.getBlocksAroundPoint(player.getLocation(), 2);
			double x, y, z;
			double r = 1;

			for (double theta = 0; theta <= 180; theta += angleTheta) {
				double dphi = anglePhi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);

					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);

					Vector direction = new Vector(x, z, y);
					FireBlast fblast = new FireBlast(location, direction.normalize(), player, damage, safeblocks);

					fblast.setRange(this.range);
					fblast.setShowParticles(false);
					blasts.add(fblast);
				}
			}
			bPlayer.addCooldown(this);
		}
		remove();
		handleSmoothParticles();
	}

	@Override
	public String getName() {
		return "FireBurst";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
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

	public boolean isCharged() {
		return charged;
	}

	public void setCharged(boolean charged) {
		this.charged = charged;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public long getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public long getRange() {
		return range;
	}

	public void setRange(long range) {
		this.range = range;
	}

	public double getAngleTheta() {
		return angleTheta;
	}

	public void setAngleTheta(double angleTheta) {
		this.angleTheta = angleTheta;
	}

	public double getAnglePhi() {
		return anglePhi;
	}

	public void setAnglePhi(double anglePhi) {
		this.anglePhi = anglePhi;
	}

	public double getParticlesPercentage() {
		return particlesPercentage;
	}

	public void setParticlesPercentage(double particlesPercentage) {
		this.particlesPercentage = particlesPercentage;
	}

	public ArrayList<FireBlast> getBlasts() {
		return blasts;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
