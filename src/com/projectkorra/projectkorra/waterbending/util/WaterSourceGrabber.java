package com.projectkorra.projectkorra.waterbending.util;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaterSourceGrabber {

	/*
	 * Using an initial WaterSource block, this class animates the block up to a
	 * specific height and then toward the players location.
	 */
	public static enum AnimationState {
		RISING, TOWARD, FINISHED, FAILED
	}

	private Player player;
	private byte data;
	private double animimationSpeed;
	private AnimationState state;
	private Material material;
	private Location currentLoc;
	private Map<Block, TempBlock> affectedBlocks;

	public WaterSourceGrabber(Player player, Location origin) {
		this(player, origin, 1);
	}

	public WaterSourceGrabber(Player player, Location origin, double animationSpeed) {
		this.player = player;
		this.animimationSpeed = animationSpeed;
		this.material = Material.STATIONARY_WATER;
		this.data = 0;
		this.currentLoc = origin.clone();
		this.state = AnimationState.RISING;
		this.affectedBlocks = new ConcurrentHashMap<>();
	}

	public void progress() {
		if (state == AnimationState.FAILED || state == AnimationState.FINISHED) {
			return;
		} else if (state == AnimationState.RISING) {
			revertBlocks();
			double locDiff = player.getEyeLocation().getY() - currentLoc.getY();
			currentLoc.add(0, animimationSpeed * Math.signum(locDiff), 0);
			Block block = currentLoc.getBlock();

			if (!(WaterAbility.isWaterbendable(player, null, block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(player, "WaterSpout", block.getLocation())) {
				remove();
				return;
			}

			createBlock(block, material, data);
			if (Math.abs(locDiff) < 1) {
				state = AnimationState.TOWARD;
			}
		} else {
			revertBlocks();
			Location eyeLoc = player.getTargetBlock((HashSet<Material>) null, 2).getLocation();
			eyeLoc.setY(player.getEyeLocation().getY());
			Vector vec = GeneralMethods.getDirection(currentLoc, eyeLoc);
			currentLoc.add(vec.normalize().multiply(animimationSpeed));

			Block block = currentLoc.getBlock();
			if (!(WaterAbility.isWaterbendable(player, null, block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(player, "WaterManipulation", block.getLocation())) {
				remove();
				return;
			}

			createBlock(block, material, data);
			if (currentLoc.distanceSquared(eyeLoc) < 1.2) {
				state = AnimationState.FINISHED;
				revertBlocks();
			}
		}
	}

	public AnimationState getState() {
		return state;
	}

	public void remove() {
		state = AnimationState.FAILED;
	}

	public void revertBlocks() {
		Iterator<Block> keys = affectedBlocks.keySet().iterator();
		while (keys.hasNext()) {
			Block block = keys.next();
			affectedBlocks.get(block).revertBlock();
			affectedBlocks.remove(block);
		}
	}

	public void createBlock(Block block, Material mat) {
		createBlock(block, mat, (byte) 0);
	}

	public void createBlock(Block block, Material mat, byte data) {
		affectedBlocks.put(block, new TempBlock(block, mat, data));
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public byte getData() {
		return data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public double getAnimimationSpeed() {
		return animimationSpeed;
	}

	public void setAnimimationSpeed(double animimationSpeed) {
		this.animimationSpeed = animimationSpeed;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public Location getCurrentLoc() {
		return currentLoc;
	}

	public void setCurrentLoc(Location currentLoc) {
		this.currentLoc = currentLoc;
	}

	public Map<Block, TempBlock> getAffectedBlocks() {
		return affectedBlocks;
	}

	public void setState(AnimationState state) {
		this.state = state;
	}

}
