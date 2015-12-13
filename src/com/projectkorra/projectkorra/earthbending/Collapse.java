package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Collapse {

	private static final double defaultradius = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Collapse.Radius");
	private static final int height = EarthColumn.standardheight;
	
	private static int selectRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Collapse.SelectRange");
	private static boolean dynamic = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.Collapse.DynamicSourcing.Enabled");
	
	private ConcurrentHashMap<Block, Block> blocks = new ConcurrentHashMap<Block, Block>();
	private ConcurrentHashMap<Block, Integer> baseblocks = new ConcurrentHashMap<Block, Integer>();
	private double radius = defaultradius;
	private Player player;

	@SuppressWarnings("deprecation")
	public Collapse(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Collapse"))
			return;

		this.player = player;
		Block sblock = BlockSource.getEarthSourceBlock(player, selectRange, selectRange, ClickType.SHIFT_DOWN, false, false, dynamic, true, EarthMethods.canSandbend(player), false);
		Location location;
		if (sblock == null) {
			location = player.getTargetBlock(EarthMethods.getTransparentEarthbending(), selectRange).getLocation();
		} else {
			location = sblock.getLocation();
		}
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (EarthMethods.isEarthbendable(player, block) && !blocks.containsKey(block) && block.getY() >= location.getBlockY()) {
				getAffectedBlocks(block);
			}
		}

		if (!baseblocks.isEmpty()) {
			bPlayer.addCooldown("Collapse", GeneralMethods.getGlobalCooldown());
		}

		for (Block block : baseblocks.keySet()) {
			new CompactColumn(player, block.getLocation());
		}
	}

	private void getAffectedBlocks(Block block) {
		Block baseblock = block;
		int tall = 0;
		ArrayList<Block> bendableblocks = new ArrayList<Block>();
		bendableblocks.add(block);
		for (int i = 1; i <= height; i++) {
			Block blocki = block.getRelative(BlockFace.DOWN, i);
			if (EarthMethods.isEarthbendable(player, blocki)) {
				baseblock = blocki;
				bendableblocks.add(blocki);
				tall++;
			} else {
				break;
			}
		}
		baseblocks.put(baseblock, tall);
		for (Block blocki : bendableblocks) {
			blocks.put(blocki, baseblock);
		}

	}

	public static String getDescription() {
		return " To use, simply left-click on an earthbendable block. " + "That block and the earthbendable blocks above it will be shoved " + "back into the earth below them, if they can. " + "This ability does have the capacity to trap something inside of it, " + "although it is incredibly difficult to do so. " + "Additionally, press sneak with this ability to affect an area around your targetted location - " + "all earth that can be moved downwards will be moved downwards. " + "This ability is especially risky or deadly in caves, depending on the " + "earthbender's goal and technique.";
	}
}
