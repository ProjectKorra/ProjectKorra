package com.projectkorra.projectkorra.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import io.papermc.lib.PaperLib;

// LMK if the changes to the logic fall out of scope of this PR, I imagine they might so I can revert those
public class RevertChecker implements Runnable {
	private static final FileConfiguration CONFIG = ConfigManager.defaultConfig.get(); // TODO: Remove this and allow for values to be updated when reloaded
	private static final boolean SAFE_REVERT = CONFIG.getBoolean("Properties.Earth.SafeRevert");
	private static final Map<Integer, Integer> AIR_REVERT_QUEUE = new ConcurrentHashMap<>();

	public static Map<Block, Block> earthRevertQueue = new ConcurrentHashMap<>(); // I would rename or final this, but it's public and that'd be a breaking change

	private final ProjectKorra plugin;
	private long time; // Is there a reason this and returnFuture are fields and not just inside the run method as local variables?
	private Future<Set<Long>> returnFuture;

	public RevertChecker(final ProjectKorra bending) {
		this.plugin = bending;
	}

	public static void revertAirBlocks() {
		for (final int id : AIR_REVERT_QUEUE.keySet()) {
			PaperLib.getChunkAtAsync(EarthAbility.getTempAirLocations().get(id).getState().getBlock().getLocation()).thenAccept(result -> EarthAbility.revertAirBlock(id));
			RevertChecker.AIR_REVERT_QUEUE.remove(id);
		}
	}

	public static void revertEarthBlocks() {
		for (final Block block : earthRevertQueue.keySet()) {
			PaperLib.getChunkAtAsync(block.getLocation()).thenAccept(result -> EarthAbility.revertBlock(block));
			earthRevertQueue.remove(block);
		}
	}

	private void addToAirRevertQueue(final int i) {
		if (!AIR_REVERT_QUEUE.containsKey(i)) {
			AIR_REVERT_QUEUE.put(i, i);
		}
	}

	private void addToRevertQueue(final Block block) {
		if (!earthRevertQueue.containsKey(block)) {
			earthRevertQueue.put(block, block);
		}
	}

	@Override
	public void run() {
		if (!this.plugin.isEnabled()) {
			return;
		}

		this.time = System.currentTimeMillis();
		if (CONFIG.getBoolean("Properties.Earth.RevertEarthbending")) {
			try {
				this.returnFuture = this.plugin.getServer().getScheduler().callSyncMethod(this.plugin, () -> {
					final Set<Long> chunks = new HashSet<>();
					for (final Player player : Bukkit.getOnlinePlayers()) {
						Location location = player.getLocation();
						chunks.add(packChunk(location.getBlockX() >> 4, location.getBlockZ() >> 4));
					}
					return chunks;
				});

				final Set<Long> chunks = this.returnFuture.get();
				final Map<Block, Information> earth = new HashMap<>(EarthAbility.getMovedEarth());

				for (final Block block : earth.keySet()) {
					if (earthRevertQueue.containsKey(block)) {
						continue;
					}

					final Information info = earth.get(block);
					final long chunk = packChunk(block.getX() >> 4, block.getZ() >> 4);

					if (this.time > (info.getTime() + CONFIG.getLong("Properties.Earth.RevertCheckTime")) && !(chunks.contains(chunk) && SAFE_REVERT)) {
						this.addToRevertQueue(block);
					}
				}

				final Map<Integer, Information> air = new HashMap<>(EarthAbility.getTempAirLocations());

				for (final Integer i : air.keySet()) {
					if (AIR_REVERT_QUEUE.containsKey(i)) {
						continue;
					}

					final Information info = air.get(i);
					final Block block = info.getBlock();
					final long chunk = packChunk(block.getX() >> 4, block.getZ() >> 4);

					if (this.time > (info.getTime() + CONFIG.getLong("Properties.Earth.RevertCheckTime")) && !(chunks.contains(chunk) && SAFE_REVERT)) {
						this.addToAirRevertQueue(i);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static long packChunk(int x, int z) {
		return (((long) x) << 32) | (z & 0xFFFFFFFFL);
	}

}
