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
			PaperLib.getChunkAtAsync(EarthAbility.getTempAirLocations().get(ID).getState().getBlock().getLocation())
					.thenAccept(result -> EarthAbility.revertAirBlock(ID));
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
		if (config.getBoolean("Properties.Earth.RevertEarthbending")) {

			try {
				final Map<Block, Information> earth = new HashMap<>(EarthAbility.getMovedEarth());

				for (final Block block : earth.keySet()) {
					if (earthRevertQueue.containsKey(block)) {
						continue;
					}

					final Information info = earth.get(block);

					if (this.time > (info.getTime() + config.getLong("Properties.Earth.RevertCheckTime"))) {
						this.addToRevertQueue(block);
					}
				}

				final Map<Integer, Information> air = new HashMap<>(EarthAbility.getTempAirLocations());

				for (final Integer i : air.keySet()) {
					if (airRevertQueue.containsKey(i)) {
						continue;
					}

					final Information info = air.get(i);

					if (this.time > (info.getTime() + config.getLong("Properties.Earth.RevertCheckTime"))) {
						this.addToAirRevertQueue(i);
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
