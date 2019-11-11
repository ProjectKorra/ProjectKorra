package com.projectkorra.projectkorra.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.legacy.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.properties.EarthPropertiesConfig;

import io.papermc.lib.PaperLib;

public class RevertChecker implements Runnable {

	private final ProjectKorra plugin;
	public static Map<Block, Block> earthRevertQueue = new ConcurrentHashMap<>();
	static Map<Integer, Integer> airRevertQueue = new ConcurrentHashMap<>();

	private long time;

	public RevertChecker(final ProjectKorra bending) {
		this.plugin = bending;
	}

	public static void revertAirBlocks() {
		for (final int ID : airRevertQueue.keySet()) {
			PaperLib.getChunkAtAsync(EarthAbility.getTempAirLocations().get(ID).getState().getBlock().getLocation()).thenAccept(result -> EarthAbility.revertAirBlock(ID));
			RevertChecker.airRevertQueue.remove(ID);
		}
	}

	public static void revertEarthBlocks() {
		for (final Block block : earthRevertQueue.keySet()) {
			PaperLib.getChunkAtAsync(block.getLocation()).thenAccept(result -> EarthAbility.revertBlock(block));
			earthRevertQueue.remove(block);
		}
	}

	private Future<Set<Map<String, Integer>>> returnFuture;

	private void addToAirRevertQueue(final int i) {
		if (!airRevertQueue.containsKey(i)) {
			airRevertQueue.put(i, i);
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
		EarthPropertiesConfig config = ConfigManager.getConfig(EarthPropertiesConfig.class);
		if (config.RevertEarthbending) {
			try {
				this.returnFuture = this.plugin.getServer().getScheduler().callSyncMethod(this.plugin, new getOccupiedChunks(this.plugin.getServer()));
				final Set<Map<String, Integer>> chunks = this.returnFuture.get();

				final Map<Block, Information> earth = new HashMap<>(EarthAbility.getMovedEarth());

				for (final Block block : earth.keySet()) {
					if (earthRevertQueue.containsKey(block)) {
						continue;
					}

					final Information info = earth.get(block);

					final Map<String, Integer> chunkcoord = new HashMap<>();
					chunkcoord.put("x", block.getX() >> 4);
					chunkcoord.put("z", block.getZ() >> 4);

					if (this.time > (info.getTime() + config.RevertCheckTime) && !(chunks.contains(chunkcoord) && config.SafeRevert)) {
						this.addToRevertQueue(block);
					}
				}

				final Map<Integer, Information> air = new HashMap<>(EarthAbility.getTempAirLocations());

				for (final Integer i : air.keySet()) {
					if (airRevertQueue.containsKey(i)) {
						continue;
					}

					final Information info = air.get(i);
					final Block block = info.getBlock();

					final Map<String, Integer> chunkcoord = new HashMap<>();
					chunkcoord.put("x", block.getX() >> 4);
					chunkcoord.put("z", block.getZ() >> 4);

					if (this.time > (info.getTime() + config.RevertCheckTime) && !(chunks.contains(chunkcoord) && config.SafeRevert)) {
						this.addToAirRevertQueue(i);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class getOccupiedChunks implements Callable<Set<Map<String, Integer>>> {
		private final Server server;

		public getOccupiedChunks(final Server server) {
			this.server = server;
		}

		@Override
		public Set<Map<String, Integer>> call() {
			final Set<Map<String, Integer>> chunks = new HashSet<>();

			for (final Player player : this.server.getOnlinePlayers()) {
				final Map<String, Integer> chunkcoord = new HashMap<>();
				chunkcoord.put("x", player.getLocation().getBlockX() >> 4);
				chunkcoord.put("z", player.getLocation().getBlockZ() >> 4);

				chunks.add(chunkcoord);
			}

			return chunks;

		}
	}

}
