package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Shockwave extends EarthAbility {
	
	private boolean charged;
	private long chargeTime;
	private long cooldown;
	private double angle;
	private double threshold;
	private double range;

	public Shockwave(Player player) {
		super(player);
				
		this.angle = Math.toRadians(40);
		this.cooldown = 1500;
		this.chargeTime = getConfig().getLong("Abilities.Earth.Shockwave.ChargeTime");
		this.threshold = getConfig().getDouble("Abilities.Earth.Shockwave.FallThreshold");
		this.range = getConfig().getDouble("Abilities.Earth.Shockwave.Range");
		
		if (bPlayer.isAvatarState()) {
			chargeTime = 0;
			cooldown = 0;
		}
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		start();
		bPlayer.addCooldown(this);
	}

	public void fallShockwave() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			return;
		} else if (player.getFallDistance() < threshold || !isEarthbendable(player.getLocation().add(0, -1, 0).getBlock())) {
			return;
		}

		areaShockwave();
		remove();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > startTime + chargeTime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				areaShockwave();
				remove();
				return;
			} else {
				remove();
				return;
			}
		} else if (charged) {
			Location location = player.getEyeLocation();
			location.getWorld().playEffect(location, Effect.SMOKE, GeneralMethods.getIntCardinalDirection(player.getEyeLocation().getDirection()), 3);
		}
	}

	public static void progressAll() {
		Ripple.progressAllCleanup();
	}

	public void areaShockwave() {
		double dtheta = 360.0 / (2 * Math.PI * this.range) - 1;
		for (double theta = 0; theta < 360; theta += dtheta) {
			double rtheta = Math.toRadians(theta);
			Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
			new Ripple(player, vector.normalize());
		}
	}

	public static void coneShockwave(Player player) {
		Shockwave shockWave = CoreAbility.getAbility(player, Shockwave.class);
		if (shockWave != null) {
			if (shockWave.charged) {
				double dtheta = 360.0 / (2 * Math.PI * shockWave.range) - 1;
				
				for (double theta = 0; theta < 360; theta += dtheta) {
					double rtheta = Math.toRadians(theta);
					Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
					if (vector.angle(player.getEyeLocation().getDirection()) < shockWave.angle) {
						new Ripple(player, vector.normalize());
					}
				}
				shockWave.remove();
			}
		}
	}

	@Override
	public String getName() { // TODO: Shockwave getName() is being overridden by Ripple
		return "Shockwave";
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

	public long getChargeTime() {
		return chargeTime;
	}

	public void setChargeTime(long chargeTime) {
		this.chargeTime = chargeTime;
	}

	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
