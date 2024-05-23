package com.projectkorra.projectkorra.avatar;

import java.util.HashMap;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

import static com.projectkorra.projectkorra.configuration.ConfigManager.languageConfig;

public class AvatarState extends AvatarAbility {

	private static final HashMap<String, Long> START_TIMES = new HashMap<>();

	private boolean bossbarEnabled;
	private boolean regenEnabled;
	private boolean speedEnabled;
	private boolean resistanceEnabled;
	private boolean fireResistanceEnabled;
	private int regenPower;
	private int speedPower;
	private int resistancePower;
	private int fireResistancePower;
	private BossBar bar;
	private String bossbarText;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private double factor;

	public AvatarState(final Player player) {
		super(player);

		final AvatarState oldAbil = getAbility(player, AvatarState.class);
		if (oldAbil != null) {
			oldAbil.remove();
			return;
		} else if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.bossbarText = languageConfig.get().getString("Abilities.Avatar.AvatarState.AvatarStateBossbar");
		this.bossbarEnabled = getConfig().getBoolean("Abilities.Avatar.AvatarState.Bossbar");
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

		playAvatarSound(player.getLocation());

		this.start();
		if (this.bossbarEnabled) {
			String title = Element.AVATAR.getColor() + this.bossbarText + " " + ChatColor.WHITE + "100%";
			this.bar = Bukkit.createBossBar(title, BarColor.PURPLE, BarStyle.SOLID);
			this.bar.setProgress(1);
			this.bar.addPlayer(player);
		}
		if (this.duration != 0) {
			START_TIMES.put(player.getName(), System.currentTimeMillis());
			player.getUniqueId();
		}

	}

	private void progressBossBar() {
		double dur1 = 1 - ((double) (System.currentTimeMillis() - this.getStartTime()) / duration);
		this.bar.setProgress(dur1);
		String title = Element.AVATAR.getColor() + this.bossbarText + " " + ChatColor.WHITE + (int) (dur1*100) + "%";
		this.bar.setTitle(title);
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
				if (this.bossbarEnabled) {
					this.bar.removeAll();
				}
				this.remove();
				return;
			}
		}
		if (this.bossbarEnabled) {
			progressBossBar();
		}
		this.addPotionEffects();
	}

	@Override
	public void remove() {
		this.bPlayer.addCooldown(this, true);
		if (this.bossbarEnabled) {
			this.bar.removeAll();
			}
		super.remove();
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
			this.addProgressPotionEffect(PotionEffectType.FIRE_RESISTANCE, this.fireResistancePower);
		}
	}

	private void addProgressPotionEffect(final PotionEffectType effect, final int power) {
		if (!this.player.hasPotionEffect(effect) || this.player.getPotionEffect(effect).getAmplifier() < power || (this.player.getPotionEffect(effect).getAmplifier() == power && this.player.getPotionEffect(effect).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(effect, 30, power, true, false), true);
		}
	}

	public static double getValue(final double value) {
		final double factor = getConfig().getDouble("Abilities.Avatar.AvatarState.PowerMultiplier");
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

	public int getFireResistancePower() {
		return this.fireResistancePower;
	}

	public void setFireResistancePower(final int fireResistancePower) {
		this.fireResistancePower = fireResistancePower;
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

}
