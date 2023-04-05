package com.projectkorra.projectkorra.firebending;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.attribute.Attribute;

public class FireJet extends FireAbility {

	@Attribute("AvatarStateToggle")
	private boolean avatarStateToggled;
	private long time;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SPEED)
	private double speed;
	private Random random;
	private Boolean previousGlidingState;
	private Boolean showGliding;

	public FireJet(final Player player) {
		super(player);

		final FireJet oldJet = getAbility(player, FireJet.class);
		if (oldJet != null) {
			oldJet.remove();
			return;
		} else if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		if (hasAbility(player, AirSpout.class)) {
			final AirSpout abil = getAbility(player, AirSpout.class);
			abil.remove();
		}

		this.avatarStateToggled = getConfig().getBoolean("Abilities.Avatar.AvatarState.Fire.FireJet.IsAvatarStateToggle");
		this.duration = (long) applyModifiers(getConfig().getLong("Abilities.Fire.FireJet.Duration"));
		this.speed = applyModifiers(getConfig().getDouble("Abilities.Fire.FireJet.Speed"));
		this.cooldown = applyModifiersCooldown(getConfig().getLong("Abilities.Fire.FireJet.Cooldown"));
		this.showGliding = getConfig().getBoolean("Abilities.Fire.FireJet.ShowGliding");
		this.random = new Random();

		this.speed = this.getDayFactor(this.speed);
		final Block block = player.getLocation().getBlock();

		if (isIgnitable(block) || ElementalAbility.isAir(block) || Tag.SLABS.isTagged(block.getType()) || this.bPlayer.isAvatarState()) {
			GeneralMethods.setVelocity(this, player, player.getEyeLocation().getDirection().clone().normalize().multiply(this.speed));
			if (!canFireGrief()) {
				if (ElementalAbility.isAir(block)) {
					createTempFire(block.getLocation());
				}

			} else if (ElementalAbility.isAir(block)) {
				createTempFire(block.getLocation());
			}

			this.flightHandler.createInstance(player, this.getName());
			player.setAllowFlight(true);
			this.time = System.currentTimeMillis();

			this.start();
			if (this.showGliding) {
				this.previousGlidingState = player.isGliding();
				player.setGliding(true);
			}
		}
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		} else if ((isWater(this.player.getLocation().getBlock()) || System.currentTimeMillis() > this.time + this.duration) && (!this.bPlayer.isAvatarState() || !this.avatarStateToggled)) {
			this.remove();
			return;
		} else {
			if (this.random.nextInt(2) == 0) {
				playFirebendingSound(this.player.getLocation());
			}

			playFirebendingParticles(this.player.getLocation(), 10, 0.3, 0.3, 0.3);
			double timefactor;

			if (this.bPlayer.isAvatarState() && this.avatarStateToggled) {
				timefactor = 1;
			} else {
				timefactor = 1 - (System.currentTimeMillis() - this.time) / (2.0 * this.duration);
			}

			final Vector velocity = this.player.getEyeLocation().getDirection().clone().normalize().multiply(this.speed * timefactor);
			GeneralMethods.setVelocity(this, this.player, velocity);
			this.player.setFallDistance(0);
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (this.showGliding) {
			this.player.setGliding(this.previousGlidingState);
		}
		this.flightHandler.removeInstance(this.player, this.getName());
		this.player.setFallDistance(0);
		this.bPlayer.addCooldown(this);
	}

	@Override
	public String getName() {
		return "FireJet";
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
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isAvatarStateToggled() {
		return this.avatarStateToggled;
	}

	public void setAvatarStateToggled(final boolean avatarStateToggled) {
		this.avatarStateToggled = avatarStateToggled;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
