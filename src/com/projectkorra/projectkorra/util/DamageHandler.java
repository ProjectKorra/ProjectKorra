package com.projectkorra.projectkorra.util;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.event.AbilityDamageEntityEvent;
import com.projectkorra.projectkorra.event.EntityBendingDeathEvent;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DamageHandler {

	// Armor percentage
	private static final HashMap<Integer, Double> ARMOR_PERCENTAGE_BY_ENTITY_ID = new HashMap<>();
	private static final String IGNORE_ARMOR_PREFIX = "Properties.IgnoreArmorPercentage.";
	private static final Set<LivingEntity> BEING_DAMAGED = new HashSet<>();

	private static boolean checkTicks(LivingEntity entity, double damage) {
		return entity.getNoDamageTicks() > entity.getMaximumNoDamageTicks() / 2.0f && damage <= entity.getLastDamage();
	}





	/**
	 *
	 * @param entity The entity that is being damaged.
	 * @return If this damage event should be call-backed to {@link #entityDamageCallback(EntityDamageEvent)}.
	 */
	public static boolean ignoreArmor(Entity entity) {
		return ARMOR_PERCENTAGE_BY_ENTITY_ID.containsKey(entity.getEntityId());
	}


	public static double getIgnoreArmorPercentage(final Ability ability) {
		FileConfiguration config = ProjectKorra.plugin.getConfig();

		double percentage = config.getDouble(IGNORE_ARMOR_PREFIX + "Default", 0.0);

		if (config.isSet(IGNORE_ARMOR_PREFIX + "Ability." + ability.getName())) {

			percentage = config.getDouble(IGNORE_ARMOR_PREFIX + "Ability." + ability.getName(), 0.0);

		} else {
			if (config.isSet(IGNORE_ARMOR_PREFIX + "Element." + ability.getElement().getName())) {
				percentage = config.getDouble(IGNORE_ARMOR_PREFIX + "Element." + ability.getElement().getName(), 0.0);
			}

			if (ability.getElement() instanceof Element.SubElement) {
				Element parentElement = ((Element.SubElement) ability.getElement()).getParentElement();
				if (config.isSet(IGNORE_ARMOR_PREFIX + "Element." + parentElement.getName())) {
					percentage = config.getDouble(IGNORE_ARMOR_PREFIX + "Element." + parentElement.getName(), 0.0);
				}
			}
		}

		return Math.max(Math.min(percentage, 1.0), 0.0);
	}

	/**
	 *
	 * Completely ignores armor for the next DamageEvent
	 *
	 * @param lent The entity the armor is going to be ignored
	 */
	private static void ignoreArmorDamage(LivingEntity lent) {
		ignorePercentageArmorDamage(lent, 1.0);
	}

	/**
	 *
	 * Ignores a percentage of armor for the next DamageEvent
	 *
	 * @param lent The entity the armor is going to be ignored
	 * @param percentage The percentage to be ignored (1.0 meaning no armor, 0.0 meaning full armor)
	 */
	private static void ignorePercentageArmorDamage(LivingEntity lent, double percentage) {
		ARMOR_PERCENTAGE_BY_ENTITY_ID.put(lent.getEntityId(), percentage);
	}

	/**
	 * Basically, what is happening here is: We execute Entity#damage on the entity
	 * (in {@link #damageEntity(Entity, Player, double, Ability, boolean)}) and that produces an {@link EntityDamageEvent} amongst other events.
	 * We catch that {@link EntityDamageEvent} in {@link com.projectkorra.projectkorra.PKListener}, and if an instance of damage is up for "armor ignoring" (checked with {@link #ignoreArmor(Entity)}),
	 * then we call back to this function, where it tinkers with the damage modifiers, so we can achieve the armor ignoring effect.
	 * <p>
	 * As for "DamageModifier" being depreciated
	 * <p>
	 * <strong><a href="https://www.spigotmc.org/threads/stripping-down-entitydamageevent-damagemodifier.194446">Recommend reading this whole thread</a></strong>
	 * <br>
	 * <a href="https://www.spigotmc.org/threads/stripping-down-entitydamageevent-damagemodifier.194446/page-6">Post by "almic"</a>
	 * <br>
	 * <a href="https://www.spigotmc.org/threads/stripping-down-entitydamageevent-damagemodifier.194446/page-6">Post by "ShaneBee"</a>
	 *
	 * @param event The event we want to modify
	 */
	public static void entityDamageCallback(EntityDamageEvent event) {
		if (!ARMOR_PERCENTAGE_BY_ENTITY_ID.containsKey(event.getEntity().getEntityId())) return;
		double ignorePercentage = ARMOR_PERCENTAGE_BY_ENTITY_ID.get(event.getEntity().getEntityId());
		ARMOR_PERCENTAGE_BY_ENTITY_ID.remove(event.getEntity().getEntityId()); // We get rid of the entry, so it doesn't call back future non-ability related damage.

		if (ignorePercentage == 0) return;

		if (ignorePercentage == 1) {
			event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, 0);
		} else {
			event.setDamage(EntityDamageEvent.DamageModifier.ARMOR, event.getDamage(EntityDamageEvent.DamageModifier.ARMOR) * (1d - ignorePercentage));
		}
	}

	/**
	 * @param livingEntity The living entity
	 * @return If the player is receiving bending damage, return true. Used to stop swing events that shouldn't fire
	 */
	public static boolean isReceivingDamage(LivingEntity livingEntity) {
		return BEING_DAMAGED.contains(livingEntity);
	}

	/**
	 * Damages an Entity by amount of damage specified. Starts a
	 * {@link EntityDamageByEntityEvent}.
	 *
	 * @param ability The ability that is used to damage the entity
	 * @param entity The entity that is receiving the damage
	 * @param damage The amount of damage to deal
	 */
	public static void damageEntity(final Entity entity, Player source, double damage, final Ability ability, boolean ignoreArmor, boolean doSourcelessDamage) {
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

		double percentage = getIgnoreArmorPercentage(ability);
		// Adding source so that we don't need to modify FireDamageTimer class, which would cause breaks.
		final AbilityDamageEntityEvent damageEvent = new AbilityDamageEntityEvent(entity, ability, damage, (ignoreArmor || percentage == 1.));
		
		if (entity instanceof Player && Commands.invincible.contains(entity.getName())) {
			damageEvent.setCancelled(true);
		}
		
		Bukkit.getServer().getPluginManager().callEvent(damageEvent);
		
		if (entity instanceof LivingEntity && !damageEvent.isCancelled()) {
			LivingEntity lent = (LivingEntity) entity;
			damage = Math.max(0, damageEvent.getDamage());
			
			if (Bukkit.getPluginManager().isPluginEnabled("NoCheatPlus") && source != null) {
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_REACH);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_DIRECTION);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_NOSWING);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SPEED);
				NCPExemptionManager.exemptPermanently(source, CheckType.COMBINED_IMPROBABLE);
				NCPExemptionManager.exemptPermanently(source, CheckType.FIGHT_SELFHIT);
			}

			if (lent.getHealth() - damage <= 0 && !entity.isDead()) {
				final EntityBendingDeathEvent event = new EntityBendingDeathEvent(entity, damage, ability);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}

			// Preparing the event call back
			if (damage > 0) {
				if (damageEvent.doesIgnoreArmor()) {
					ignoreArmorDamage(lent);
				} else {
					if (percentage == 1d) {
						ignoreArmorDamage(lent);
					} else if (percentage != 0d) {
						ignorePercentageArmorDamage(lent, percentage);
					}
				}
			}

			final EntityDamageByEntityEvent finalEvent = new EntityDamageByEntityEvent(source, entity, DamageCause.CUSTOM, damage);
			final double prevHealth = lent.getHealth();
			BEING_DAMAGED.add(lent); //Stops StackOverflows

			if (doSourcelessDamage) {
				lent.damage(damage);
			} else {
				lent.damage(damage, source);
			}
			BEING_DAMAGED.remove(lent);

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
	
	public static void damageEntity(final Entity entity, final Player source, final double damage, final Ability ability, final boolean ignoreArmor) {
		damageEntity(entity, source, damage, ability, ignoreArmor);
	}
	
	public static void damageEntity(final Entity entity, final double damage, final Ability ability, final boolean ignoreArmor) {
		damageEntity(entity, ability.getPlayer(), damage, ability, ignoreArmor, false);
	}

	public static void damageEntity(final Entity entity, final Player source, final double damage, final Ability ability) {
		damageEntity(entity, source, damage, ability, false, false);
	}

	public static void damageEntity(final Entity entity, final double damage, final Ability ability) {
		damageEntity(entity, ability.getPlayer(), damage, ability);
	}
}
