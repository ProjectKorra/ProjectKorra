package com.projectkorra.ProjectKorra.waterbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class FreezeMelt {

	public static ConcurrentHashMap<Block, Byte> frozenblocks = new ConcurrentHashMap<Block, Byte>();

	public static final int defaultrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.PhaseChange.Range");
	public static final int defaultradius = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.PhaseChange.Radius");
	
	public static final int OVERLOADING_LIMIT = 200;
	public static boolean overloading = false;
	public static int overloadCounter = 0;
	

	public FreezeMelt(Player player) {
		if(!Methods.canIcebend(player))
			return;
		
		int range = (int) Methods.waterbendingNightAugment(defaultrange, player.getWorld());
		int radius = (int) Methods.waterbendingNightAugment(defaultradius, player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
			// radius = AvatarState.getValue(radius);
		}

		Location location = Methods.getTargetedLocation(player, range);
		for (Block block : Methods.getBlocksAroundPoint(location, radius)) {
			if (isFreezable(player, block)) {
				freeze(player, block);
			}
		}

	}

	private static boolean isFreezable(Player player, Block block) {
		if (Methods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation()))
			return false;
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER)
			if (WaterManipulation.canPhysicsChange(block) && !TempBlock.isTempBlock(block))
				return true;
		return false;
	}

	@SuppressWarnings("deprecation")
	static void freeze(Player player, Block block) {
		if (Methods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation()))
			return;
		if (TempBlock.isTempBlock(block))
			return;
		byte data = block.getData();
		block.setType(Material.ICE);
		Methods.playIcebendingSound(block.getLocation());
		frozenblocks.put(block, data);
	}

	@SuppressWarnings("deprecation")
	public static void thaw(Block block) {
		if (frozenblocks.containsKey(block)) {
			byte data = frozenblocks.get(block);
			frozenblocks.remove(block);
			block.setType(Material.WATER);
			block.setData(data);
		}
	}

	public static void handleFrozenBlocks() {
		int size = frozenblocks.keySet().size();
		overloadCounter++;
		overloadCounter %= 10;
		if (overloadCounter == 0)
			overloading = size > OVERLOADING_LIMIT ? true : false;
		
		// We only want to run this method once every 10 ticks if we are overloading.
		if (overloading && overloadCounter != 0)
			return;
			
		if (overloading) {
			int i = 0;
			for (Block block : frozenblocks.keySet()) {
				final Block fblock = block;
				new BukkitRunnable() {
					public void run() {
						if (canThaw(fblock))
							thaw(fblock);
					}
				}.runTaskLater(ProjectKorra.plugin, i % 10);
				i++;
			}
		}
		else {
			for (Block block : frozenblocks.keySet()) {
				if (canThaw(block))
					thaw(block);
			}
		}
	}

	public static boolean canThaw(Block block) {
		if (frozenblocks.containsKey(block)) {
			for (Player player : block.getWorld().getPlayers()) {
				if (Methods.getBoundAbility(player) == null) {
					return true;
				}
				if (Methods.getBoundAbility(player).equalsIgnoreCase("OctopusForm")) {
					if (block.getLocation().distance(player.getLocation()) <= OctopusForm.RADIUS + 2)
						return false;
				}
				if (Methods.canBend(player.getName(), "PhaseChange")) {
					double range = Methods.waterbendingNightAugment(defaultrange, player.getWorld());
					if (AvatarState.isAvatarState(player)) {
						range = AvatarState.getValue(range);
					}
					if (block.getLocation().distance(player.getLocation()) <= range)
						return false;
				}
			}
		}
		if (!WaterManipulation.canPhysicsChange(block))
			return false;
		return true;
	}

	@SuppressWarnings("deprecation")
	private static void thawAll() {
		for (Block block : frozenblocks.keySet()) {
			if (block.getType() == Material.ICE) {
				byte data = frozenblocks.get(block);
				block.setType(Material.WATER);
				block.setData(data);
				frozenblocks.remove(block);
			}
		}
	}

	public static void removeAll() {
		thawAll();
	}

	public static String getDescription() {
		return "To use, simply left-click. "
				+ "Any water you are looking at within range will instantly freeze over into solid ice. "
				+ "Provided you stay within range of the ice and do not unbind FreezeMelt, "
				+ "that ice will not thaw. If, however, you do either of those the ice will instantly thaw. "
				+ "If you sneak (default: shift), anything around where you are looking at will instantly melt. "
				+ "Since this is a more favorable state for these things, they will never re-freeze unless they "
				+ "would otherwise by nature or some other bending ability. Additionally, if you tap sneak while "
				+ "targetting water with FreezeMelt, it will evaporate water around that block that is above "
				+ "sea level. ";
	}

}