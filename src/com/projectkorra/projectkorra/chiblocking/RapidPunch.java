package com.projectkorra.projectkorra.chiblocking;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.util.DamageHandler;

public class RapidPunch extends ChiAbility {

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

	public RapidPunch(final Player sourceplayer, final Entity targetentity) {
		super(sourceplayer);
		if (!this.bPlayer.canBend(this)) {
			return;
		}

		this.damage = getConfig().getDouble("Abilities.Chi.RapidPunch.Damage");
		this.punches = getConfig().getInt("Abilities.Chi.RapidPunch.Punches");
		this.cooldown = getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");
		this.interval = getConfig().getLong("Abilities.Chi.RapidPunch.Interval");
		this.target = targetentity;
		this.start();
		if (!isRemoved()) {
			this.bPlayer.addCooldown(this);
		}
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

}
