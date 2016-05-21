package com.projectkorra.projectkorra.chiblocking;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.ChiAbility;

public class Paralyze extends ChiAbility {
	
	private static List<Entity> paralyzed = new ArrayList<Entity>();
	
	private Ability ability;
	private double distance;
	private Entity target;
	private long cooldown;
	private long duration;
	private long time;

	public Paralyze(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		} else if (paralyzed.contains(target)) {
			return;
		}
		
		this.distance = getConfig().getDouble("Abilities.Chi.Paralyze.Distance");
		this.target = GeneralMethods.getTargetedEntity(player, distance);
		this.cooldown = getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
		this.duration = getConfig().getLong("Abilities.Chi.Paralyze.Duration");
		
		bPlayer.addCooldown(this);
		
		this.time = System.currentTimeMillis();
		paralyzed.add(target);
		start();
	}
	
	public Paralyze(Ability ability, Entity target, long duration, boolean ignoreBinds, boolean ignoreCooldowns) {
		super(ability.getPlayer());
		
		if (ignoreCooldowns || ignoreBinds) {
			if (ignoreCooldowns && ignoreBinds) {
				if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
					return;
				}
			} else if (ignoreBinds) {
				if (!bPlayer.canBendIgnoreBinds(this)) {
					return;
				}
			} else if (ignoreCooldowns) {
				if (!bPlayer.canBendIgnoreCooldowns(this)) {
					return;
				}
			}
		} else if (paralyzed.contains(player)) {
			return;
		}
		
		this.ability = ability;
		this.target = target;
		this.duration = duration;
		
		this.time = System.currentTimeMillis();
		paralyzed.add(target);
		start();
	}
	
	@Override
	public void progress() {
		if (System.currentTimeMillis() >= time + duration) {
			
			if (ability != null) {
				ability.remove();
			}
			
			remove();
			return;
		} else if (!paralyzed.contains(target)) {
			
			if (ability != null) {
				ability.remove();
			}
			
			remove();			
			return;
		}
	}
	
	public static boolean isParalyzed(Entity entity) {
		if (paralyzed.contains(entity)) {
			return true;
		} else {
			return false;
		}
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
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}
	
	public Ability getAbility() {
		return ability != null ? ability : null;
	}
	
	public void setAbility(Ability ability) {
		this.ability = ability;
	}
	
	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}
}
