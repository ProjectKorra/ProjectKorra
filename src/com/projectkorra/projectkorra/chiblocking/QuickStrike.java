package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class QuickStrike extends ChiAbility {

	private int damage;
	private int blockChance;
	private Entity target;

	public QuickStrike(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.damage = getConfig().getInt("Abilities.Chi.QuickStrike.Damage");
		this.blockChance = getConfig().getInt("Abilities.Chi.QuickStrike.ChiBlockChance");
		target = GeneralMethods.getTargetedEntity(player, 2);
		if (target == null) {
			return;
		}
		start();
	}

	@Override
	public void progress() {
		if (target == null) {
			remove();
			return;
		}

		DamageHandler.damageEntity(target, damage, this);
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
		return 0;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
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
