package com.projectkorra.projectkorra.util;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.EntityBendingDeathEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DamageHandler {

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 * 
	 * @param ability The ability that is used to damage the entity
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 */
	@SuppressWarnings("deprecation")
	public static void damageEntity(Entity entity, Player source, double damage, Ability ability) {
		
		if (ability == null)
			return;
		
		Player player = ability.getPlayer();
		AbilityDamageEntityEvent damageEvent = new AbilityDamageEntityEvent(entity, ability, damage);
		Bukkit.getServer().getPluginManager().callEvent(damageEvent);
		if (entity instanceof LivingEntity) {
			if (entity instanceof Player && Commands.invincible.contains(entity.getName())) {
				damageEvent.setCancelled(true);
			}
			if (!damageEvent.isCancelled()) {
				damage = damageEvent.getDamage();
				if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
					NCPExemptionManager.exemptPermanently(player, CheckType.FIGHT_REACH);
				}
				
				if(((LivingEntity) entity).getHealth() - damage <= 0 && !entity.isDead()) {
					EntityBendingDeathEvent event = new EntityBendingDeathEvent(entity, damage, ability);
					Bukkit.getServer().getPluginManager().callEvent(event);
				}
				
				((LivingEntity) entity).damage(damage, source);
				
				entity.setLastDamageCause(new EntityDamageByEntityEvent(player, entity, DamageCause.CUSTOM, damage));
				
				if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus")) {
					NCPExemptionManager.unexempt(player);
				}
			}
		}

	}
	
	public static void damageEntity(Entity entity, double damage, Ability ability) {
		damageEntity(entity, ability.getPlayer(), damage, ability);
	}
}
