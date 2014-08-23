package com.projectkorra.ProjectKorra.earthbending;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.RevertChecker;

public class EarthbendingManager implements Runnable {

	public ProjectKorra plugin;

	public EarthbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	public void run() {
		EarthPassive.revertSands();
		EarthPassive.handleMetalPassives();
		for (Block block : RevertChecker.revertQueue.keySet()) {
			Methods.revertBlock(block);
			RevertChecker.revertQueue.remove(block);
		}
		for (Player player : EarthTunnel.instances.keySet()) {
			EarthTunnel.progress(player);
		}
		for (Player player : EarthArmor.instances.keySet()) {
			EarthArmor.moveArmor(player);
		}
		Tremorsense.manage(Bukkit.getServer());
		for (int ID : Catapult.instances.keySet()) {
			Catapult.progress(ID);
		}

		for (int ID : EarthColumn.instances.keySet()) {
			EarthColumn.progress(ID);
		}

		for (int ID : CompactColumn.instances.keySet()) {
			CompactColumn.progress(ID);
		}

		Shockwave.progressAll();
		for (int ID : EarthBlast.instances.keySet()) {
			EarthBlast.progress(ID);
		}

		for (int ID : LavaWall.instances.keySet()) {
			LavaWall.progress(ID);
		}
		for (int ID : LavaWave.instances.keySet()) {
			LavaWave.progress(ID);
		}

	}
}
