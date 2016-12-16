package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhaseChangeMelt extends IceAbility {

	private static final List<Block> MELTED_BLOCKS = new CopyOnWriteArrayList<Block>();
	private static final byte FULL = 0x0;
	
	private int seaLevel;
	private long cooldown;
	private double range;
	private double radius;
	private double evaporateRadius;
	private Location location;
	
	public PhaseChangeMelt(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		this.seaLevel = getConfig().getInt("Properties.SeaLevel");
		this.range = getConfig().getDouble("Abilities.Water.PhaseChange.Range");
		this.radius = getConfig().getDouble("Abilities.Water.PhaseChange.Radius");
		this.cooldown = getConfig().getLong("Abilities.Water.PhaseChange.Melt.Cooldown");
		this.evaporateRadius = 3;
		
		this.range = getNightFactor(range);
		this.radius = getNightFactor(radius);
		
		if (!bPlayer.canBend(this) || !bPlayer.canIcebend() || bPlayer.isOnCooldown("PhaseChangeMelt")) {
			return;
		}		

		if (bPlayer.isAvatarState()) {
			range = AvatarState.getValue(range);
			radius = AvatarState.getValue(radius);
		}
		
		boolean evaporate = false;
		location = GeneralMethods.getTargetedLocation(player, range, 0, 8, 9);
		if (isWater(player.getTargetBlock((HashSet<Material>) null, (int) range)) && player.getEyeLocation().getBlockY() > seaLevel) {
			evaporate = true;
			radius = (int) getNightFactor(evaporateRadius);
		}
		
		start();
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (evaporate && block.getY() > seaLevel) {
				evaporate(player, block);
			} else {
				melt(player, block);
			}
		}
		
		bPlayer.addCooldown("PhaseChangeMelt", cooldown);
		remove();
	}

	@SuppressWarnings("deprecation")
	public static void melt(Player player, final Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation())) {
			return;
		} else if (!SurgeWave.canThaw(block)) {
			SurgeWave.thaw(block);
			return;
		} else if (!Torrent.canThaw(block)) {
			Torrent.thaw(block);
			return;
		} else if (WaterArmsSpear.canThaw(block)) {
			WaterArmsSpear.thaw(block);
			return;
		}
		
		WaterSpoutWave.thaw(block);
		WaterCombo.thaw(block);
		
		if (isMeltable(block) && !TempBlock.isTempBlock(block) && WaterManipulation.canPhysicsChange(block)) {
			if (block.getType() == Material.SNOW) {
				block.setType(Material.AIR);
				return;
			} else if (PhaseChangeFreeze.getFrozenBlocks().containsKey(block)) {
				PhaseChangeFreeze.thaw(block);
			} else {
				MELTED_BLOCKS.add(block);
				block.setType(Material.WATER);
				block.setData(FULL);
				
				new BukkitRunnable() {
					@Override
					public void run() {
						MELTED_BLOCKS.remove(block);
						block.setType(Material.ICE);
					}
				}.runTaskLater(ProjectKorra.plugin, 5 * 20 * 60);
			}
		}
	}

	public static void evaporate(Player player, Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation())) {
			return;
		} else if (isWater(block) && !TempBlock.isTempBlock(block) && WaterManipulation.canPhysicsChange(block)) {
			block.setType(Material.AIR);
			block.getWorld().playEffect(block.getLocation(), Effect.SMOKE, 1);
		}
	}
	
	public static void removeAllCleanup() {
		for (Block b : MELTED_BLOCKS) {
			b.setType(Material.ICE);
			MELTED_BLOCKS.remove(b);
		}
	}

	@Override
	public String getName() {
		return "PhaseChange";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return location;
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
		return false;
	}

	public int getSeaLevel() {
		return seaLevel;
	}

	public void setSeaLevel(int seaLevel) {
		this.seaLevel = seaLevel;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getEvaporateRadius() {
		return evaporateRadius;
	}

	public void setEvaporateRadius(double evaporateRadius) {
		this.evaporateRadius = evaporateRadius;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
	public static List<Block> getMeltedBlocks() {
		return MELTED_BLOCKS;
	}
	
}
