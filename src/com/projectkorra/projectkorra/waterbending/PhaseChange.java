package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

public class PhaseChange extends IceAbility {
	
	public static enum PhaseChangeType {
		FREEZE, MELT, SKATE, PASSIVE;
		
		@Override
		public String toString() {
			if (this == FREEZE) {
				return "Freeze";
			} else if (this == MELT) {
				return "Melt";
			} else if (this == SKATE) {
				return "Skate";
			} else if (this == PASSIVE) {
				return "Passive";
			}
			return "";
		}
	}
	
	private List<PhaseChangeType> active_types = new ArrayList<>();
	private static Map<Player, List<TempBlock>> BLOCKS_BY_PLAYER = new HashMap<>();
	private CopyOnWriteArrayList<TempBlock> blocks = new CopyOnWriteArrayList<>();
	private Random r = new Random();
	
	private int sourceRange = 8;
	
	//Freeze Variables
	private long freezeCooldown = 500;
	private int freezeRadius = 3;
	private int depth = 1;
	private double controlRadius = 25;
	
	//Melt Variables
	private Location meltLoc;
	private long meltCooldown = 7000;
	private int meltRadius = 1;
	private int meltMaxRadius = 7;
	private int meltDelay = 50;
	private long lastBlockTime = 0;
	private CopyOnWriteArrayList<TempBlock> melted_blocks = new CopyOnWriteArrayList<>();
	
	/*Skate Variables
	private long skateCooldown = 7000;
	private int skateRadius = 1;
	private long duration = 7000;
	private double speed = 0.335;
	*/
	
	//Passive Variables
	private CopyOnWriteArrayList<TempBlock> passive_blocks = new CopyOnWriteArrayList<>();
	
	public PhaseChange(Player player, PhaseChangeType type) {
		super(player);
		startNewType(type);
		start();
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}
		
		if (active_types.contains(PhaseChangeType.FREEZE)) {
			if (blocks.isEmpty()) {
				active_types.remove(PhaseChangeType.FREEZE);
				return;
			}
			
			for (TempBlock tb : blocks) {
				if (tb.getLocation().distanceSquared(player.getLocation()) > (controlRadius*controlRadius)) {
					tb.revertBlock();
					blocks.remove(tb);
					BLOCKS_BY_PLAYER.get(player).remove(tb);
				}
			}
		} 
		
		if (active_types.contains(PhaseChangeType.MELT)) {
			if (active_types.contains(PhaseChangeType.SKATE)) {
				active_types.remove(PhaseChangeType.MELT);
				return;
			}
			if (!player.isSneaking()) {
				active_types.remove(PhaseChangeType.MELT);
				return;
			}
			if (meltRadius >= meltMaxRadius) {
				active_types.remove(PhaseChangeType.MELT);
				bPlayer.addCooldown("PhaseChangeMelt", meltCooldown);
				return;
			}
			
			Location l = GeneralMethods.getTargetedLocation(player, sourceRange);
			resetMeltLocation(l);
			meltArea(l, meltRadius);
		} 
		
		/*if (active_types.contains(PhaseChangeType.SKATE)) {
			if (!player.isSprinting()) {
				active_types.remove(PhaseChangeType.SKATE);
				bPlayer.addCooldown("PhaseChangeSkate", skateCooldown);
				return;
			}
			
			if (System.currentTimeMillis() > getStartTime() + duration) {
				return;
			}
			
			Location center = player.getLocation().clone().subtract(0, 1, 0);
			if (isWater(center.getBlock()) || isIce(center.getBlock())) {
				freezeArea(center, skateRadius, PhaseChangeType.SKATE);
				Vector v = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());
				player.setVelocity(v.normalize().multiply(speed));
				displaySkateParticles();
			}
		}*/
		
		if (active_types.contains(PhaseChangeType.PASSIVE)) {
			if (!bPlayer.getBoundAbilityName().equals("PhaseChange")) {
				active_types.remove(PhaseChangeType.PASSIVE);
				return;
			}
			checkPassive();
		}
		
