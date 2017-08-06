package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.util.MovementHandler;

public class Paralyze extends ChiAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Entity target;

	public Paralyze(Player sourceplayer, Entity targetentity) {
		super(sourceplayer);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.target = targetentity;
		this.cooldown = getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
		start();
	}

	@Override
	public void progress() {
		if (bPlayer.canBend(this)) {
			if (target instanceof Player) {
				if (Commands.invincible.contains(((Player) target).getName())) {
					remove();
					return;
				}
			}
			paralyze(target);
			bPlayer.addCooldown(this);
		} else {
			remove();
		}
	}

	private static void paralyze(Entity entity) {
		if (entity instanceof Creature) {
			((Creature) entity).setTarget(null);
		}
		
		if (entity instanceof Player) {
			if (Suffocate.isChannelingSphere((Player) entity)) {
				Suffocate.remove((Player) entity);
			}
		} 
		MovementHandler mh = new MovementHandler((LivingEntity) entity);
		mh.stop(getDuration()/1000*20, Element.CHI.getColor() + "* Paralyzed *");
		entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 2, 0);
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
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public static long getDuration() {
		return getConfig().getLong("Abilities.Chi.Paralyze.Duration");
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

}
