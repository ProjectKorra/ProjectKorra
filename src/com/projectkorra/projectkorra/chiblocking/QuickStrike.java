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

	public QuickStrike(final Player sourceplayer, final Entity targetentity) {
		super(sourceplayer);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.damage = getConfig().getDouble("Abilities.Chi.QuickStrike.Damage");
		this.cooldown = getConfig().getLong("Abilities.Chi.QuickStrike.Cooldown");
		this.blockChance = getConfig().getInt("Abilities.Chi.QuickStrike.ChiBlockChance");
		this.target = targetentity;
		if (this.target == null) {
			return;
		}
		this.start();
	}

	@Override
	public void progress() {
		if (this.bPlayer.isOnCooldown(this)) {
			this.remove();
			return;
		}

		if (this.target == null) {
			this.remove();
			return;
		}

		this.bPlayer.addCooldown(this);
		DamageHandler.damageEntity(this.target, this.damage, this);

		if (this.target instanceof Player && ChiPassive.willChiBlock(this.player, (Player) this.target)) {
			ChiPassive.blockChi((Player) this.target);
		}

		this.remove();
	}

	@Override
	public String getName() {
		return "QuickStrike";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
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

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public int getBlockChance() {
		return this.blockChance;
	}

	public void setBlockChance(final int blockChance) {
		this.blockChance = blockChance;
	}

	public Entity getTarget() {
		return this.target;
	}

	public void setTarget(final Entity target) {
		this.target = target;
	}
}
