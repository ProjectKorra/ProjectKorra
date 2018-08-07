package com.projectkorra.projectkorra.waterbending.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.TempBlock;

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
	private final Map<Block, TempBlock> affectedBlocks;

	public WaterSourceGrabber(final Player player, final Location origin) {
		this(player, origin, 1);
	}

	public WaterSourceGrabber(final Player player, final Location origin, final double animationSpeed) {
		this.player = player;
		this.animimationSpeed = animationSpeed;
		this.material = Material.STATIONARY_WATER;
		this.data = 0;
		this.currentLoc = origin.clone();
		this.state = AnimationState.RISING;
		this.affectedBlocks = new ConcurrentHashMap<>();
	}

	public void progress() {
		if (this.state == AnimationState.FAILED || this.state == AnimationState.FINISHED) {
			return;
		} else if (this.state == AnimationState.RISING) {
			this.revertBlocks();
			final double locDiff = this.player.getEyeLocation().getY() - this.currentLoc.getY();
			this.currentLoc.add(0, this.animimationSpeed * Math.signum(locDiff), 0);
			final Block block = this.currentLoc.getBlock();

			if (!(WaterAbility.isWaterbendable(this.player, null, block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(this.player, "WaterSpout", block.getLocation())) {
				this.remove();
				return;
			}

			this.createBlock(block, this.material, this.data);
			if (Math.abs(locDiff) < 1) {
				this.state = AnimationState.TOWARD;
			}
		} else {
			this.revertBlocks();
			final Location eyeLoc = this.player.getTargetBlock((HashSet<Material>) null, 2).getLocation();
			eyeLoc.setY(this.player.getEyeLocation().getY());
			final Vector vec = GeneralMethods.getDirection(this.currentLoc, eyeLoc);
			this.currentLoc.add(vec.normalize().multiply(this.animimationSpeed));

			final Block block = this.currentLoc.getBlock();
			if (!(WaterAbility.isWaterbendable(this.player, null, block) || block.getType() == Material.AIR) || GeneralMethods.isRegionProtectedFromBuild(this.player, "WaterManipulation", block.getLocation())) {
				this.remove();
				return;
			}

			this.createBlock(block, this.material, this.data);
			if (this.currentLoc.distanceSquared(eyeLoc) < 1.2) {
				this.state = AnimationState.FINISHED;
				this.revertBlocks();
			}
		}
	}

	public AnimationState getState() {
		return this.state;
	}

	public void remove() {
		this.state = AnimationState.FAILED;
	}

	public void revertBlocks() {
		final Iterator<Block> keys = this.affectedBlocks.keySet().iterator();
		while (keys.hasNext()) {
			final Block block = keys.next();
			this.affectedBlocks.get(block).revertBlock();
			this.affectedBlocks.remove(block);
		}
	}

	public void createBlock(final Block block, final Material mat) {
		this.createBlock(block, mat, (byte) 0);
	}

	public void createBlock(final Block block, final Material mat, final byte data) {
		this.affectedBlocks.put(block, new TempBlock(block, mat, data));
	}

	public Player getPlayer() {
		return this.player;
	}

	public void setPlayer(final Player player) {
		this.player = player;
	}

	public byte getData() {
		return this.data;
	}

	public void setData(final byte data) {
		this.data = data;
	}

	public double getAnimimationSpeed() {
		return this.animimationSpeed;
	}

	public void setAnimimationSpeed(final double animimationSpeed) {
		this.animimationSpeed = animimationSpeed;
	}

	public Material getMaterial() {
		return this.material;
	}

	public void setMaterial(final Material material) {
		this.material = material;
	}

	public Location getCurrentLoc() {
		return this.currentLoc;
	}

	public void setCurrentLoc(final Location currentLoc) {
		this.currentLoc = currentLoc;
	}

	public Map<Block, TempBlock> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public void setState(final AnimationState state) {
		this.state = state;
	}

}
