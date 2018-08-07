package com.projectkorra.projectkorra.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class RevertChecker implements Runnable {

	private final ProjectKorra plugin;

	private static final FileConfiguration config = ConfigManager.defaultConfig.get();
	private static final boolean safeRevert = config.getBoolean("Properties.Earth.SafeRevert");
	public static Map<Block, Block> earthRevertQueue = new ConcurrentHashMap<>();
	static Map<Integer, Integer> airRevertQueue = new ConcurrentHashMap<>();

	private long time;

	public RevertChecker(final ProjectKorra bending) {
		this.plugin = bending;
	}

	public static void revertAirBlocks() {
		for (final int ID : airRevertQueue.keySet()) {
			EarthAbility.revertAirBlock(ID);
			RevertChecker.airRevertQueue.remove(ID);
		}
	}

	public static void revertEarthBlocks() {
		for (final Block block : earthRevertQueue.keySet()) {
			EarthAbility.revertBlock(block);
			earthRevertQueue.remove(block);
		}
	}

	private Future<ArrayList<Chunk>> returnFuture;

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
		if (config.getBoolean("Properties.Earth.RevertEarthbending")) {

			try {
				this.returnFuture = this.plugin.getServer().getScheduler().callSyncMethod(this.plugin, new getOccupiedChunks(this.plugin.getServer()));
				final ArrayList<Chunk> chunks = this.returnFuture.get();

				final Map<Block, Information> earth = new HashMap<Block, Information>();
				earth.putAll(EarthAbility.getMovedEarth());

				for (final Block block : earth.keySet()) {
					if (earthRevertQueue.containsKey(block)) {
						continue;
					}
					boolean remove = true;
					final Information info = earth.get(block);
					if (this.time < info.getTime() + config.getLong("Properties.Earth.RevertCheckTime") || (chunks.contains(block.getChunk()) && safeRevert)) {
						remove = false;
					}
					if (remove) {
						this.addToRevertQueue(block);
					}
				}

				final Map<Integer, Information> air = new HashMap<Integer, Information>();
				air.putAll(EarthAbility.getTempAirLocations());

				for (final Integer i : air.keySet()) {
					if (airRevertQueue.containsKey(i)) {
						continue;
					}
					boolean remove = true;
					final Information info = air.get(i);
					final Block block = info.getBlock();
					if (this.time < info.getTime() + config.getLong("Properties.Earth.RevertCheckTime") || (chunks.contains(block.getChunk()) && safeRevert)) {
						remove = false;
					}
					if (remove) {
						this.addToAirRevertQueue(i);
					}
				}
			}
			catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class getOccupiedChunks implements Callable<ArrayList<Chunk>> {
		private final Server server;

		public getOccupiedChunks(final Server server) {
			this.server = server;
		}

		@Override
		public ArrayList<Chunk> call() throws Exception {

			final ArrayList<Chunk> chunks = new ArrayList<Chunk>();

			for (final Player player : this.server.getOnlinePlayers()) {
				final Chunk chunk = player.getLocation().getChunk();
				if (!chunks.contains(chunk)) {
					chunks.add(chunk);
				}
			}
			return chunks;

		}
	}

}
