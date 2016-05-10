package com.projectkorra.projectkorra.chiblocking;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;

/*
 * TODO: Combo classes should eventually be rewritten so that each combo is
 * treated as an individual ability. In the mean time, we will just place "fake"
 * classes so that CoreAbility will register each ability.
 */

public class ChiCombo extends ChiAbility implements ComboAbility {

	public static List<Entity> paralyzed = new ArrayList<Entity>();
	
	private String name;
	private Player player;
	private long cooldown;

	private Entity target;
	
	private long startTime;
	private long duration;

	public ChiCombo(Player player, String ability) {
		super(player);
		
		this.player = player;
		
		if (ability.equalsIgnoreCase("Immobilize")) {
			this.name = ability;

			this.cooldown = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.Cooldown");
			this.duration = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.ParalyzeDuration");
			
			this.target = GeneralMethods.getTargetedEntity(player, 5);
			
			if (target != null && paralyzed.contains(target)) {
				return;
			}
			
			paralyzed.add(target);
			bPlayer.addCooldown("Immobilize", getCooldown());
		}
		
		if (ability != null) {
			this.startTime = System.currentTimeMillis();
			start();
		}
	}

	@Override
	public void progress() {
		
		if (name.equalsIgnoreCase("Immobilize")) {
			
			if (target == null || target == player) {
				paralyzed.remove(target);
				remove();
				return;
			}
			
			if (startTime + duration <= System.currentTimeMillis()) {
				paralyzed.remove(target);
				remove();
				return;
			}

			if (!paralyzed.contains(target)) {
				remove();
				return;
			}
		}
	}

	public static boolean isParalyzed(Entity entity) {
		if (paralyzed.contains(entity)) {
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return name != null ? name : "ChiCombo";
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

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setName(String name) {
		this.name = name;
	}
}
