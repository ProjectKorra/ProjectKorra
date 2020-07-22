package com.projectkorra.projectkorra.earthbending.passive;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

public class DensityShift extends EarthAbility implements PassiveAbility {
	private static final Set<TempBlock> SAND_BLOCKS = new HashSet<>();

	public DensityShift(final Player player) {
		super(player);
	}

	public static boolean softenLanding(final Player player) {
		if (Commands.isToggledForAll && ConfigManager.defaultConfig.get().getBoolean("Properties.TogglePassivesWithAllBending")) {
			return false;
		}

		final Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		final BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null) {
			return false;
		} else if (bPlayer.canMetalbend() && ElementalAbility.isMetalBlock(block)) {
			return true;
		}

		if (ElementalAbility.isEarth(block)) {
			if ((player.getLocation().getY() % 1) != 0) {
				return true;
			}

			for (final Block affectedBlock : GeneralMethods.getBlocksAroundPoint(block.getLocation(), 2)) {
				if (ElementalAbility.isEarth(affectedBlock)) {
					if (GeneralMethods.isSolid(affectedBlock.getRelative(BlockFace.DOWN))) {
						Material sand = Material.SAND;
						if (affectedBlock.getType() == Material.RED_SANDSTONE) {
							sand = Material.RED_SAND;
						}

						final TempBlock tb = new TempBlock(affectedBlock, sand);

						if (!SAND_BLOCKS.contains(tb)) {
							SAND_BLOCKS.add(tb);
							tb.setRevertTime(getDuration());
							tb.setRevertTask(() -> SAND_BLOCKS.remove(tb));
						}
					}
				}
			}

			return true;
		}

		return (TempBlock.isTempBlock(block) && EarthAbility.isEarthbendable(TempBlock.get(block).getBlock().getType(), true, true, false)) || EarthAbility.isEarthbendable(block.getType(), true, true, false) || ElementalAbility.isTransparent(player, block);
	}

	public static boolean isPassiveSand(final Block block) {
		if (TempBlock.isTempBlock(block)) {
			return SAND_BLOCKS.contains(TempBlock.get(block));
		} else {
			return false;
		}
	}

	public static void revertSand(final Block block) {
		if (TempBlock.isTempBlock(block)) {
			TempBlock.get(block).revertBlock();
		}
	}

	public static void revertAllSand() {
		for (final TempBlock block : SAND_BLOCKS) {
			block.setRevertTask(null);
			block.revertBlock();
		}
		SAND_BLOCKS.clear();
	}

	public static void removeAll() {
		revertAllSand();
	}

	public static Set<TempBlock> getSandBlocks() {
		return SAND_BLOCKS;
	}

	public static long getDuration() {
		return ConfigManager.getConfig().getLong("Abilities.Earth.Passive.Duration");
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
		return "DensityShift";
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
}
