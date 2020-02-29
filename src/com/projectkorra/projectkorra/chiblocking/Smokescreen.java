package com.projectkorra.projectkorra.chiblocking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class Smokescreen extends ChiAbility {

	private static final Map<Integer, Smokescreen> SNOWBALLS = new ConcurrentHashMap<>();
	private static final Map<String, Long> BLINDED_TIMES = new ConcurrentHashMap<>();
	private static final Map<String, Smokescreen> BLINDED_TO_ABILITY = new ConcurrentHashMap<>();
	private static int particleAmount;

	@Attribute(Attribute.DURATION)
	private int duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double radius;

	public Smokescreen(final Player player) {
		super(player);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = getConfig().getLong("Abilities.Chi.Smokescreen.Cooldown");
		this.duration = getConfig().getInt("Abilities.Chi.Smokescreen.Duration");
		this.radius = getConfig().getDouble("Abilities.Chi.Smokescreen.Radius");
		particleAmount = getConfig().getInt("Abilities.Chi.Smokescreen.ParticleAmount");
		this.start();
	}

	@Override
	public void progress() {
		SNOWBALLS.put(this.player.launchProjectile(Snowball.class).getEntityId(), this);
		this.bPlayer.addCooldown(this);
		this.remove();
	}

	public static void playEffect(final Location loc) {
		for (int i = 0; i < 125; i++)
			ParticleEffect.SMOKE_NORMAL.display(loc, particleAmount, Math.random() + 0.7, Math.random() + 0.5, Math.random() + 0.7);
	}

	public void applyBlindness(final Entity entity) {
		if (entity instanceof Player) {
			if (Commands.invincible.contains(entity.getName())) {
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				return;
			}
			final Player p = (Player) entity;
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, this.duration * 20, 2));
			BLINDED_TIMES.put(p.getName(), System.currentTimeMillis());
			BLINDED_TO_ABILITY.put(p.getName(), this);
		}
	}

	public static void removeFromHashMap(final Entity entity) {
		if (entity instanceof Player) {
			final Player p = (Player) entity;
			if (BLINDED_TIMES.containsKey(p.getName())) {
				final Smokescreen smokescreen = BLINDED_TO_ABILITY.get(p.getName());
				if (BLINDED_TIMES.get(p.getName()) + smokescreen.duration >= System.currentTimeMillis()) {
					BLINDED_TIMES.remove(p.getName());
					BLINDED_TO_ABILITY.remove(p.getName());
				}
			}
		}
	}

	@Override
	public String getName() {
		return "Smokescreen";
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

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public int getDuration() {
		return this.duration;
	}

	public void setDuration(final int duration) {
		this.duration = duration;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public static Map<Integer, Smokescreen> getSnowballs() {
		return SNOWBALLS;
	}

	public static Map<String, Long> getBlindedTimes() {
		return BLINDED_TIMES;
	}

	public static Map<String, Smokescreen> getBlindedToAbility() {
		return BLINDED_TO_ABILITY;
	}

}
