package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Tremorsense extends EarthAbility {

	private static final Map<Block, Player> BLOCKS = new ConcurrentHashMap<Block, Player>();

	private byte lightThreshold;
	private int maxDepth;
	private int radius;
	private long cooldown;
	private Block block;
	
	public Tremorsense(Player player) {
		super(player);
		
		this.maxDepth = getConfig().getInt("Abilities.Earth.Tremorsense.MaxDepth");
		this.radius = getConfig().getInt("Abilities.Earth.Tremorsense.Radius");
		this.lightThreshold = (byte) getConfig().getInt("Abilities.Earth.Tremorsense.LightThreshold");
		this.cooldown = getConfig().getLong("Abilities.Earth.Tremorsense.Cooldown");

		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		byte lightLevel = player.getLocation().getBlock().getLightLevel();

		if (lightLevel < this.lightThreshold && isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			bPlayer.addCooldown(this);
			activate();
			start();
		}
	}

	private void activate() {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				boolean earth = false;
				boolean foundAir = false;
				Block smokeBlock = null;
				
				for (int k = 0; k <= maxDepth; k++) {
					Block blocki = block.getRelative(BlockFace.EAST, i).getRelative(BlockFace.NORTH, j).getRelative(BlockFace.DOWN, k);
					if (GeneralMethods.isRegionProtectedFromBuild(this, blocki.getLocation())) {
						continue;
					}
					if (isEarthbendable(blocki) && !earth) {
						earth = true;
						smokeBlock = blocki;
					} else if (!isEarthbendable(blocki) && earth) {
						foundAir = true;
						break;
					} else if (!isEarthbendable(blocki) && !earth && blocki.getType() != Material.AIR) {
						break;
					}
				}
				if (foundAir) {
					smokeBlock.getWorld().playEffect(smokeBlock.getRelative(BlockFace.UP).getLocation(), Effect.SMOKE, 4, radius);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void tryToSetGlowBlock() {
		Block standBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		if (!bPlayer.isTremorSensing()) {
			if (block != null) {
				remove();
			}
			return;
		}
		
		boolean isBendable = isEarthbendable(standBlock);

		if (isBendable && block == null) {
			block = standBlock;
			player.sendBlockChange(block.getLocation(), 89, (byte) 1);
		} else if (isBendable && !block.equals(standBlock)) {
			revertGlowBlock();
			block = standBlock;
			player.sendBlockChange(block.getLocation(), 89, (byte) 1);
		} else if (block == null) {
			return;
		} else if (!player.getWorld().equals(block.getWorld())) {
			remove();
			return;
		} else if (!isBendable) {
			revertGlowBlock();
			return;
		}
	}

	@SuppressWarnings("deprecation")
	public void revertGlowBlock() {
		if (block != null) {
			player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		revertGlowBlock();
	}
	
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || player.getLocation().getBlock().getLightLevel() > lightThreshold) { 
			remove();
			return;
		} else {
			tryToSetGlowBlock();
		}
	}

	public static void manage(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			
			if (canTremorSense(player) && !hasAbility(player, Tremorsense.class)) {
				new Tremorsense(player);
			}
		}
	}
	
	public static boolean canTremorSense(Player player) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer != null && bPlayer.canBendIgnoreBindsCooldowns(getAbility("Tremorsense"))) {
			return true;
		}
		
		return false;
	}
	
	public static Map<Block, Player> getBlocks() {
		return BLOCKS;
	}

	@Override
	public String getName() {
		return "Tremorsense";
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

	public byte getLightThreshold() {
		return lightThreshold;
	}

	public void setLightThreshold(byte lightThreshold) {
		this.lightThreshold = lightThreshold;
	}

	public int getMaxDepth() {
		return maxDepth;
	}

	public void setMaxDepth(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
