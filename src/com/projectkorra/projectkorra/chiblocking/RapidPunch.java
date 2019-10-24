package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.configuration.configs.abilities.chi.RapidPunchConfig;
import com.projectkorra.projectkorra.util.DamageHandler;

public class RapidPunch extends ChiAbility<RapidPunchConfig> {

	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute("Hits")
	private int punches;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private int numPunches;
	private long interval;
	private final long last = 0;
	private Entity target;

	public RapidPunch(final RapidPunchConfig config, final Player sourceplayer, final Entity targetentity) {
		super(config, sourceplayer);
		if (!this.bPlayer.canBend(this)) {
			return;
		}

		this.damage = config.DamagePerPunch;
		this.punches = config.TotalPunches;
		this.cooldown = config.Cooldown;
		this.interval = config.Interval;
		this.target = targetentity;
		this.bPlayer.addCooldown(this);
		this.start();
	}

	@Override
	public void progress() {
		if (this.numPunches >= this.punches || this.target == null || !(this.target instanceof LivingEntity)) {
			this.remove();
			return;
		}

		if (System.currentTimeMillis() >= this.last + this.interval) {
			final LivingEntity lt = (LivingEntity) this.target;
			DamageHandler.damageEntity(this.target, this.damage, this);

			if (this.target instanceof Player) {
				if (ChiPassive.willChiBlock(this.player, (Player) this.target)) {
					ChiPassive.blockChi((Player) this.target);
				}
				if (Suffocate.isChannelingSphere((Player) this.target)) {
					Suffocate.remove((Player) this.target);
				}
			}

			lt.setNoDamageTicks(0);
			this.numPunches++;
		}
	}

	@Override
	public String getName() {
		return "RapidPunch";
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

	public double getDamage() {
		return this.damage;
	}

	public void setDamage(final double damage) {
		this.damage = damage;
	}

	public int getPunches() {
		return this.punches;
	}

	public void setPunches(final int punches) {
		this.punches = punches;
	}

	public int getNumPunches() {
		return this.numPunches;
	}

	public void setNumPunches(final int numPunches) {
		this.numPunches = numPunches;
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
	
	@Override
	public Class<RapidPunchConfig> getConfigType() {
		return RapidPunchConfig.class;
	}

}
