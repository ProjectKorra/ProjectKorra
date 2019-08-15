package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.configuration.better.configs.abilities.chi.SwiftKickConfig;
import com.projectkorra.projectkorra.util.DamageHandler;

public class SwiftKick extends ChiAbility<SwiftKickConfig> {

	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("ChiBlockChance")
	private double blockChance;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private Entity target;

	public SwiftKick(final SwiftKickConfig config, final Player sourceplayer, final Entity targetentity) {
		super(config, sourceplayer);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.damage = config.Damage;
		this.blockChance = config.ChiBlockChance;
		this.cooldown = config.Cooldown;
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

	public double getBlockChance() {
		return this.blockChance;
	}

	public void setBlockChance(final double blockChance) {
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
