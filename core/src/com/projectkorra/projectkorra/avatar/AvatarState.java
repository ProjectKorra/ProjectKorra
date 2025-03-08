package com.projectkorra.projectkorra.avatar;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.attribute.AttributeCache;
import com.projectkorra.projectkorra.attribute.AttributeModification;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import org.bukkit.potion.PotionEffectTypeWrapper;
import org.jetbrains.annotations.NotNull;

public class AvatarState extends AvatarAbility {

	private Map<PotionEffectType, Integer> potionEffects = new HashMap<>();
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute("ShowParticles")
	private boolean showParticles;
	@Attribute("PlaySound")
	private boolean playSound;
	@Attribute("GlowEnabled")
	private boolean glow;
	@Attribute("DarkAvatar")
	private boolean darkAvatar = false;

	public AvatarState(final Player player) {
		super(player);

		final AvatarState oldAbil = getAbility(player, AvatarState.class);
		if (oldAbil != null) {
			oldAbil.remove();
			return;
		} else if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		for (String key : ConfigManager.avatarStateConfig.get().getConfigurationSection("PotionEffects").getKeys(false)) {
			final PotionEffectType type = PotionEffectTypeWrapper.getByName(key);
			if (type == null) {
				ProjectKorra.log.warning("Invalid PotionEffectType: " + key + " in AvatarState config.");
				continue;
			}
			final int power = ConfigManager.avatarStateConfig.get().getInt("PotionEffects." + key) - 1;
			this.potionEffects.put(type, power);
		}

		this.duration = ConfigManager.avatarStateConfig.get().getLong("AvatarState.Duration");
		this.cooldown = ConfigManager.avatarStateConfig.get().getLong("AvatarState.Cooldown");
		this.showParticles = ConfigManager.avatarStateConfig.get().getBoolean("AvatarState.ShowParticles");
		this.playSound = ConfigManager.avatarStateConfig.get().getBoolean("AvatarState.PlaySound");
		this.glow = ConfigManager.avatarStateConfig.get().getBoolean("AvatarState.GlowEnabled");

		if (playSound) playAvatarSound(player.getLocation());

		this.recalculateAttributes();

		if (showParticles) {
			player.getWorld().spawnParticle(Particle.FLASH, player.getLocation().add(0, 0.8, 0), 1, 0, 0, 0);


			Random rand = new Random();
			for (int i = 0; i < 60; i++) {
				Particle particle = i % 2 == 0 ? Particle.END_ROD : (darkAvatar ? Particle.SPELL_WITCH : Particle.FIREWORKS_SPARK);

				player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), 0, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, 0.3);
			}
		}

		this.start();
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		}

		//Check the duration of the ability
		if (System.currentTimeMillis() - this.getStartTime() > this.duration) {
			player.playSound(player.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 0.5F, 0.1F);
			this.remove();
			return;
		}

		if (this.glow && !this.player.isGlowing()) this.player.setGlowing(true);

		this.addPotionEffects();
		this.showParticles();
	}

	private void showParticles() {
		if (this.showParticles) {
			if (this.getRunningTicks() % 4 == 0) {
				final Location loc = this.player.getLocation().add(0, 0.8, 0);
				loc.getWorld().spawnParticle(Particle.END_ROD, loc, 1, 0.3, 0.3, 0.3, 0.01);
			}
			if (this.getRunningTicks() % 6 == 0) {
				final Location loc = this.player.getEyeLocation();
				loc.getWorld().spawnParticle((darkAvatar ? Particle.SPELL_WITCH : Particle.FIREWORKS_SPARK), loc, 1, 0.3, 0.3, 0.3, 0);
			}

		}
	}

	public static boolean activateLowHealth(@NotNull BendingPlayer player, double damage, boolean willDie) {
		if (!player.getAbilities().containsValue("AvatarState")) return false;
		if (player.isOnCooldown("AvatarState")) return false;

		if (ConfigManager.avatarStateConfig.get().getBoolean("LowHealth.Enabled")) {
			boolean preventDeath = ConfigManager.avatarStateConfig.get().getBoolean("LowHealth.PreventDeath", false);
			final double healthThreshold = ConfigManager.avatarStateConfig.get().getDouble("LowHealth.Threshold", 4);
			final boolean boostHealth = ConfigManager.avatarStateConfig.get().getBoolean("LowHealth.BoostHealth.Enabled");
			final int amount = ConfigManager.avatarStateConfig.get().getInt("LowHealth.BoostHealth.Amount", 2);
			boolean yellowHearts = ConfigManager.avatarStateConfig.get().getBoolean("LowHealth.BoostHealth.YellowHearts");
			final double currentHealth = player.getPlayer().getHealth() - damage;

			if (currentHealth <= healthThreshold) {

				if (willDie && !preventDeath) {
					return false;
				}

				if (boostHealth) {
					//Delay by 1 tick so the event doesn't override our changes
					Bukkit.getScheduler().runTaskLater(ProjectKorra.plugin, () -> {
						if (yellowHearts) {
							if (willDie) player.getPlayer().setHealth(0.5);
							player.getPlayer().setAbsorptionAmount(amount);
						} else {
							if (willDie) player.getPlayer().setHealth(amount);
							else player.getPlayer().setHealth(Math.min(currentHealth + amount, player.getPlayer().getMaxHealth()));
						}
					}, 1L);
				}

				new AvatarState(player.getPlayer());
				return preventDeath && willDie;
			}
		}
		return false;
	}

	@Override
	public void remove() {
		this.bPlayer.addCooldown(this, true);
		this.player.setGlowing(false);
		super.remove();
	}

	private void addPotionEffects() {
		for (PotionEffectType type : this.potionEffects.keySet()) {
			final int power = this.potionEffects.get(type);

			if (!this.player.hasPotionEffect(type) || this.player.getPotionEffect(type).getAmplifier() < power
					|| (this.player.getPotionEffect(type).getAmplifier() == power && this.player.getPotionEffect(type).getDuration() == 1)) {
				addProgressPotionEffect(type, power);
			}
		}
	}

	private void addProgressPotionEffect(final PotionEffectType effect, final int power) {
		if (!this.player.hasPotionEffect(effect) || this.player.getPotionEffect(effect).getAmplifier() < power
				|| (this.player.getPotionEffect(effect).getAmplifier() == power && this.player.getPotionEffect(effect).getDuration() == 1)) {
			this.player.addPotionEffect(new PotionEffect(effect, 30, power, true, false), true);
		}
	}

	/**
	 * Deprecated for removal. Use {@link #getValue(double, CoreAbility, String)} instead
	 */
	@Deprecated
	public static double getValue(final double value) {
		final double factor = getConfig().getDouble("Abilities.Avatar.AvatarState.PowerMultiplier");
		return factor * value;
	}

	/**
	 * Deprecated for removal. Use {@link #getValue(int, CoreAbility, String)} instead
	 */
	@Deprecated
	public static int getValue(final int value) {
		return (int) getValue((double) value);
	}

	/**
	 * Gets the value of an attribute with the AvatarState modifier applied.
	 * @param initialValue The initial value of the attribute
	 * @param ability The ability that the attribute is associated with
	 * @param attribute The attribute to get the value of
	 * @return The value of the attribute with the AvatarState modifier applied
	 */
	public static double getValue(double initialValue, CoreAbility ability, String attribute) {
		AttributeCache cache = CoreAbility.getAttributeCache(ability).get(attribute);

		if (cache == null || !cache.getAvatarStateModifier().isPresent()) return initialValue;
		AttributeModification mod = cache.getAvatarStateModifier().get();

		if (!(mod.getModification() instanceof Number)) return initialValue;

		return (double) mod.getModifier().performModification(initialValue, (Number) mod.getModification());
	}

	/**
	 * Gets the value of an attribute with the AvatarState modifier applied.
	 * @param initialValue The initial value of the attribute
	 * @param ability The ability that the attribute is associated with
	 * @param attribute The attribute to get the value of
	 * @return The value of the attribute with the AvatarState modifier applied
	 */
	public static int getValue(int initialValue, CoreAbility ability, String attribute) {
		AttributeCache cache = CoreAbility.getAttributeCache(ability).get(attribute);

		if (cache == null || !cache.getAvatarStateModifier().isPresent()) return initialValue;
		AttributeModification mod = cache.getAvatarStateModifier().get();

		if (!(mod.getModification() instanceof Number)) return initialValue;

		return (int) mod.getModifier().performModification(initialValue, (Number) mod.getModification());
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
		//Scale the cooldown based on the duration of the ability
		return this.isStarted() && this.duration > 0 ? (long) (((double)(System.currentTimeMillis() - this.getStartTime())
				/ (double) this.duration) * this.cooldown) : this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return ConfigManager.avatarStateConfig.get().getBoolean("AvatarState.Enabled");
	}

	public Map<PotionEffectType, Integer> getPotionEffects() {
		return potionEffects;
	}

	public long getDuration() {
		return this.duration;
	}

	public void setDuration(final long duration) {
		this.duration = duration;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
