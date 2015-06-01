package com.projectkorra.ProjectKorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.earthbending.EarthMethods;

public class RevertChecker implements Runnable {

	public static ConcurrentHashMap<Block, Block> earthRevertQueue = new ConcurrentHashMap<Block, Block>();
	static ConcurrentHashMap<Integer, Integer> airRevertQueue = new ConcurrentHashMap<Integer, Integer>();
	private Future<ArrayList<Chunk>> returnFuture;
	// static ConcurrentHashMap<Block, Material> movedEarthQueue = new
	// ConcurrentHashMap<Block, Material>();

	static ConcurrentHashMap<Chunk, Chunk> chunks = new ConcurrentHashMap<Chunk, Chunk>();

	private ProjectKorra plugin;

	private static final boolean safeRevert = ProjectKorra.plugin.getConfig().getBoolean("Properties.Earth.SafeRevert");

	private long time;

	public RevertChecker(ProjectKorra bending) {
		plugin = bending;
	}

	private class getOccupiedChunks implements Callable<ArrayList<Chunk>> {

		private Server server;

		public getOccupiedChunks(Server server) {
			this.server = server;
		}

		@Override
		public ArrayList<Chunk> call() throws Exception {

			ArrayList<Chunk> chunks = new ArrayList<Chunk>();

			for (Player player : server.getOnlinePlayers()) {
				Chunk chunk = player.getLocation().getChunk();
				if (!chunks.contains(chunk))
					chunks.add(chunk);
			}
			return chunks;

		}
	}
	


	public void run() {
		time = System.currentTimeMillis();

		if (plugin.getConfig().getBoolean("Properties.Earth.RevertEarthbending")) {

			try {
				returnFuture = plugin
						.getServer()
						.getScheduler()
						.callSyncMethod(plugin,
								new getOccupiedChunks(plugin.getServer()));
				ArrayList<Chunk> chunks = returnFuture.get();

				Map<Block, Information> earth = new HashMap<Block, Information>();
				earth.putAll(EarthMethods.movedearth);

				for (Block block : earth.keySet()) {
					if (earthRevertQueue.containsKey(block))
						continue;
					boolean remove = true;
					Information info = earth.get(block);
					if (time < info.getTime() + ProjectKorra.plugin.getConfig().getLong("Properties.Earth.RevertCheckTime")
							|| (chunks.contains(block.getChunk()) && safeRevert)) {
						remove = false;
					}
					if (remove) {
						addToRevertQueue(block);
					}
				}

				Map<Integer, Information> air = new HashMap<Integer, Information>();
				air.putAll(EarthMethods.tempair);

				for (Integer i : air.keySet()) {
					if (airRevertQueue.containsKey(i))
						continue;
					boolean remove = true;
					Information info = air.get(i);
					Block block = info.getBlock();
					if (time < info.getTime() + ProjectKorra.plugin.getConfig().getLong("Properties.Earth.RevertCheckTime")
							|| (chunks.contains(block.getChunk()) && safeRevert)) {
						remove = false;
					}
					if (remove) {
						addToAirRevertQueue(i);
					}
				}
			} catch (Exception e) {
				GeneralMethods.logError(e, false);
			}
		}
	}

	private void addToAirRevertQueue(int i) {
		if (!airRevertQueue.containsKey(i))
			airRevertQueue.put(i, i);

	}

	// void addToMovedEarthQueue(Block block, Material type) {
	// if (!movedEarthQueue.containsKey(block))
	// movedEarthQueue.put(block, type);
	//
	// }

	void addToRevertQueue(Block block) {
		if (!earthRevertQueue.containsKey(block))
			earthRevertQueue.put(block, block);
	}

	public static void revertEarthBlocks() {
		for (Block block : earthRevertQueue.keySet()) {
			EarthMethods.revertBlock(block);
			earthRevertQueue.remove(block);
		}
	}

	public static void revertAirBlocks() {
		for (int ID : airRevertQueue.keySet()) {
			EarthMethods.revertAirBlock(ID);
			RevertChecker.airRevertQueue.remove(ID);
		}
	}

}