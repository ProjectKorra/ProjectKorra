package com.projectkorra.projectkorra.chiblocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;

/**
 * A representation of all chi combo moves.
 * @author kingbirdy
 *
 */
public class ChiCombo {
	
	private static boolean enabled = ConfigManager.defaultConfig.get().getBoolean("Abilities.Chi.ChiCombo.Enabled");
	
	public static long IMMOBILIZE_DURATION = ConfigManager.defaultConfig.get().getLong("Abilities.Chi.ChiCombo.Immobilize.ParalyzeDuration");
	public static long IMMOBILIZE_COOLDOWN = ConfigManager.defaultConfig.get().getLong("Abilities.Chi.ChiCombo.Immobilize.Cooldown");
	/**
	 * A List of every instance of an active {@link ChiCombo}.
	 */
	public static ConcurrentHashMap<Player, ChiCombo> instances = new ConcurrentHashMap<Player, ChiCombo>();
	/**
	 * A Map containing every entity which is paralyzed, and the time in milliseconds at which they will be unparalyzed.
	 */
	public static Map<Entity, Long> paralyzedEntities = new HashMap<Entity, Long>();
	
	private static Player p;
	private Entity target;
	
	public ChiCombo(Player player, String ability) {
		if (!enabled)
			return;
		if (ability.equalsIgnoreCase("Immobilize")) {
			if (!GeneralMethods.canBend(player.getName(), "Immobilize") || GeneralMethods.getBendingPlayer(player.getName()).isOnCooldown("Immobilize"))
				return;
			else {
				p = player;
				instances.put(player, this);
				target = GeneralMethods.getTargetedEntity(player, 5, new ArrayList<Entity>());
				paralyze(target, IMMOBILIZE_DURATION);
				GeneralMethods.getBendingPlayer(player.getName()).addCooldown("Immobilize", IMMOBILIZE_COOLDOWN);
			}
		}
	}
	
	/**
	 * Gets the player
	 * @return player
	 */
	public Player getPlayer() {
		return p;
	}

	/**
	 * Paralyzes the target for the given duration. The player will
	 * be unable to move or interact for the duration.
	 * @param target The Entity to be paralyzed
	 * @param duration The time in milliseconds the target will be paralyzed
	 */
	private static void paralyze(Entity target, Long duration) {
		paralyzedEntities.put(target, (System.currentTimeMillis() + duration));
	}

	/**
	 * Convenience method to see if a Player is paralyzed by a ChiCombo. 
	 * Calls {@link ChiCombo#isParalyzed(Entity)} with the Player casted to an Entity.
	 * 
	 * @param player The player to check if they're paralyzed
	 * @return True if the player is paralyzed, false otherwise
	 */
	public static boolean isParalyzed(Player player) {
		return isParalyzed((Entity) player);
	}

	/**
	 * Checks if an entity is paralyzed by a ChiCombo.
	 * 
	 * @param entity The entity to check if they're paralyzed
	 * @return True if the entity is paralyzed, false otherwise
	 */
	public static boolean isParalyzed(Entity entity) {
		return paralyzedEntities.containsKey(entity);
	}

	/**
	 * Checks the status of all paralyzed entities. If their paralysis has expired,
	 * it removes them from {@link ChiCombo#paralyzedEntities paralyzedEntities} and
	 * removes the instance of the combo from {@link ChiCombo#instances instances}.
	 */
	public static void handleParalysis() {
		for (Entity e : paralyzedEntities.keySet()) {
			if (paralyzedEntities.get(e) <= System.currentTimeMillis()) {
				paralyzedEntities.remove(e);
				for (ChiCombo c : instances.values()) {
					if (c.target.equals(e) && c.getPlayer() == p)
						instances.remove(c);
				}
			}
		}
	}
}
