package com.projectkorra.projectkorra.runnable;

import java.util.HashMap;

import com.projectkorra.projectkorra.util.ChatUtil;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempFallingBlock;
import org.bukkit.World;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.RevertChecker;

public class BendingManager implements Runnable {

	private static BendingManager instance;
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	long time;
	long interval;

	public BendingManager() {
		instance = this;
	}

	public static BendingManager getInstance() {
		return instance;
	}

	@Override
	public void run() {
		this.interval = System.currentTimeMillis() - this.time;
		this.time = System.currentTimeMillis();
		ProjectKorra.time_step = this.interval;

		RevertChecker.revertAirBlocks();

		HorizontalVelocityTracker.updateAll();

		TempFallingBlock.manage();

		final long currentTime = System.currentTimeMillis();
		while (!TempBlock.REVERT_QUEUE.isEmpty()) {
			final TempBlock tempBlock = TempBlock.REVERT_QUEUE.peek(); //Check if the top TempBlock is ready for reverting
			if (currentTime >= tempBlock.getRevertTime()) {
				TempBlock.REVERT_QUEUE.poll();
				tempBlock.revertBlock();
			} else {
				break;
			}
		}
	}

	public static String getSunriseMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Fire.DayMessage"));
	}

	public static String getSunsetMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Fire.NightMessage"));
	}

	public static String getMoonriseMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Water.NightMessage"));
	}

	public static String getMoonsetMessage() {
		return ChatUtil.color(ConfigManager.languageConfig.get().getString("Extras.Water.DayMessage"));
	}

}
