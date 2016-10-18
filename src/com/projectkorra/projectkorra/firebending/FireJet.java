package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.earthbending.SandSpout;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.ParticleEffect;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Random;

public class FireJet extends FireAbility {

	private boolean avatarStateToggled;
	private long time;
	private long duration;
	private long cooldown;
	private double speed;
	private Random random;
	
	public FireJet(Player player) {
		super(player);
		
		FireJet oldJet = getAbility(player, FireJet.class);
		if (oldJet != null) {
			oldJet.remove();
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (hasAbility(player, AirSpout.class)) {
			AirSpout abil = getAbility(player, AirSpout.class);
			abil.remove();
		} else if (hasAbility(player, SandSpout.class)) {
			SandSpout abil = getAbility(player, SandSpout.class);
			abil.remove();
		}
		
		this.avatarStateToggled = getConfig().getBoolean("Abilities.Fire.FireJet.IsAvatarStateToggle");
		this.duration = getConfig().getLong("Abilities.Fire.FireJet.Duration");
		this.speed = getConfig().getDouble("Abilities.Fire.FireJet.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireJet.Cooldown");
		this.random = new Random();

		this.speed = getDayFactor(speed);
		Block block = player.getLocation().getBlock();
		
		if (BlazeArc.isIgnitable(player, block) || block.getType() == Material.AIR || block.getType() == Material.STEP || block.getType() == Material.WOOD_STEP || bPlayer.isAvatarState()) {
			player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(speed));
			if (canFireGrief()) {
				if (block.getType() == Material.AIR) {
					createTempFire(block.getLocation());
					}
				
			} else if (block.getType() == Material.AIR) {
				block.setType(Material.FIRE);
			}
			
			new Flight(player);
			player.setAllowFlight(true);
			time = System.currentTimeMillis();
			
			start();
			bPlayer.addCooldown(this);
		}
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if ((isWater(player.getLocation().getBlock()) || System.currentTimeMillis() > time + duration)
				&& (!bPlayer.isAvatarState() || !avatarStateToggled)) {
			remove();
			return;
		} else {
			if (random.nextInt(2) == 0) {
				playFirebendingSound(player.getLocation());
			}
			
			ParticleEffect.FLAME.display(player.getLocation(), 0.6F, 0.6F, 0.6F, 0, 20);
			ParticleEffect.SMOKE.display(player.getLocation(), 0.6F, 0.6F, 0.6F, 0, 20);
			double timefactor;
			
			if (bPlayer.isAvatarState() && avatarStateToggled) {
				timefactor = 1;
			} else {
				timefactor = 1 - (System.currentTimeMillis() - time) / (2.0 * duration);
			}
			
			Vector velocity = player.getEyeLocation().getDirection().clone().normalize().multiply(speed * timefactor);
			player.setVelocity(velocity);
			player.setFallDistance(0);
		}
	}

	@Override
	public String getName() {
		return "FireJet";
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
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isAvatarStateToggled() {
		return avatarStateToggled;
	}

	public void setAvatarStateToggled(boolean avatarStateToggled) {
		this.avatarStateToggled = avatarStateToggled;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
