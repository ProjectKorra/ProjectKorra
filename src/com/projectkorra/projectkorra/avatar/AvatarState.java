package com.projectkorra.projectkorra.avatar;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class AvatarState extends AvatarAbility {

	private static final HashMap<String, Long> START_TIMES = new HashMap<>();
	private static final HashMap<UUID, Long> GLOBAL_COOLDOWNS = new HashMap<UUID, Long>();

	private boolean regenEnabled;
	private boolean speedEnabled;
	private boolean resistanceEnabled;
	private boolean fireResistanceEnabled;
	private int regenPower;
	private int speedPower;
	private int resistancePower;
	private int fireResistancePower;
	private long duration;
	private long cooldown;
	private double factor;
	
	public AvatarState(Player player) {
		super(player);
		
		AvatarState oldAbil = getAbility(player, AvatarState.class);
		if (oldAbil != null) {
			oldAbil.remove();
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (GLOBAL_COOLDOWNS.containsKey(player.getUniqueId())) {
			return;
		}
		
		this.regenEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Enabled");
		this.speedEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.Speed.Enabled");
		this.resistanceEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Enabled");
		this.fireResistanceEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Enabled");
		this.regenPower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.Regeneration.Power") - 1;
		this.speedPower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.Speed.Power") - 1;
		this.resistancePower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.DamageResistance.Power") - 1;
		this.fireResistancePower = getConfig().getInt("Abilities.Avatar.AvatarState.PotionEffects.FireResistance.Power") - 1;
		this.duration = getConfig().getLong("Abilities.Avatar.AvatarState.Duration");
		this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Cooldown");	
		this.factor = getConfig().getDouble("Abilities.Avatar.AvatarState.PowerMultiplier");
		
		new Flight(player);
		playAvatarSound(player.getLocation());
		
		start();
		bPlayer.addCooldown(this);
		if (duration != 0) {
			START_TIMES.put(player.getName(), System.currentTimeMillis());
			GLOBAL_COOLDOWNS.put(player.getUniqueId(), System.currentTimeMillis() + cooldown);
			final UUID id = player.getUniqueId();
			Bukkit.getScheduler().runTaskLaterAsynchronously(ProjectKorra.plugin, new Runnable() {
				@Override
				public void run() {
					GLOBAL_COOLDOWNS.remove(id);
				}
			}, cooldown/50);
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			if (player != null) {
				if (bPlayer.isOnCooldown(this)) {
					bPlayer.removeCooldown(this);
				}
			}
			remove();
			return;
		}

		if (START_TIMES.containsKey(player.getName())) {
			if (START_TIMES.get(player.getName()) + duration < System.currentTimeMillis()) {
				START_TIMES.remove(player.getName());
				remove();
				return;
			}
		}

		addPotionEffects();
	}

	private void addPotionEffects() {
		int duration = 70;
		if (regenEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, regenPower));
		}
		if (speedEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, speedPower));
		}
		if (resistanceEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, resistancePower));
		}
		if (fireResistanceEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, fireResistancePower));
		}
	}

	public static double getValue(double value) {
		double factor = getConfig().getDouble("Abilities.Avatar.AvatarState.PowerMultiplier");
		return factor * value;
	}

	public static int getValue(int value) {
		return (int) getValue((double) value);
	}
	
	public static double getValue(double value, Player player) {
		AvatarState astate = getAbility(player, AvatarState.class);
		if (astate != null) {
			return astate.getFactor() * value;
		}
		return value;
	}

	@Override
	public String getName() {
		return "AvatarState";
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
		return true;
	}

	public boolean isRegenEnabled() {
		return regenEnabled;
	}

	public void setRegenEnabled(boolean regenEnabled) {
		this.regenEnabled = regenEnabled;
	}

	public boolean isSpeedEnabled() {
		return speedEnabled;
	}

	public void setSpeedEnabled(boolean speedEnabled) {
		this.speedEnabled = speedEnabled;
	}

	public boolean isResistanceEnabled() {
		return resistanceEnabled;
	}

	public void setResistanceEnabled(boolean resistanceEnabled) {
		this.resistanceEnabled = resistanceEnabled;
	}

	public boolean isFireResistanceEnabled() {
		return fireResistanceEnabled;
	}

	public void setFireResistanceEnabled(boolean fireResistanceEnabled) {
		this.fireResistanceEnabled = fireResistanceEnabled;
	}

	public int getRegenPower() {
		return regenPower;
	}

	public void setRegenPower(int regenPower) {
		this.regenPower = regenPower;
	}

	public int getSpeedPower() {
		return speedPower;
	}

	public void setSpeedPower(int speedPower) {
		this.speedPower = speedPower;
	}

	public int getResistancePower() {
		return resistancePower;
	}

	public void setResistancePower(int resistancePower) {
		this.resistancePower = resistancePower;
	}

	public int getFireResistancePower() {
		return fireResistancePower;
	}

	public void setFireResistancePower(int fireResistancePower) {
		this.fireResistancePower = fireResistancePower;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public static HashMap<String, Long> getStartTimes() {
		return START_TIMES;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
