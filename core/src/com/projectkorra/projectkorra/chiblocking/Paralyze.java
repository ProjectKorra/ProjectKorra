package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.BendingPlayer;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.MovementHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Paralyze extends ChiAbility {

	private static final Map<LivingEntity, Pair<MovementHandler, Double>> paralyzedEntities = new ConcurrentHashMap<>();

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	private Entity target;

	@Attribute("EntityDamageThreshold")
	private double entityDamageThreshold;

    public Paralyze(final Player sourceplayer, final Entity targetentity) {
		super(sourceplayer);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.target = targetentity;
		if (!(this.target instanceof LivingEntity)) {
			return;
		}
		this.cooldown = getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
		this.duration = getConfig().getLong("Abilities.Chi.Paralyze.Duration");
		this.entityDamageThreshold = getConfig().getDouble("Abilities.Chi.Paralyze.EntityDamageThreshold");

		this.start();
	}

	@Override
	public void progress() {
		if (this.bPlayer.canBend(this)) {
			if (this.target instanceof Player) {
				if (Commands.invincible.contains(((Player) this.target).getName()) || !BendingPlayer.getBendingPlayer((Player) this.target).canBeChiblocked()) {
					this.remove();
					return;
				}
			}
			this.paralyze(this.target);
			this.bPlayer.addCooldown(this);
		}
		this.remove();
	}

	private void paralyze(final Entity entity) {
		if (entity instanceof Creature) {
			((Creature) entity).setTarget(null);
		}

		if (entity instanceof Player) {
			if (Suffocate.isChannelingSphere((Player) entity)) {
				Suffocate.remove((Player) entity);
			}
		}
        MovementHandler movementHandler = new MovementHandler((LivingEntity) entity, CoreAbility.getAbility(Paralyze.class));
		paralyzedEntities.put((LivingEntity) entity, new Pair<>(movementHandler, this.entityDamageThreshold)); // Initialize damage at threshold
		movementHandler.stopWithDuration(this.duration / 1000 * 20, Element.CHI.getColor() + "* Paralyzed *");
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDER_DRAGON_HURT, 2, 0);
	}

	@Override
	public String getName() {
		return "Paralyze";
	}

	@Override
	public Location getLocation() {
		return this.target != null ? this.target.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public Entity getTarget() {
		return this.target;
	}

	public void setTarget(final Entity target) {
		this.target = target;
	}

	public long getDuration() {
		return this.duration;
	}

	public double getEntityDamageThreshold() {
		return entityDamageThreshold;
	}

	public static void addDamage(LivingEntity entity, double damage) {
		Pair<MovementHandler, Double> entry = paralyzedEntities.get(entity);
		if (entry != null) {
			double newDamage = entry.getSecond() - damage; // Increment accumulated damage

			// Check if damage threshold is exceeded
			if (newDamage <= 0) {
				entity.sendMessage(NamedTextColor.RED + "You have taken too much damage and are no longer paralyzed!");

				// Reset movement and remove the handler
				entry.getFirst().reset();
				paralyzedEntities.remove(entity);
			} else {
				// Update the accumulated damage
				paralyzedEntities.put(entity, new Pair<>(entry.getFirst(), newDamage));
			}
		}
	}

	public static Map<LivingEntity, Pair<MovementHandler, Double>> getParalyzedEntities() {
		return paralyzedEntities;
	}

	public static class Pair<F, S> {
		private F first; // first member of pair.
		private S second; // second member of pair.

		public Pair(final F first, final S second) {
			this.first = first;
			this.second = second;
		}

		public void setFirst(final F first) {
			this.first = first;
		}

		public void setSecond(final S second) {
			this.second = second;
		}

		public F getFirst() {
			return this.first;
		}

		public S getSecond() {
			return this.second;
		}
	}
}
