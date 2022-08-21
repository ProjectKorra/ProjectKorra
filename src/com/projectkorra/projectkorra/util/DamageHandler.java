package com.projectkorra.projectkorra.util;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.EntityBendingDeathEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

public class DamageHandler {

	private static boolean checkTicks(LivingEntity entity, double damage) {
		return entity.getNoDamageTicks() > entity.getMaximumNoDamageTicks() / 2.0f && damage <= entity.getLastDamage();
	}

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 *
	 * @param ability The ability that is used to damage the entity
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 */
	public static void damageEntity(final Entity entity, Player source, double damage, final Ability ability, boolean ignoreArmor) {
		if (ability == null) {
			return;
		}
		
		if (entity instanceof LivingEntity) {
			if (checkTicks((LivingEntity) entity, damage)) {
				return;
			}
			
			if (TempArmor.hasTempArmor((LivingEntity) entity)) {
				ignoreArmor = true;
			}
		}
		
		if (source == null) {
			source = ability.getPlayer();
		}

		// Adding source so that we don't need to modify FireDamageTimer class, which would cause breaks.
		final AbilityDamageEntityEvent damageEvent = new AbilityDamageEntityEvent(source, entity, ability, damage, ignoreArmor);
		
		if (entity instanceof Player && Commands.invincible.contains(entity.getName())) {
			damageEvent.setCancelled(true);
		}
		
		Bukkit.getServer().getPluginManager().callEvent(damageEvent);
		
		if (entity instanceof LivingEntity && !damageEvent.isCancelled()) {
			LivingEntity lent = (LivingEntity) entity;
			damage = Math.max(0, damageEvent.getDamage());
			
			if (damageEvent.doesIgnoreArmor() && damage > 0) {
				double defense = lent.getAttribute(Attribute.GENERIC_ARMOR).getValue();
				double toughness = lent.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
				damage /= 1 - (Math.min(20, Math.max(defense / 5, defense - 4 * damage / (toughness + 8)))) / 25;
			}
			
			if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus") && source != null) {
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_REACH);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_DIRECTION);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_NOSWING);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SPEED);
				NCPExemptionManager.exemptPermanently(source, CheckType.COMBINED_IMPROBABLE);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SELFHIT);
			}

			if (lent.getHealth() - damage <= 0 && !entity.isDead()) {
				final EntityBendingDeathEvent event = new EntityBendingDeathEvent(source, entity, damage, ability);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}

			final EntityDamageByEntityEvent finalEvent = new EntityDamageByEntityEvent(source, entity, DamageCause.CUSTOM, damage);
			final double prevHealth = lent.getHealth();
			lent.damage(damage, source);
			final double nextHealth = lent.getHealth();
			entity.setLastDamageCause(finalEvent);

			if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus") && source != null) {
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

	}

	public static void damageEntity(final Entity entity, final Player source, final double damage, final Ability ability) {
		damageEntity(entity, source, damage, ability, true);
	}

	public static void damageEntity(final Entity entity, final double damage, final Ability ability) {
		damageEntity(entity, ability.getPlayer(), damage, ability);
	}
}
