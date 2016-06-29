package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.command.Commands;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Smokescreen extends ChiAbility {

	private static final Map<Integer, Smokescreen> SNOWBALLS = new ConcurrentHashMap<>();
	private static final Map<String, Long> BLINDED_TIMES = new ConcurrentHashMap<>();
	private static final Map<String, Smokescreen> BLINDED_TO_ABILITY = new ConcurrentHashMap<>();

	private int duration;
	private long cooldown;
	private double radius;
	
	public Smokescreen(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.cooldown = getConfig().getLong("Abilities.Chi.Smokescreen.Cooldown");
		this.duration = getConfig().getInt("Abilities.Chi.Smokescreen.Duration");
		this.radius = getConfig().getDouble("Abilities.Chi.Smokescreen.Radius");
		start();
	}
	
	@Override
	public void progress() {
		SNOWBALLS.put(player.launchProjectile(Snowball.class).getEntityId(), this);
		bPlayer.addCooldown(this);
		remove();
	}

	public static void playEffect(Location loc) {
		int z = -2;
		int x = -2;
		int y = 0;
		
		for (int i = 0; i < 125; i++) {
			Location newLoc = new Location(loc.getWorld(), loc.getX() + x, loc.getY() + y, loc.getZ() + z);
			for (int direction = 0; direction < 8; direction++) {
				loc.getWorld().playEffect(newLoc, Effect.SMOKE, direction);
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

	public void applyBlindness(Entity entity) {
		if (entity instanceof Player) {
			if (Commands.invincible.contains(((Player) entity).getName())) {
				return;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
				return;
			}
			Player p = (Player) entity;
			p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration * 20, 2));
			BLINDED_TIMES.put(p.getName(), System.currentTimeMillis());
			BLINDED_TO_ABILITY.put(p.getName(), this);
		}
	}

	public static void removeFromHashMap(Entity entity) {
		if (entity instanceof Player) {
			Player p = (Player) entity;
			if (BLINDED_TIMES.containsKey(p.getName())) {
				Smokescreen smokescreen = BLINDED_TO_ABILITY.get(p.getName());
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
	
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
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
