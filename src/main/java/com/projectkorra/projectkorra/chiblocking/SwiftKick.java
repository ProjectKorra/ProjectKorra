package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.util.DamageHandler;

public class SwiftKick extends ChiAbility {

	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("ChiBlockChance")
	private int blockChance;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Entity target;

	public SwiftKick(final Player sourceplayer, final Entity targetentity) {
		super(sourceplayer);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.damage = getConfig().getDouble("Abilities.Chi.SwiftKick.Damage");
		this.blockChance = getConfig().getInt("Abilities.Chi.SwiftKick.ChiBlockChance");
		this.cooldown = getConfig().getInt("Abilities.Chi.SwiftKick.Cooldown");
		this.target = targetentity;
		this.start();
	}

	@Override
	public void progress() {
		if (this.target == null) {
			this.remove();
			return;
		}
		if (!ElementalAbility.isAir(this.player.getLocation().subtract(0, 0.5, 0).getBlock().getType())) {
			this.remove();
			return;
		}
		DamageHandler.damageEntity(this.target, this.damage, this);
		if (this.target instanceof Player && ChiPassive.willChiBlock(this.player, (Player) this.target)) {
			ChiPassive.blockChi((Player) this.target);
		}
		this.bPlayer.addCooldown(this);
		this.remove();
	}

	@Override
	public String getName() {
		return "SwiftKick";
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
		return true;
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

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
