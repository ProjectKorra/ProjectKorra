package com.projectkorra.ProjectKorra.chiblocking;

import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.Element;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.airbending.Suffocate;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class Paralyze {

	private static ConcurrentHashMap<Entity, Long> entities = new ConcurrentHashMap<Entity, Long>();
	private static ConcurrentHashMap<Entity, Long> cooldowns = new ConcurrentHashMap<Entity, Long>();

	private static final long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
	private static final long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Paralyze.Duration");

	public Paralyze(Player sourceplayer, Entity targetentity) {
		if (GeneralMethods.getBoundAbility(sourceplayer) == null)
			return;
		if (GeneralMethods.isBender(sourceplayer.getName(), Element.Chi) && GeneralMethods.getBoundAbility(sourceplayer).equalsIgnoreCase("Paralyze") && GeneralMethods.canBend(sourceplayer.getName(), "Paralyze")) {
			if (cooldowns.containsKey(targetentity)) {
				if (System.currentTimeMillis() < cooldowns.get(targetentity) + cooldown) {
					return;
				} else {
					cooldowns.remove(targetentity);
				}
			}
			if (targetentity instanceof Player) {
				if (Commands.invincible.contains(((Player) targetentity).getName()))
					return;
			}
			paralyze(targetentity);
			cooldowns.put(targetentity, System.currentTimeMillis());
		}
	}

	private static void paralyze(Entity entity) {
		entities.put(entity, System.currentTimeMillis());
		if (entity instanceof Creature) {
			((Creature) entity).setTarget(null);
		}

		if (entity instanceof Player) {
			if (Suffocate.isChannelingSphere((Player) entity)) {
				Suffocate.remove((Player) entity);
			}
		}
	}

	public static boolean isParalyzed(Entity entity) {
		if (entity instanceof Player) {
			if (AvatarState.isAvatarState((Player) entity))
				return false;
		}
		if (entities.containsKey(entity)) {
			if (System.currentTimeMillis() < entities.get(entity) + duration) {
				return true;
			}
			entities.remove(entity);
		}
		return false;

	}

}
