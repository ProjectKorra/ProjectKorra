package com.projectkorra.projectkorra.chiblocking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;

public class Paralyze extends ChiAbility {

	private static final Map<Entity, Long> ENTITIES = new ConcurrentHashMap<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Entity target;

	public Paralyze(Player sourceplayer, Entity targetentity) {
		super(sourceplayer);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.target = targetentity;
		this.cooldown = getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
		start();
	}

	@Override
	public void progress() {
		if (bPlayer.canBend(this)) {
			if (target instanceof Player) {
				if (Commands.invincible.contains(((Player) target).getName())) {
					remove();
					return;
				}
			}
			paralyze(target);
			bPlayer.addCooldown(this);
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

	public static Map<Entity, Long> getEntities() {
		return ENTITIES;
	}

}
