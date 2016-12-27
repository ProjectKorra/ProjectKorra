package com.projectkorra.projectkorra.waterbending;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.util.TempBlock;

public class PhaseChange extends IceAbility {
	
	public static enum PhaseChangeType {
		FREEZE, MELT, SKATE;
		
		@Override
		public String toString() {
			if (this == FREEZE) {
				return "Freeze";
			} else if (this == MELT) {
				return "Melt";
			} else if (this == SKATE) {
				return "Skate";
			}
			return "";
		}
	}
	
	private List<PhaseChangeType> active_types = new ArrayList<>();
	private static Map<TempBlock, Player> PLAYER_BY_BLOCK = new HashMap<>();
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
	private int meltRadius;
	private int meltMaxRadius = 7;
	private int meltDelay = 50;
	private boolean allowMeltFlow;
	private long lastBlockTime = 0;
	private CopyOnWriteArrayList<Block> melted_blocks = new CopyOnWriteArrayList<>();
	
	/*Skate Variables
	private long skateCooldown = 7000;
	private int skateRadius = 1;
	private long duration = 7000;
	private double speed = 0.335;
	*/
	
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
				if (tb.getLocation().getWorld() != player.getWorld()) {
					tb.revertBlock();
					blocks.remove(tb);
					PLAYER_BY_BLOCK.remove(tb);
				} else if (tb.getLocation().distanceSquared(player.getLocation()) > (controlRadius*controlRadius)) {
					tb.revertBlock();
					blocks.remove(tb);
					PLAYER_BY_BLOCK.remove(tb);
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
				bPlayer.addCooldown("PhaseChangeMelt", meltCooldown);
				return;
			}
			if (meltRadius >= meltMaxRadius) {
				meltRadius = 1;
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
		setFields(type);
	}
	
