package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.util.DamageHandler;

public class QuickStrike extends ChiAbility {

	@Attribute(Attribute.DAMAGE)
	private double damage;
	private int blockChance;
	private Entity target;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;

	public QuickStrike(Player sourceplayer, Entity targetentity) {
		super(sourceplayer);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.damage = getConfig().getDouble("Abilities.Chi.QuickStrike.Damage");
		this.cooldown = getConfig().getLong("Abilities.Chi.QuickStrike.Cooldown");
		this.blockChance = getConfig().getInt("Abilities.Chi.QuickStrike.ChiBlockChance");
		target = targetentity;
		if (target == null) {
			return;
		}
		start();
	}

	@Override
	public void progress() {
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		if (target == null) {
			remove();
			return;
		}
		DamageHandler.damageEntity(target, damage, this);
		bPlayer.addCooldown(this);
		if (target instanceof Player && ChiPassive.willChiBlock(player, (Player) target)) {
			ChiPassive.blockChi((Player) target);
		}

		remove();
	}

	@Override
	public String getName() {
		return "QuickStrike";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
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

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public int getBlockChance() {
		return blockChance;
	}

	public void setBlockChance(int blockChance) {
		this.blockChance = blockChance;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}
}