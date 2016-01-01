package com.projectkorra.projectkorra.waterbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.util.TempBlock;

public class FreezeMelt {

	public static ConcurrentHashMap<Block, Byte> frozenblocks = new ConcurrentHashMap<Block, Byte>();

	public static final int defaultrange = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.PhaseChange.Range");
	public static final int defaultradius = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.PhaseChange.Radius");

	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Water.PhaseChange.Cooldown");

	public FreezeMelt(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		
		if (!WaterMethods.canIcebend(player)) {
			return;
		}
		if (bPlayer.isOnCooldown("PhaseChange")) {
			return;
		}
		bPlayer.addCooldown("PhaseChange", cooldown);

		int range = (int) WaterMethods.waterbendingNightAugment(defaultrange, player.getWorld());
		int radius = (int) WaterMethods.waterbendingNightAugment(defaultradius, player.getWorld());
		if (AvatarState.isAvatarState(player)) {
			range = AvatarState.getValue(range);
		}

		Location location = GeneralMethods.getTargetedLocation(player, range);
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (isFreezable(player, block)) {
				freeze(player, block);
			}
		}
	}

	private static boolean isFreezable(Player player, Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation())) {
			return false;
		}
		if (block.getType() == Material.WATER || block.getType() == Material.STATIONARY_WATER) {
			if (WaterManipulation.canPhysicsChange(block) && !TempBlock.isTempBlock(block)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	static void freeze(Player player, Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation())) {
			return;
		}
		if (TempBlock.isTempBlock(block)) {
			return;
		}
		byte data = block.getData();
		block.setType(Material.ICE);
		if(frozenblocks.size() % 50 == 0) {
			WaterMethods.playIcebendingSound(block.getLocation());
		}
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
			new BukkitRunnable() {
				public void run() {
					canThaw();
				}
			}.runTaskLater(ProjectKorra.plugin, 100);
	}
	
	public static void canThaw() {
		for (Block block : frozenblocks.keySet()) {
			int canThaw = 0;
			for (Player player : block.getWorld().getPlayers()) {
				double range = WaterMethods.waterbendingNightAugment(defaultrange, player.getWorld());
				if (AvatarState.isAvatarState(player)) {
					range = AvatarState.getValue(range);
				}
				
				if (player == null || !player.isOnline()) {
					canThaw++;
				}
				else if (!GeneralMethods.canBend(player.getName(), "PhaseChange")) {
					canThaw++;
				}
				else if (block.getLocation().distance(player.getLocation()) > range) {
					canThaw++;
				}
			}
			if(canThaw >= block.getWorld().getPlayers().size()) {
				thaw(block);
			}
		}
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
		return "To use, simply left-click. " + "Any water you are looking at within range will instantly freeze over into solid ice. " + "Provided you stay within range of the ice and do not unbind PhaseChange, " + "that ice will not thaw. If, however, you do either of those the ice will instantly thaw. " + "If you sneak (default: shift), anything around where you are looking at will instantly melt. " + "Since this is a more favorable state for these things, they will never re-freeze unless they " + "would otherwise by nature or some other bending ability. Additionally, if you tap sneak while " + "targetting water with PhaseChange, it will evaporate water around that block that is above " + "sea level. ";
	}

}
