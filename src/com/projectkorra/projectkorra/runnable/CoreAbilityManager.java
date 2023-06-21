package com.projectkorra.projectkorra.runnable;

import java.util.HashMap;

import org.bukkit.World;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class CoreAbilityManager implements Runnable {

	private static CoreAbilityManager instance;
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	long time;
	long interval;

	public CoreAbilityManager() {
		instance = this;
	}

	public static CoreAbilityManager getInstance() {
		return instance;
	}

	@Override
	public void run() {
		this.interval = System.currentTimeMillis() - this.time;
		this.time = System.currentTimeMillis();
		ProjectKorra.time_step = this.interval;
		CoreAbility.progressAll();
	}
}
