package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.command.Commands;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class Paralyze extends ChiAbility {

	private static final ConcurrentHashMap<Entity, Long> ENTITIES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Entity, Long> COOLDOWNS = new ConcurrentHashMap<>();

	private long cooldown;
	private Entity target;

	public Paralyze(Player sourceplayer, Entity targetentity) {
		super(sourceplayer);
		this.target = targetentity;
		this.cooldown = getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
		start();
	}
	
	@Override
	public void progress() {
		if (bPlayer.canBendIgnoreCooldowns(this)) {
			if (COOLDOWNS.containsKey(target)) {
				if (System.currentTimeMillis() < COOLDOWNS.get(target) + cooldown) {
					return;
				} else {
					COOLDOWNS.remove(target);
				}
			}
			if (target instanceof Player) {
				if (Commands.invincible.contains(((Player) target).getName())) {
					remove();
					return;
				}
			}
			paralyze(target);
			COOLDOWNS.put(target, System.currentTimeMillis());
		} else {
			remove();
		}
	}

	private static void paralyze(Entity entity) {
		ENTITIES.put(entity, System.currentTimeMillis());
		if (entity instanceof Creature) {
			((Creature) entity).setTarget(null);
		}

		if (entity instanceof Player) {
			if (Suffocate.isChannelingSphere((Player) entity)) {
				Suffocate.remove((Player) entity);
			}
		}
	}

	//TODO change paralyze to use Spigot metadata rather than checking this class
	public static boolean isParalyzed(Entity entity) {
		if (entity instanceof Player) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) entity);
			if (bPlayer != null && bPlayer.isAvatarState()) {
				return false;
			}
		}
		if (ENTITIES.containsKey(entity)) {
			if (System.currentTimeMillis() < ENTITIES.get(entity) + getDuration()) {
				return true;
			}
			ENTITIES.remove(entity);
		}
		return false;

	}

	@Override
	public String getName() {
		return "Paralyze";
	}
	
	@Override
	public Location getLocation() {
		return target != null ? target.getLocation() : null;
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
	
	public static long getDuration() {
		return getConfig().getLong("Abilities.Chi.Paralyze.Duration");
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public static ConcurrentHashMap<Entity, Long> getEntities() {
		return ENTITIES;
	}

	public static ConcurrentHashMap<Entity, Long> getCooldowns() {
		return COOLDOWNS;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
