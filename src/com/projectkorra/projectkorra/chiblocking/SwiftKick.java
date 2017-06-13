package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.util.DamageHandler;

public class SwiftKick extends ChiAbility {

	@Attribute(Attribute.DAMAGE)
	private double damage;
	private int blockChance;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Entity target;

	public SwiftKick(Player sourceplayer, Entity targetentity) {
		super(sourceplayer);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.damage = getConfig().getDouble("Abilities.Chi.SwiftKick.Damage");
		this.blockChance = getConfig().getInt("Abilities.Chi.SwiftKick.ChiBlockChance");
		this.cooldown = getConfig().getInt("Abilities.Chi.SwiftKick.Cooldown");
		this.target = targetentity;
		start();
	}

	@Override
	public void progress() {
		if (target == null) {
			remove();
			return;
		}
		if (player.getLocation().subtract(0, 0.5, 0).getBlock().getType() != Material.AIR) {
			remove();
			return;
		}
		DamageHandler.damageEntity(target, damage, this);
		if (target instanceof Player && ChiPassive.willChiBlock(player, (Player) target)) {
			ChiPassive.blockChi((Player) target);
		}
		bPlayer.addCooldown(this);
		remove();
	}

	@Override
	public String getName() {
		return "SwiftKick";
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
		return true;
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

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