	public void setFields(PhaseChangeType type) {
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
			
			freezeArea(GeneralMethods.getTargetedLocation(player, sourceRange));
		} else if (type == PhaseChangeType.MELT) {
			meltRadius = 1;
			meltCooldown = getConfig().getLong("Abilities.Water.PhaseChange.Melt.Cooldown");
			meltDelay = getConfig().getInt("Abilities.Water.PhaseChange.Melt.Delay")/night;
			meltMaxRadius = night*getConfig().getInt("Abilities.Water.PhaseChange.Melt.Radius");
			allowMeltFlow = getConfig().getBoolean("Abilities.Water.PhaseChange.Melt.AllowFlow");
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
	
	/*public void displaySkateParticles() {
		Location right = GeneralMethods.getRightSide(player.getLocation(), 0.3);
		Location left = GeneralMethods.getLeftSide(player.getLocation(), 0.3);
		
		ParticleEffect.SNOW_SHOVEL.display(right, 0, 0, 0, 0.00012F, 1);
		ParticleEffect.SNOW_SHOVEL.display(left, 0, 0, 0, 0.00012F, 1);
	}*/
	
	public void resetMeltLocation(Location loc) {
		if (meltLoc == null) {
			meltLoc = loc;
			return;
		}
		
		if (meltLoc.distance(loc) < 1) {
			return;
		}

		if (!loc.equals(meltLoc)) {
			meltLoc = loc;
			meltRadius = 1;
		}
	}
	
	public ArrayList<BlockFace> getBlockFacesTowardsPlayer(Location center) {
		ArrayList<BlockFace> faces = new ArrayList<>();
		Vector toPlayer = GeneralMethods.getDirection(center, player.getEyeLocation());
		double[] vars = {toPlayer.getX(), toPlayer.getY(), toPlayer.getZ()};
		for (int i = 0; i < 3; i++) {
			if (vars[i] != 0) {
				faces.add(getBlockFaceFromValue(i, vars[i]));
			} else {
				continue;
			}
		}
		return faces;
	}
	
	private BlockFace getBlockFaceFromValue(int xyz, double value) {
		switch (xyz) {
			case 0:
				if (value > 0) {
					return BlockFace.EAST;
				} else if (value < 0) {
					return BlockFace.WEST;
				}
			case 1:
				if (value > 0) {
					return BlockFace.UP;
				} else if (value < 0) {
					return BlockFace.DOWN;
				}
			case 2:
				if (value > 0) {
					return BlockFace.SOUTH;
				} else if (value < 0) {
					return BlockFace.NORTH;
				}
			default: return null;
		}
	}
	
	public ArrayList<Block> getBlocksToFreeze(Location center, int radius) {
		ArrayList<Block> blocks = new ArrayList<>();
		for (Location l : GeneralMethods.getCircle(center, radius, depth, false, true, 0)) {
			Block b = l.getBlock();
			loop: for (int i = 1; i <= depth; i++) {
				for (BlockFace face : getBlockFacesTowardsPlayer(center)) {
					if (b.getRelative(face, i).getType() == Material.AIR) {
						blocks.add(b);
						break loop;
					}
				}
			}
		}
		return blocks;
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
		
		ArrayList<Block> toFreeze = getBlocksToFreeze(center, radius);
		for (Block b : toFreeze) {
			freeze(b);
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
		if (b.getWorld() != player.getWorld()) {
			return;
		}
		
		if (b.getLocation().distanceSquared(player.getLocation()) > controlRadius*controlRadius) {
			return;
		}
		
		if (GeneralMethods.isRegionProtectedFromBuild(player, b.getLocation())) {
			return;
		}
		
		if (!isWater(b)) {
			return;
		}
		
		TempBlock tb = null;
		
		if (TempBlock.isTempBlock(b)) {
			tb = TempBlock.get(b);
			if (melted_blocks.contains(tb.getBlock())) {
				melted_blocks.remove(tb.getBlock());
				tb.revertBlock();
				tb.setType(Material.ICE);
			}
		}
		if (tb == null) {
			tb = new TempBlock(b, Material.ICE, (byte)0);
		}
		blocks.add(tb);
		PLAYER_BY_BLOCK.put(tb, player);
	}
	
	public void meltArea(Location center, int radius) {
		if (System.currentTimeMillis() < lastBlockTime + meltDelay) {
			return;
		}

		List<Block> ice = new ArrayList<Block>();
		for (Location l : GeneralMethods.getCircle(center, radius, 3, true, true, 0)) {
			if (isIce(l.getBlock()) || isSnow(l.getBlock())) {
				ice.add(l.getBlock());
			}
		}

		lastBlockTime = System.currentTimeMillis();
		if (ice.size() == 0) {
			meltRadius++;
			return;
		}

		Block b = ice.get(r.nextInt(ice.size()));
		melt(b);

	}
	
	public void meltArea(Location center) {
		meltArea(center, meltRadius);
	}
	
	@SuppressWarnings("deprecation")
	public void melt(Block b) {
		if (b.getWorld() != player.getWorld()) {
			return;
		}
		if (b.getLocation().distanceSquared(player.getLocation()) > controlRadius*controlRadius) {
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, b.getLocation())) {
			return;
		}
		if (SurgeWall.getWallBlocks().containsKey(b)) {
			return;
		}
		if (SurgeWave.isBlockWave(b)) {
			return;
		}
		if (!SurgeWave.canThaw(b)) {
			SurgeWave.thaw(b);
			return;
		}
		if (!Torrent.canThaw(b)) {
			Torrent.thaw(b);
			return;
		}
		if (WaterArmsSpear.canThaw(b)) {
			WaterArmsSpear.thaw(b);
			return;
		}
		if (TempBlock.isTempBlock(b)) {
			TempBlock tb = TempBlock.get(b);
			
			if (!isIce(tb.getBlock()) && !isSnow(tb.getBlock())) {
				return;
			}
			
			if (PLAYER_BY_BLOCK.containsKey(tb)) {
				thaw(tb);
			}
		} else if (isWater(b)) {
			//Figure out what to do here also
		} else if (isIce(b)) {
			Material m = allowMeltFlow ? Material.WATER : Material.STATIONARY_WATER;
			b.setType(m);
			melted_blocks.add(b);
		} else if (isSnow(b)) {
			byte data = b.getData();
			Material m = allowMeltFlow ? Material.WATER : Material.STATIONARY_WATER;
			b.setType(m);
			b.setData(data);
			melted_blocks.add(b);
		}
	}
	
	/**
	 * Only works with PhaseChange frozen blocks!
	 * @param tb TempBlock being thawed
	 * @return true when it is thawed successfully
	 */
	public static boolean thaw(TempBlock tb) {
		if (!PLAYER_BY_BLOCK.containsKey(tb)) {
			return false;
		} else {
			Player p = PLAYER_BY_BLOCK.get(tb);
			PhaseChange pc = getAbility(p, PhaseChange.class);
			PLAYER_BY_BLOCK.remove(tb);
			pc.getFrozenBlocks().remove(tb);
			tb.revertBlock();
			return true;
		}
	}
	
	/**
	 * Only works if the block is a {@link TempBlock} and PhaseChange frozen!
	 * @param b Block being thawed
	 * @return false if not a {@link TempBlock}
	 */
	public static boolean thaw(Block b) {
		if (!TempBlock.isTempBlock(b)) {
			return false;
		}
		TempBlock tb = TempBlock.get(b);
		return thaw(tb);
	}
	
	public static Map<TempBlock, Player> getFrozenBlocksMap() {
		return PLAYER_BY_BLOCK;
	}
	
	public static List<TempBlock> getFrozenBlocksAsTempBlock() {
		List<TempBlock> list = new ArrayList<>();
		list.addAll(PLAYER_BY_BLOCK.keySet());
		return list;
	}
	
	public static List<Block> getFrozenBlocksAsBlock() {
		List<Block> list = new ArrayList<>();
		for (TempBlock tb : PLAYER_BY_BLOCK.keySet()) {
			Block b = tb.getBlock();
			list.add(b);
		}
		return list;
	}
	
	public void revertFrozenBlocks() {
		if (active_types.contains(PhaseChangeType.FREEZE)) {
			for (TempBlock tb : blocks) {
				tb.revertBlock();
			}
			blocks.clear();
			PLAYER_BY_BLOCK.remove(player);
		}
	}
	
	public void revertMeltedBlocks() {
		if (active_types.contains(PhaseChangeType.MELT)) {
			for (Block b : melted_blocks) {
				if (TempBlock.isTempBlock(b)) {
					TempBlock.get(b).revertBlock();
				} else {
					b.setType(Material.ICE);
				}
			}
			melted_blocks.clear();
		} 
	}
	
	@Override
	public void remove() {
		super.remove();
		revertFrozenBlocks();
		revertMeltedBlocks();
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public String getName() {
		return "PhaseChange";
	}

	@Override
	public Location getLocation() {
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
	
	public CopyOnWriteArrayList<Block> getMeltedBlocks() {
		return melted_blocks;
	}
	
	public List<PhaseChangeType> getActiveTypes() {
		return active_types;
	}
}
