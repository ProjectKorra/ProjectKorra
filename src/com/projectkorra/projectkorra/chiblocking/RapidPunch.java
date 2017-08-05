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
	private Entity target;

	public RapidPunch(Player sourceplayer, Entity targetentity) {
		super(sourceplayer);
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		this.damage = getConfig().getDouble("Abilities.Chi.RapidPunch.Damage");
		this.punches = getConfig().getInt("Abilities.Chi.RapidPunch.Punches");
		this.cooldown = getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");
		this.target = targetentity;
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public void progress() {
		if (numPunches >= punches || target == null || !(target instanceof LivingEntity)) {
			remove();
			return;
		}

		LivingEntity lt = (LivingEntity) target;
		DamageHandler.damageEntity(target, damage, this);

		if (target instanceof Player) {
			if (ChiPassive.willChiBlock(player, (Player) target)) {
				ChiPassive.blockChi((Player) target);
			}
			if (Suffocate.isChannelingSphere((Player) target)) {
				Suffocate.remove((Player) target);
			}
		}

		lt.setNoDamageTicks(0);
		numPunches++;
	}

	@Override
	public String getName() {
		return "RapidPunch";
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

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public int getPunches() {
		return punches;
	}

	public void setPunches(int punches) {
		this.punches = punches;
	}

	public int getNumPunches() {
		return numPunches;
	}

	public void setNumPunches(int numPunches) {
		this.numPunches = numPunches;
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
