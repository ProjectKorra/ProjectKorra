package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.util.DamageHandler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RapidPunch extends ChiAbility {

	private int damage;
	private int punches;
	private int distance;
	private long cooldown;
	private int numPunches;
	private Entity target;
	
	public RapidPunch(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		this.damage = getConfig().getInt("Abilities.Chi.RapidPunch.Damage");
		this.punches = getConfig().getInt("Abilities.Chi.RapidPunch.Punches");
		this.distance = getConfig().getInt("Abilities.Chi.RapidPunch.Distance");
		this.cooldown = getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");
		this.target = GeneralMethods.getTargetedEntity(player, distance);
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
		bPlayer.addCooldown(this);
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

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getPunches() {
		return punches;
	}

	public void setPunches(int punches) {
		this.punches = punches;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
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
