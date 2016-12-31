package com.projectkorra.projectkorra.waterbending.passives;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class IceSkate extends IceAbility implements PassiveAbility {

	public IceSkate(Player player) {
		super(player);
		Bukkit.broadcastMessage("IceSkate initiated!");
	}

	@Override
	public void progress() {
		if (isIce(player.getLocation().clone().add(0, -1, 0).getBlock())) {
			ParticleEffect.SNOW_SHOVEL.display(player.getLocation(), 0.2F, 0.2F, 0.2F, 0.01F, 10);
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "IceSkate";
	}

	@Override
	public Location getLocation() {
		return null;
	}

}