		if (active_types.isEmpty()) {
			remove();
		}
	}
	
	public void startNewType(PhaseChangeType type) {
		if (type == PhaseChangeType.MELT) {
			if (bPlayer.isOnCooldown("PhaseChangeMelt")) {
				return;
			}
		}
		
		active_types.add(type);
		loadVariables(type);
	}
	
	public void loadVariables(PhaseChangeType type) {
		int night = 1;
		if (isNight(player.getWorld())) {
			night = (int) Math.round(getNightFactor());
		}
		sourceRange = night*getConfig().getInt("Abilities.Water.PhaseChange.SourceRange");
		
		if (type == PhaseChangeType.FREEZE) {
			depth = night*getConfig().getInt("Abilities.Water.PhaseChange.Freeze.Depth");
			controlRadius = night*getConfig().getDouble("Abilities.Water.PhaseChange.Freeze.ControlRadius");
			freezeCooldown = getConfig().getLong("Abilities.Water.PhaseChange.Freeze.Cooldown");
			freezeRadius = night*getConfig().getInt("Abilities.Water.PhaseChange.Freeze.Radius");
			
			if (!BLOCKS_BY_PLAYER.containsKey(player)) {
				BLOCKS_BY_PLAYER.put(player, blocks);
			}
			
			freezeArea(player.getTargetBlock((Set<Material>)null, sourceRange).getLocation());
		} else if (type == PhaseChangeType.MELT) {
			meltCooldown = getConfig().getLong("Abilities.Water.PhaseChange.Melt.Cooldown");
			meltDelay = getConfig().getInt("Abilities.Water.PhaseChange.Melt.Delay")/night;
			meltMaxRadius = night*getConfig().getInt("Abilities.Water.PhaseChange.Melt.Radius");
		/*} else if (type == PhaseChangeType.SKATE) {
			if (bPlayer.isOnCooldown("PhaseChangeSkate")) {
				return;
			}
			duration = night*getConfig().getLong("Abilities.Water.PhaseChange.Skate.Duration");
			speed = night*getConfig().getDouble("Abilities.Water.PhaseChange.Skate.Speed");
			skateCooldown = getConfig().getLong("Abilities.Water.PhaseChange.Skate.Cooldown");
			skateRadius = night*getConfig().getInt("Abilities.Water.PhaseChange.Skate.Radius");
			
			freezeArea(player.getLocation().clone().subtract(0, 1, 0), skateRadius, PhaseChangeType.SKATE);*/
		}
	}
	
	public void checkPassive() {
		Location feet = player.getLocation().clone();
		Location head = feet.clone().add(0, 1, 0);
		Vector direction = new Vector(player.getLocation().getDirection().getX(), 0, player.getLocation().getDirection().getZ());
		Location f1 = feet.clone().add(direction.multiply(1));
		Location f2 = head.clone().add(direction.multiply(1));
		Location[] checks = {feet, head, f1, f2};
		
		if (!passive_blocks.isEmpty()) {
			for (TempBlock tb : passive_blocks) {
				for (Location l : checks) {
					if (tb.getLocation() != l.getBlock().getLocation()) {
						tb.revertBlock();
						passive_blocks.remove(tb);
					}
				}
			}
		}
		
		for (Location l : checks) {
			if (TempBlock.isTempBlock(l.getBlock())) {
				TempBlock tb = TempBlock.get(l.getBlock());
				if (isIce(l.getBlock())) {
					tb.revertBlock();
				}
				if (blocks.contains(tb)) {
					blocks.remove(tb);
					BLOCKS_BY_PLAYER.get(player).remove(tb);
				}
			} else if (isIce(l.getBlock())) {
				TempBlock tb = new TempBlock(l.getBlock(), Material.AIR, (byte)0);
				passive_blocks.add(tb);
			}
		}
	}
	
	public void displaySkateParticles() {
		Location right = GeneralMethods.getRightSide(player.getLocation(), 0.3);
		Location left = GeneralMethods.getLeftSide(player.getLocation(), 0.3);
		
		ParticleEffect.SNOW_SHOVEL.display(right, 0, 0, 0, 0.00012F, 1);
		ParticleEffect.SNOW_SHOVEL.display(left, 0, 0, 0, 0.00012F, 1);
	}
	
	public void resetMeltLocation(Location loc) {
		if (meltLoc == null) {
			meltLoc = loc;
			return;
		}

		if (!loc.equals(meltLoc)) {
			meltRadius = 1;
			meltLoc = loc;
		}
	}
	
	public void freezeArea(Location center, int radius, PhaseChangeType type) {
		if (type == PhaseChangeType.FREEZE) {
			if (bPlayer.isOnCooldown("PhaseChangeFreeze")) {
				return;
			}
		}
		
		if (depth > 1) {
			center.subtract(0, depth-1, 0);
		}
		
		for (Location l : GeneralMethods.getCircle(center, radius, depth, false, false, 0)) {
			freeze(l.getBlock());
		}
		if (!blocks.isEmpty()) {
			if (type == PhaseChangeType.FREEZE) {
				bPlayer.addCooldown("PhaseChangeFreeze", freezeCooldown);
			}
		}
	}
	
	public void freezeArea(Location center, int radius) {
		freezeArea(center, radius, PhaseChangeType.FREEZE);
	}
	
	public void freezeArea(Location center, PhaseChangeType type) {
		freezeArea(center, freezeRadius, type);
	}
	
	public void freezeArea(Location center) {
		freezeArea(center, freezeRadius);
	}
	
	public void freeze(Block b) {
		if (!isWater(b)) {
			return;
		}
		
		TempBlock tb = new TempBlock(b, Material.ICE, (byte)0);
		blocks.add(tb);
		BLOCKS_BY_PLAYER.get(player).add(tb);
	}
	
	public void meltArea(Location center, int radius) {
		if (System.currentTimeMillis() < lastBlockTime + meltDelay) {
			return;
		}

		List<Block> ice = new ArrayList<Block>();
		for (Location l : GeneralMethods.getCircle(center, radius, 3, true, true, 0)) {
			if (isIce(l.getBlock())) {
				ice.add(l.getBlock());
			}
		}

		lastBlockTime = System.currentTimeMillis();
		if (ice.size() == 0) {
			meltRadius++;
			return;
		}

		Block b = ice.get(r.nextInt(ice.size()));
		TempBlock tb;

		if (TempBlock.isTempBlock(b)) {
			tb = TempBlock.get(b);
			if (blocks.contains(tb)) {
				blocks.remove(tb);
			}
			tb.setType(Material.WATER);
			
		} else {
			tb = new TempBlock(b, Material.WATER, (byte)0);
		}

		if (!melted_blocks.contains(tb)) {
			melted_blocks.add(tb);
		}
	}
	
	public void meltArea(Location center) {
		meltArea(center, meltRadius);
	}
	
	public void melt(Block b) {
		if (TempBlock.isTempBlock(b)) {
			TempBlock tb = TempBlock.get(b);
			
			if (!blocks.contains(tb)) {
				if (isWater(tb.getBlock())) {
					//Figure out what to do here
				}
				return;
			}
			
			tb.revertBlock();
			blocks.remove(tb);
			BLOCKS_BY_PLAYER.get(player).remove(tb);
		} else if (isWater(b)) {
			//Figure out what to do here also
		} else if (isIce(b)) {
			TempBlock tb = new TempBlock(b, Material.WATER, (byte)0);
			melted_blocks.add(tb);
		}
	}
	
	public static void thaw(TempBlock tb) {
		for (Player p : BLOCKS_BY_PLAYER.keySet()) {
			if (!BLOCKS_BY_PLAYER.get(p).contains(tb)) {
				continue;
			}
			tb.revertBlock();
			BLOCKS_BY_PLAYER.get(p).remove(tb);
			getAbility(p, PhaseChange.class).getFrozenBlocks().remove(tb);
			return;
		}
	}
	
	public static void thaw(Block b) {
		if (!TempBlock.isTempBlock(b)) {
			return;
		}
		TempBlock tb = TempBlock.get(b);
		thaw(tb);
	}
	
	public static Map<Player, List<TempBlock>> getFrozenBlocksMap() {
		return BLOCKS_BY_PLAYER;
	}
	
	public static List<TempBlock> getFrozenBlocksAsTempBlock() {
		List<TempBlock> list = new ArrayList<>();
		for (Player p : BLOCKS_BY_PLAYER.keySet()) {
			list.addAll(BLOCKS_BY_PLAYER.get(p));			
		}
		return list;
	}
	
	public static List<Block> getFrozenBlocksAsBlock() {
		List<Block> list = new ArrayList<>();
		for (Player p : BLOCKS_BY_PLAYER.keySet()) {
			for (TempBlock tb : BLOCKS_BY_PLAYER.get(p)) {
				Block b = tb.getBlock();
				list.add(b);
			}
		}
		return list;
	}
	
	public void revertFrozenBlocks() {
		if (active_types.contains(PhaseChangeType.FREEZE)) {
			for (TempBlock tb : blocks) {
				tb.revertBlock();
			}
			blocks.clear();
			BLOCKS_BY_PLAYER.remove(player);
		}
	}
	
	public void revertMeltedBlocks() {
		if (active_types.contains(PhaseChangeType.MELT)) {
			for (TempBlock tb : melted_blocks) {
				tb.revertBlock();
			}
			melted_blocks.clear();
		} 
	}
	
	public void revertPassiveBlocks() {
		if (active_types.contains(PhaseChangeType.PASSIVE)) {
			for (TempBlock tb : passive_blocks) {
				tb.revertBlock();
			}
			passive_blocks.clear();
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		revertFrozenBlocks();
		revertMeltedBlocks();
		revertPassiveBlocks();
	}

	@Override
	public boolean isSneakAbility() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public long getCooldown() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "PhaseChange";
	}

	@Override
	public Location getLocation() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public void setDepth(int value) {
		depth = value;
	}
	
	public int getSourceRange() {
		return sourceRange;
	}
	
	public void setSourceRange(int value) {
		sourceRange = value;
	}
	
	public double getFreezeControlRadius() {
		return controlRadius;
	}
	
	public int getMeltRadius() {
		return meltRadius;
	}
	
	/*public int getSkateFreezeRadius() {
		return skateRadius;
	}*/
	
	public void setFreezeControlRadius(int value) {
		controlRadius = value;
	}
	
	public CopyOnWriteArrayList<TempBlock> getFrozenBlocks() {
		return blocks;
	}
	
	public CopyOnWriteArrayList<TempBlock> getMeltedBlocks() {
		return melted_blocks;
	}
	
	public CopyOnWriteArrayList<TempBlock> getPassiveBlocks() {
		return passive_blocks;
	}
	
	public List<PhaseChangeType> getActiveTypes() {
		return active_types;
	}
}
