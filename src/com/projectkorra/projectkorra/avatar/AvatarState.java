package com.projectkorra.projectkorra.avatar;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.api.AvatarAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.avatar.AvatarStateConfig;

public class AvatarState extends AvatarAbility<AvatarStateConfig> {

	private static final HashMap<String, Long> START_TIMES = new HashMap<>();

	private boolean regenEnabled;
	private boolean speedEnabled;
	private boolean resistanceEnabled;
	private boolean fireResistanceEnabled;
	private int regenPower;
	private int speedPower;
	private int resistancePower;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double factor;

	public AvatarState(final AvatarStateConfig config, final Player player) {
		super(config, player);

		final AvatarState oldAbil = getAbility(player, AvatarState.class);
		if (oldAbil != null) {
			oldAbil.remove();
			return;
		} else if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.regenEnabled = config.RegenerationEnabled;
		this.speedEnabled = config.SpeedEnabled;
		this.resistanceEnabled = config.ResistanceEnabled;
		this.fireResistanceEnabled = config.FireResistanceEnabled;
		this.regenPower = config.RegenerationPower - 1;
		this.speedPower = config.SpeedPower - 1;
		this.resistancePower = config.ResistancePower - 1;
		this.duration = config.Duration;
		this.cooldown = config.Cooldown;
		this.factor = config.PowerMultiplier;

		playAvatarSound(player.getLocation());

		this.start();
		this.bPlayer.addCooldown(this, true);
		if (this.duration != 0) {
			START_TIMES.put(player.getName(), System.currentTimeMillis());
			player.getUniqueId();
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		if (START_TIMES.containsKey(this.player.getName())) {
			if (START_TIMES.get(this.player.getName()) + this.duration < System.currentTimeMillis()) {
				START_TIMES.remove(this.player.getName());
				this.remove();
				return;
			}
		}

		this.addPotionEffects();
	}

	private void addPotionEffects() {
		if (this.regenEnabled) {
			this.addProgressPotionEffect(PotionEffectType.REGENERATION, this.regenPower);
		}
		if (this.speedEnabled) {
			this.addProgressPotionEffect(PotionEffectType.SPEED, this.speedPower);
		}
		if (this.resistanceEnabled) {
			this.addProgressPotionEffect(PotionEffectType.DAMAGE_RESISTANCE, this.resistancePower);
		}
		if (this.fireResistanceEnabled) {
			this.addProgressPotionEffect(PotionEffectType.FIRE_RESISTANCE, 0);
		}
	}

	private void addProgressPotionEffect(final PotionEffectType effect, final int power) {
		if (!this.player.hasPotionEffect(effect) || this.player.getPotionEffect(effect).getAmplifier() < power || (this.player.getPotionEffect(effect).getAmplifier() == power && this.player.getPotionEffect(effect).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(effect, 10, power, true, false), true);
		}
	}

	public static double getValue(final double value) {
		final double factor = ConfigManager.getConfig(AvatarStateConfig.class).PowerMultiplier;
		return factor * value;
	}

	public static int getValue(final int value) {
		return (int) getValue((double) value);
	}

	public static double getValue(final double value, final Player player) {
		final AvatarState astate = getAbility(player, AvatarState.class);
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
		return true;
	}

	public boolean isRegenEnabled() {
		return this.regenEnabled;
	}

	public void setRegenEnabled(final boolean regenEnabled) {
		this.regenEnabled = regenEnabled;
	}

	public boolean isSpeedEnabled() {
		return this.speedEnabled;
	}

	public void setSpeedEnabled(final boolean speedEnabled) {
		this.speedEnabled = speedEnabled;
	}

	public boolean isResistanceEnabled() {
		return this.resistanceEnabled;
	}

	public void setResistanceEnabled(final boolean resistanceEnabled) {
		this.resistanceEnabled = resistanceEnabled;
	}

	public boolean isFireResistanceEnabled() {
		return this.fireResistanceEnabled;
	}

	public void setFireResistanceEnabled(final boolean fireResistanceEnabled) {
		this.fireResistanceEnabled = fireResistanceEnabled;
	}

	public int getRegenPower() {
		return this.regenPower;
	}

	public void setRegenPower(final int regenPower) {
		this.regenPower = regenPower;
	}

	public int getSpeedPower() {
		return this.speedPower;
	}

	public void setSpeedPower(final int speedPower) {
		this.speedPower = speedPower;
	}

	public int getResistancePower() {
		return this.resistancePower;
	}

	public void setResistancePower(final int resistancePower) {
		this.resistancePower = resistancePower;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public double getFactor() {
		return this.factor;
	}

	public void setFactor(final double factor) {
		this.factor = factor;
	}

	public static HashMap<String, Long> getStartTimes() {
		return START_TIMES;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
	
	@Override
	public Class<AvatarStateConfig> getConfigType() {
		return AvatarStateConfig.class;
	}

}
