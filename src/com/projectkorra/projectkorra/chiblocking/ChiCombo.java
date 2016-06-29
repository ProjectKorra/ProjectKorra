package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * TODO: Combo classes should eventually be rewritten so that each combo is
 * treated as an individual ability. In the mean time, we will just place "fake"
 * classes so that CoreAbility will register each ability.
 */
public class ChiCombo extends ChiAbility implements ComboAbility {

	/**
	 * a Map containing every entity which is paralyzed, and the time in
	 * milliseconds at which they will be unparalyzed.
	 */
	private static final Map<Entity, Long> PARALYZED_ENTITIES = new ConcurrentHashMap<>();

	private long duration;
	private long cooldown;
	private Entity target;
	private String name;

	public ChiCombo(Player player, String ability) {
		super(player);

		this.name = ability;

		if (ability.equalsIgnoreCase("Immobilize")) {
			this.cooldown = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.Cooldown");
			this.duration = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.ParalyzeDuration");
			target = GeneralMethods.getTargetedEntity(player, 5);
			if (!bPlayer.canBendIgnoreBinds(this)) {
				return;
			}
			if (target == null) {
				remove();
				return;
			} else {
				paralyze(target, duration);
				start();
				bPlayer.addCooldown(this);
			}
		}
	}

	/**
	 * Paralyzes the target for the given duration. The player will be unable to
	 * move or interact for the duration.
	 * 
	 * @param target The Entity to be paralyzed
	 * @param duration The time in milliseconds the target will be paralyzed
	 */
	private static void paralyze(Entity target, Long duration) {
		if (target != null) {
			PARALYZED_ENTITIES.put(target, (System.currentTimeMillis() + duration));
		}
	}

	/**
	 * Convenience method to see if a Player is paralyzed by a ChiCombo. Calls
	 * {@link ChiCombo#isParalyzed(Entity)} with the Player casted to an Entity.
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
		return PARALYZED_ENTITIES.containsKey(entity);
	}

	/**
	 * Checks the status of all paralyzed entities. If their paralysis has
	 * expired, it removes them from {@link ChiCombo#PARALYZED_ENTITIES
	 * paralyzedEntities} and removes the instance of the combo from
	 * {@link ChiCombo#instances instances}.
	 */
	public static void handleParalysis() {
		for (Entity entity : PARALYZED_ENTITIES.keySet()) {
			entity.setFallDistance(0);
			if (PARALYZED_ENTITIES.get(entity) <= System.currentTimeMillis()) {
				PARALYZED_ENTITIES.remove(entity);

				for (ChiCombo combo : getAbilities(ChiCombo.class)) {
					if (combo.target == null) {
						combo.remove();
						continue;
					} else if (combo.target.equals(entity)) {
						combo.remove();
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return name != null ? name : "ChiCombo";
	}

	@Override
	public void progress() {
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
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public String getInstructions() {
		return null;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public static Map<Entity, Long> getParalyzedEntities() {
		return PARALYZED_ENTITIES;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setName(String name) {
		this.name = name;
	}

	public class Immobilize extends ChiCombo {

		public Immobilize(Player player, String name) {
			super(player, "Immobilize");
		}

		@Override
		public String getName() {
			return "Immobilize";
		}
	}

}
