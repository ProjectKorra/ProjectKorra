package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Illumination extends FireAbility {
	
	private static final Map<Block, Player> BLOCKS = new ConcurrentHashMap<>();

	private byte normalData;
	private long cooldown;
	private double range;
	private int lightThreshold;
	private Material normalType;
	private Block block;
	private int oldLevel;
	
	public Illumination(Player player) {
		super(player);
		
		this.range = getConfig().getDouble("Abilities.Fire.Illumination.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.Illumination.Cooldown");
		this.range = getDayFactor(this.range);
		this.lightThreshold = getConfig().getInt("Abilities.Fire.Illumination.LightThreshold");
		
		Illumination oldIllum = getAbility(player, Illumination.class);
		if (oldIllum != null) {
			oldIllum.remove();
			return;
		}
		
		if (!bPlayer.isIlluminating()) {
			remove();
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
		
		if (oldLevel > this.lightThreshold) {
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

	@SuppressWarnings("deprecation")
	private void revert() {
		if (block != null) {
			BLOCKS.remove(block);
			block.setType(normalType);
			block.setData(normalData);
			oldLevel = player.getLocation().getBlock().getLightLevel();
		}
	}

	@SuppressWarnings("deprecation")
	private void set() {
		Block standingBlock = player.getLocation().getBlock();
		Block standBlock = standingBlock.getRelative(BlockFace.DOWN);
		
		if (standBlock.getType() == Material.GLOWSTONE) {
			revert();
		} else if ((BlazeArc.isIgnitable(player, standingBlock) 
				&& standBlock.getType() != Material.LEAVES && standBlock.getType() != Material.LEAVES_2) 
				&& block == null && !BLOCKS.containsKey(standBlock)) {
			block = standingBlock;
			normalType = block.getType();
			normalData = block.getData();
			
			block.setType(Material.TORCH);
			BLOCKS.put(block, player);
		} else if ((BlazeArc.isIgnitable(player, standingBlock) 
				&& standBlock.getType() != Material.LEAVES && standBlock.getType() != Material.LEAVES_2)
				&& !block.equals(standBlock)
				&& !BLOCKS.containsKey(standBlock)
				&& GeneralMethods.isSolid(standBlock)) {
			revert();
			block = standingBlock;
			normalType = block.getType();
			normalData = block.getData();
			
			block.setType(Material.TORCH);
			BLOCKS.put(block, player);
		} else if (block == null) {
			return;
		} else if (!player.getWorld().equals(block.getWorld())) {
			revert();
		} else if (player.getLocation().distanceSquared(block.getLocation()) > range * range) {
			revert();
		}
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

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public static Map<Block, Player> getBlocks() {
		return BLOCKS;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
