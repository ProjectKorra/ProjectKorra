package com.projectkorra.projectkorra.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public class DamageHandler {

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 *
	 * @param ability The ability that is used to damage the entity
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 */
	public static void damageEntity(final Entity entity, Player source, double damage, final Ability ability, final boolean velocityDamage) {
		if (ability == null) {
			return;
		} else if (!(entity instanceof LivingEntity)) {
			return;
		} else if (entity.isDead()) {
			return;
		}
		
		if (source == null) {
			source = ability.getPlayer();
		}

		LivingEntity target = (LivingEntity) entity;
		
		if (entity instanceof Player && Commands.invincible.contains(entity.getName())) {
			return;
		}
		
		final AbilityDamageEntityEvent damageEvent = new AbilityDamageEntityEvent(entity, ability, damage);
		Bukkit.getServer().getPluginManager().callEvent(damageEvent);
		
		if (damageEvent.isCancelled()) {
			return;
		}
		
		damage = damageEvent.getDamage();
		final boolean noCheat = Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus") && source != null;
		
		if (noCheat) {
			NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_REACH);
			NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_DIRECTION);
			NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_NOSWING);
			NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SPEED);
			NCPExemptionManager.exemptPermanently(source, CheckType.COMBINED_IMPROBABLE);
			NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SELFHIT);
		}

		final EntityDamageByEntityEvent finalEvent = new EntityDamageByEntityEvent(source, target, DamageCause.CUSTOM, damage);
		final double prevHealth = target.getHealth();
		new AbilityDamageTimestamp(entity, ability, System.currentTimeMillis(), velocityDamage);
		target.damage(damage, source);	
		final double nextHealth = target.getHealth();
		entity.setLastDamageCause(finalEvent);

		if (noCheat) {
			NCPExemptionManager.unexempt(source, CheckType.FIGHT_REACH);
			NCPExemptionManager.unexempt(source, CheckType.FIGHT_DIRECTION);
			NCPExemptionManager.unexempt(source, CheckType.FIGHT_NOSWING);
			NCPExemptionManager.unexempt(source, CheckType.FIGHT_SPEED);
			NCPExemptionManager.unexempt(source, CheckType.COMBINED_IMPROBABLE);
			NCPExemptionManager.unexempt(source, CheckType.FIGHT_SELFHIT);
		}

		if (prevHealth != nextHealth) {
			if (entity instanceof Player) {
				StatisticsMethods.addStatisticAbility(source.getUniqueId(), CoreAbility.getAbility(ability.getName()), Statistic.PLAYER_DAMAGE, (long) damage);
			}
			StatisticsMethods.addStatisticAbility(source.getUniqueId(), CoreAbility.getAbility(ability.getName()), Statistic.TOTAL_DAMAGE, (long) damage);
		}
	}
	
	public static void damageEntity(final Entity entity, final Player source, final double damage, final Ability ability) {
		damageEntity(entity, source, damage, ability, false);
	}

	public static void damageEntity(final Entity entity, final double damage, final Ability ability) {
		damageEntity(entity, ability.getPlayer(), damage, ability);
	}
}
