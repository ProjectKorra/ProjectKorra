package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AcrobatStance extends ChiAbility {
	
	private int speed;
	private int jump;
	private double chiBlockBoost;
	private double paralyzeDodgeBoost;
	
	public AcrobatStance(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		this.speed = getConfig().getInt("Abilities.Chi.AcrobatStance.Speed") + 1;
		this.jump = getConfig().getInt("Abilities.Chi.AcrobatStance.Jump") + 1;
		this.chiBlockBoost = getConfig().getDouble("Abilities.Chi.AcrobatStance.ChiBlockBoost");
		this.paralyzeDodgeBoost = getConfig().getDouble("Abilities.Chi.AcrobatStance.ParalyzeChanceDecrease");
		
		ChiAbility stance = bPlayer.getStance();
		if (stance != null) {
			stance.remove();
			if (stance instanceof AcrobatStance) {
				bPlayer.setStance(null);
				GeneralMethods.displayMovePreview(player, this);
				player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_SHOOT, 0.5F, 2F);
				return;
			}
		}
		start();
		bPlayer.setStance(this);
		GeneralMethods.displayMovePreview(player, this);
		player.playSound(player.getLocation(), Sound.ENTITY_ENDERDRAGON_HURT, 0.5F, 2F);
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || !bPlayer.hasElement(Element.CHI)) {
			remove();
			return;
		}
		
		if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speed, true));
		}
		if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jump, true));
		}
	}
	
	@Override
	public String getName() {
		return "AcrobatStance";
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
		return true;
	}
	
	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getJump() {
		return jump;
	}

	public void setJump(int jump) {
		this.jump = jump;
	}

	public double getChiBlockBoost() {
		return chiBlockBoost;
	}

	public void setChiBlockBoost(double chiBlockBoost) {
		this.chiBlockBoost = chiBlockBoost;
	}

	public double getParalyzeDodgeBoost() {
		return paralyzeDodgeBoost;
	}

	public void setParalyzeDodgeBoost(double paralyzeDodgeBoost) {
		this.paralyzeDodgeBoost = paralyzeDodgeBoost;
	}
	
}