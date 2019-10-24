package com.projectkorra.projectkorra.waterbending.passive;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.api.ElementalAbility;
import com.projectkorra.projectkorra.ability.api.PassiveAbility;
import com.projectkorra.projectkorra.ability.api.WaterAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.water.HydroSinkConfig;
import com.projectkorra.projectkorra.configuration.configs.properties.GeneralPropertiesConfig;
import com.projectkorra.projectkorra.util.TempBlock;

public class HydroSink extends WaterAbility<HydroSinkConfig> implements PassiveAbility {
	public HydroSink(final HydroSinkConfig config, final Player player) {
		super(config, player);
	}

	public static boolean applyNoFall(final Player player) {
		if (Commands.isToggledForAll && ConfigManager.getConfig(GeneralPropertiesConfig.class).TogglePassivesWithAllBending) {
			return false;
		}

		final Block block = player.getLocation().getBlock();
		final Block fallBlock = block.getRelative(BlockFace.DOWN);
		if (TempBlock.isTempBlock(fallBlock) && (fallBlock.getType().equals(Material.ICE))) {
			return true;
		} else if (WaterAbility.isWaterbendable(player, null, block) && !ElementalAbility.isPlant(block)) {
			return true;
		} else if (ElementalAbility.isAir(fallBlock.getType())) {
			return true;
		} else if ((WaterAbility.isWaterbendable(player, null, fallBlock) && !ElementalAbility.isPlant(fallBlock)) || fallBlock.getType() == Material.SNOW_BLOCK) {
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
		return this.player.getLocation();
	}

	@Override
	public boolean isInstantiable() {
		return false;
	}

	@Override
	public boolean isProgressable() {
		return false;
	}
	
	@Override
	public Class<HydroSinkConfig> getConfigType() {
		return HydroSinkConfig.class;
	}
}
