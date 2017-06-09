package com.projectkorra.projectkorra.firebending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.TempBlock;

public class Illumination extends FireAbility {

	private static final Map<TempBlock, Player> BLOCKS = new ConcurrentHashMap<>();

	private byte normalData;
	private long cooldown;
	private double range;
	private int lightThreshold;
	private Material normalType;
	private TempBlock block;
	private int oldLevel;

	public Illumination(Player player) {
		super(player);

		this.range = getDayFactor(getConfig().getDouble("Abilities.Fire.Illumination.Range"));
		this.cooldown = getConfig().getLong("Abilities.Fire.Illumination.Cooldown");
		this.lightThreshold = getConfig().getInt("Abilities.Fire.Illumination.LightThreshold");

		Illumination oldIllumination = getAbility(player, Illumination.class);
		if (oldIllumination != null) {
			oldIllumination.remove();
			return;
		}

		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (player.getLocation().getBlock().getLightLevel() < this.lightThreshold) {
			oldLevel = player.getLocation().getBlock().getLightLevel();
			bPlayer.addCooldown(this);
			set();
			start();
		}

	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (!bPlayer.isIlluminating()) {
			remove();
			return;
		}

		if (bPlayer.hasElement(Element.EARTH) && bPlayer.isTremorSensing()) {
			remove();
			return;
		}

		if (oldLevel > this.lightThreshold) {
			remove();
			return;
		}
		
		if (block == null) {
			return;
		}
		
		if (!player.getWorld().equals(block.getBlock().getWorld())) {
			remove();
			return;
		} 
		
		if (player.getLocation().distanceSquared(block.getLocation()) > range * range) {
			remove();
			return;
		}

		set();
	}

	@Override
	public void remove() {
		super.remove();
		revert();
	}

	private void revert() {
		if (block != null) {
			BLOCKS.remove(block);
			block.revertBlock();
		}
	}

	private void set() {
		Block standingBlock = player.getLocation().getBlock();
		Block standBlock = standingBlock.getRelative(BlockFace.DOWN);

		if (!BlazeArc.isIgnitable(player, standingBlock)) {
			return;
		} else if (!GeneralMethods.isSolid(standBlock)) {
			return;
		} else if (block != null && standingBlock.equals(block.getBlock())) {
			return;
		} else if (standBlock.getType() == Material.LEAVES || standBlock.getType() == Material.LEAVES_2) {
			return;
		}
		
		revert();
		this.block = new TempBlock(standingBlock, Material.TORCH, (byte)0);
		BLOCKS.put(block, player);
	}

	@Override
	public String getName() {
		return "Illumination";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	public byte getNormalData() {
		return normalData;
	}

	public void setNormalData(byte normalData) {
		this.normalData = normalData;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public Material getNormalType() {
		return normalType;
	}

	public void setNormalType(Material normalType) {
		this.normalType = normalType;
	}

	public TempBlock getBlock() {
		return block;
	}

	public void setBlock(TempBlock block) {
		this.block = block;
	}

	public static Map<TempBlock, Player> getBlocks() {
		return BLOCKS;
	}

	/**
	 * Returns whether the block provided is a torch created by Illumination
	 * 
	 * @param block The block being tested
	 */
	public static boolean isIlluminationTorch(Block block) {
		for (TempBlock b : BLOCKS.keySet()) {
			if (b.getBlock().equals(block)) {
				return true;
			}
		}
		return false;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
