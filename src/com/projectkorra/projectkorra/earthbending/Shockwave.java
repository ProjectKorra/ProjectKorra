package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Shockwave extends EarthAbility {

	@Attribute(Attribute.CHARGE_DURATION)
	private long chargeTime;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double angle;
	private double threshold;
	@Attribute(Attribute.RANGE)
	private double range;

	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.KNOCKBACK)
	private double knockback;

	private List<Ripple> ripple = new ArrayList<>();

	private Stage stage = Stage.CHARGING;

	public Shockwave(final Player player, final boolean fall) {
		super(player);

		this.angle = Math.toRadians(getConfig().getDouble("Abilities.Earth.Shockwave.Angle"));
		this.threshold = getConfig().getDouble("Abilities.Earth.Shockwave.FallThreshold");

		if (this.bPlayer.isAvatarState()) {
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.Shockwave.Range");
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.Shockwave.Cooldown");
			this.chargeTime = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.Shockwave.ChargeTime");
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.Shockwave.Damage");
			this.knockback = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.Shockwave.Knockback");
		} else {
			this.range = getConfig().getDouble("Abilities.Earth.Shockwave.Range");
			this.cooldown = getConfig().getLong("Abilities.Earth.Shockwave.Cooldown");
			this.chargeTime = getConfig().getLong("Abilities.Earth.Shockwave.ChargeTime");
			this.damage = getConfig().getDouble("Abilities.Earth.Shockwave.Damage");
			this.knockback = getConfig().getDouble("Abilities.Earth.Shockwave.Knockback");
		}

		if (!this.bPlayer.canBend(this) || hasAbility(player, Shockwave.class)) {
			return;
		}

		if (fall) {
			createRipples();
		}

		this.start();
	}

	@Override
	public void progress() {
		if ((!this.player.isOnline() || this.player.isDead()) && stage == Stage.RIPPLE) {
			progressRipples(false);
		} else if (!this.bPlayer.canBendIgnoreCooldowns(this)) {
			this.remove();
			return;
		}

		if (stage == Stage.CHARGING) {
			if (System.currentTimeMillis() > this.getStartTime() + this.chargeTime) {
				this.stage = Stage.CHARGED;
			} else if (!this.player.isSneaking()) {
				this.remove();
			}
		} else if (stage == Stage.CHARGED) {
			ParticleEffect.SMOKE_NORMAL.display(player.getEyeLocation().add(this.player.getEyeLocation().getDirection()), 1);
			if (!this.player.isSneaking()) {
				createRipples();
			} else if (leftClick) {
				createConeRipples();
			}
		} else {
			progressRipples(true);
		}
	}

	public static void progressAll() {
		Ripple.progressAllCleanup();
	}

	private void progressRipples(boolean damage) {
		for (Ripple rip : new ArrayList<>(ripple)) {
			if (rip.isToBeRemoved()) {
				ripple.remove(rip);
			} else {
				rip.progress(damage);
			}
		}
		if (ripple.isEmpty()) {
			remove();
		}
	}

	private void createRipples() {
		final double dtheta = 360.0 / (2 * Math.PI * range) - 1;
		for (double theta = 0; theta < 360; theta += dtheta) {
			final double rtheta = Math.toRadians(theta);
			final Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
			ripple.add(new Ripple(this, vector.normalize(), range, damage, knockback));
		}
		this.bPlayer.addCooldown(this);
		stage = Stage.RIPPLE;
	}

	private void createConeRipples() {
		final double dtheta = 360.0 / (2 * Math.PI * range) - 1;

		for (double theta = 0; theta < 360; theta += dtheta) {
			final double rtheta = Math.toRadians(theta);
			final Vector vector = new Vector(Math.cos(rtheta), 0, Math.sin(rtheta));
			if (vector.angle(player.getEyeLocation().getDirection()) < angle) {
				ripple.add(new Ripple(this, vector.normalize(), range, damage, knockback));
			}
		}
		this.bPlayer.addCooldown(this);
		stage = Stage.RIPPLE;
	}

	private boolean leftClick = false;

	public static void leftClick(final Player player) {
		final Shockwave shockWave = getAbility(player, Shockwave.class);
		if (shockWave != null && shockWave.getStage() == Stage.CHARGED) {
			shockWave.leftClick = true;
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

	public Stage getStage() {
		return stage;
	}

	public long getChargeTime() {
		return this.chargeTime;
	}

	public double getDamage() {
		return damage;
	}

	public double getKnockback() {
		return knockback;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public void setKnockback(double knockback) {
		this.knockback = knockback;
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

	public enum Stage {
		CHARGING,
		CHARGED,
		RIPPLE
	}

}