package com.projectkorra.projectkorra.waterbending.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

public class HydroSink extends WaterAbility implements PassiveAbility {
	public HydroSink(Player player) {
		super(player);
	}
	
	public static boolean applyNoFall(Player player) {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return false;
		}
		
		Block block = player.getLocation().getBlock();
		Block fallBlock = block.getRelative(BlockFace.DOWN);
		if (TempBlock.isTempBlock(fallBlock) && (fallBlock.getType().equals(Material.ICE))) {
			return true;
		} else if (WaterAbility.isWaterbendable(player, null, block) && !WaterAbility.isPlant(block)) {
			return true;
		} else if (fallBlock.getType() == Material.AIR) {
			return true;
		} else if ((WaterAbility.isWaterbendable(player, null, fallBlock) && !WaterAbility.isPlant(fallBlock)) || fallBlock.getType() == Material.SNOW_BLOCK) {
			return true;
		}
		
		return false;
	}

	@Override
	public void progress() {}

	@Override
	public boolean isSneakAbility() {
		return false;
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
		return "HydroSink";
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}
}
