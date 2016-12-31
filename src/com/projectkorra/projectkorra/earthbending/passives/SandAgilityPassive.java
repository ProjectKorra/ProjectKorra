package com.projectkorra.projectkorra.earthbending.passives;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.SandAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class SandAgilityPassive extends SandAbility implements PassiveAbility {

	// Configurable variables
	private int speedPower;
	
	// Instance related variables
	private boolean speedActivate;

	public SandAgilityPassive(Player player) {
		super(player);
		setFields();
	}
	
	public void setFields() {
		this.speedPower = ConfigManager.getConfig().getInt("Abilities.Earth.Passive.SandAgility.SpeedPower");
	}

	@Override
	public void progress() {
		if (!player.isSprinting()) {
			return;
		} else if (!isSand(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			return;
		}
		speedActivate = true;
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			for (PotionEffect potion : player.getActivePotionEffects()) {
				if (potion.getType() == PotionEffectType.SPEED) {
					if (potion.getAmplifier() > speedPower - 1) {
						speedActivate = false;
					}
				}
			}
		}
		if (speedActivate) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, speedPower - 1, true, false), false);
		}
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "SandAgility";
	}

	@Override
	public Location getLocation() {
		return null;
	}

}
