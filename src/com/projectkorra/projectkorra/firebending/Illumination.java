package com.projectkorra.projectkorra.firebending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.TempBlock;

public class Illumination extends FireAbility {
	
	private static final Map<TempBlock, Player> BLOCKS = new ConcurrentHashMap<>();

	private byte normalData;
	private long cooldown;
	private double range;
	private Material normalType;
	private TempBlock block;
	
	public Illumination(Player player) {
		super(player);
		
		Illumination oldIllum = getAbility(player, Illumination.class);
		if (oldIllum != null) {
			oldIllum.remove();
			return;
		}
		
		if (!bPlayer.isIlluminating()) {
			remove();
			return;
		}
		
		this.range = getConfig().getDouble("Abilities.Fire.Illumination.Range");
		this.cooldown = getConfig().getLong("Abilities.Fire.Illumination.Cooldown");
		
		this.range = getDayFactor(this.range);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		set();
		start();
		bPlayer.addCooldown(this);
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
			TempBlock.removeBlock(block.getBlock());
		}
	}

	@SuppressWarnings("deprecation")
	private void set() {
		Block standingBlock = player.getLocation().getBlock();
		Block standBlock = standingBlock.getRelative(BlockFace.DOWN);
		
		if (standBlock.getType() == Material.GLOWSTONE) {
			revert();
		} else if ((BlazeArc.isIgnitable(player, standingBlock) 
				&& standBlock.getType() != Material.LEAVES && standBlock .getType() != Material.LEAVES_2) 
				&& block == null && !BLOCKS.containsKey(standBlock)) {
			
			normalType = standingBlock.getType();
			normalData = standingBlock.getData();
			block = new TempBlock(standingBlock, Material.TORCH, (byte)0);
			
			block.setType(Material.TORCH);
			BLOCKS.put(block, player);
		} else if ((BlazeArc.isIgnitable(player, standingBlock) 
				&& standBlock.getType() != Material.LEAVES && standBlock .getType() != Material.LEAVES_2)
				&& !block.equals(standBlock)
				&& !BLOCKS.containsKey(standBlock)
				&& GeneralMethods.isSolid(standBlock)) {
			revert();
			
			normalType = standingBlock.getType();
			normalData = standingBlock.getData();
			block = new TempBlock(standingBlock, Material.TORCH, (byte)0);
			
			block.setType(Material.TORCH);
			BLOCKS.put(block, player);
		} else if (block == null) {
			return;
		} else if (!player.getWorld().equals(block.getBlock().getWorld())) {
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

	public TempBlock getBlock() {
		return block;
	}

	public void setBlock(TempBlock block) {
		this.block = block;
	}

	public static Map<TempBlock, Player> getBlocks() {
		return BLOCKS;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}