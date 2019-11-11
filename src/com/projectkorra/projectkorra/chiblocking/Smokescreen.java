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
import com.projectkorra.projectkorra.ability.legacy.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.configs.abilities.chi.SmokescreenConfig;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class Smokescreen extends ChiAbility<SmokescreenConfig> {

	private static final Map<Integer, Smokescreen> SNOWBALLS = new ConcurrentHashMap<>();
	private static final Map<String, Long> BLINDED_TIMES = new ConcurrentHashMap<>();
	private static final Map<String, Smokescreen> BLINDED_TO_ABILITY = new ConcurrentHashMap<>();

	@Attribute(Attribute.DURATION)
	private int duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RADIUS)
	private double radius;

	public Smokescreen(final SmokescreenConfig config, final Player player) {
		super(config, player);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = config.Cooldown;
		this.duration = config.Duration;
		this.radius = config.Radius;
		this.start();
	}

	@Override
	public void progress() {
		SNOWBALLS.put(this.player.launchProjectile(Snowball.class).getEntityId(), this);
		this.bPlayer.addCooldown(this);
		this.remove();
	}

	public static void playEffect(final Location loc) {
		int z = -2;
		int x = -2;
		final int y = 0;

		for (int i = 0; i < 125; i++) {
			final Location newLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z);
			for (int direction = 0; direction < 8; direction++) {
				ParticleEffect.SMOKE_NORMAL.display(newLoc, 4, 0.5, 0.5, 0.5);
			}
			if (z == 2) {
				z = -2;
			}
			if (x == 2) {
				x = -2;
				z++;
			}
			x++;
		}
	}

	public void applyBlindness(final Entity entity) {
		if (entity instanceof Player) {
			if (Commands.invincible.contains(((Player) entity).getName())) {
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
	
	@Override
	public Class<SmokescreenConfig> getConfigType() {
		return SmokescreenConfig.class;
	}

}
