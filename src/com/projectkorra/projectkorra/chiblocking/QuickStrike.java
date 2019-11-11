package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.legacy.ChiAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.configuration.configs.abilities.chi.QuickStrikeConfig;
import com.projectkorra.projectkorra.util.DamageHandler;

public class QuickStrike extends ChiAbility<QuickStrikeConfig> {

	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("ChiBlockChance")
	private double blockChance;
	private Entity target;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;

	public QuickStrike(final QuickStrikeConfig config, final Player sourceplayer, final Entity targetentity) {
		super(config, sourceplayer);
		if (!this.bPlayer.canBend(this)) {
			return;
		}
		this.damage = config.Damage;
		this.cooldown = config.Cooldown;
		this.blockChance = config.ChiBlockChance;
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
	
	@Override
	public Class<QuickStrikeConfig> getConfigType() {
		return QuickStrikeConfig.class;
	}
}
