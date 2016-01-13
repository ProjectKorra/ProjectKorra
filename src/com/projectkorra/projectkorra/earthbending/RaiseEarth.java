package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

public class RaiseEarth extends EarthAbility {
	
	private static final ConcurrentHashMap<Block, Block> ALL_AFFECTED_BLOCKS = new ConcurrentHashMap<>();

	private int distance;
	private int height;
	private long time;
	private long interval;
	private long cooldown;
	private double range;
	private double speed;
	private Block block;
	private Vector direction;
	private Location origin;
	private Location location;
	private ConcurrentHashMap<Block, Block> affectedBlocks;
	
	public RaiseEarth(Player player) {
		super(player);
		setFields();
		
		if (!bPlayer.canBend(this)) {
			return;
		}

		try {
			if (bPlayer.isAvatarState()) {
				height = (int) (2.0 / 5.0 * (double) AvatarState.getValue(height));
			}
			block = BlockSource.getEarthSourceBlock(player, range, ClickType.LEFT_CLICK);
			if (block == null) {
				return;
			}
			
			origin = block.getLocation();
			location = origin.clone();
			distance = getEarthbendableBlocksLength(block, direction.clone().multiply(-1), height);
		}
		catch (IllegalStateException e) {
			return;
		}

		loadAffectedBlocks();

		if (distance != 0 && canInstantiate()) {
			bPlayer.addCooldown(this);
			time = System.currentTimeMillis() - interval;
			start();
		}
	}

	public RaiseEarth(Player player, Location origin) {
		this(player, origin, ConfigManager.getConfig().getInt("Abilities.Earth.RaiseEarth.Column.Height"));
	}

	public RaiseEarth(Player player, Location origin, int height) {
		super(player);
		setFields();
		
		this.height = height;
		this.origin = origin;
		this.location = origin.clone();
		this.block = location.getBlock();
		this.distance = getEarthbendableBlocksLength(block, direction.clone().multiply(-1), height);

		loadAffectedBlocks();

		if (distance != 0 && canInstantiate()) {
			time = System.currentTimeMillis() - interval;
			start();
		}
	}
	
	private void setFields() {
		this.height = getConfig().getInt("Abilities.Earth.RaiseEarth.Column.Height");
		this.range = 20;
		this.speed = 8;
		this.direction = new Vector(0, 1, 0);
		this.interval = (long) (1000.0 / speed);
		this.cooldown = GeneralMethods.getGlobalCooldown();
		this.affectedBlocks = new ConcurrentHashMap<Block, Block>();
	}
	
	private boolean canInstantiate() {
		for (Block block : affectedBlocks.keySet()) {
			if (block.getType() == Material.AIR || ALL_AFFECTED_BLOCKS.containsKey(block)) {
				return false;
			}
		}
		return true;
	}

	private void loadAffectedBlocks() {
		affectedBlocks.clear();
		Block thisBlock;
		for (int i = 0; i <= distance; i++) {
			thisBlock = block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(-i)));
			affectedBlocks.put(thisBlock, thisBlock);
			if (Collapse.blockInAllAffectedBlocks(thisBlock)) {
				Collapse.revert(thisBlock);
			}
		}
	}
	
	@Override
	public void progress() {
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			Block block = location.getBlock();
			location = location.add(direction);
			moveEarth(block, direction, distance);
			loadAffectedBlocks();

			if (location.distanceSquared(origin) >= distance * distance) {
				remove();
				return;
			}
		}
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		return ALL_AFFECTED_BLOCKS.containsKey(block);
	}

	public static void revertAffectedBlock(Block block) {
		ALL_AFFECTED_BLOCKS.remove(block);
		for (RaiseEarth raiseEarth : CoreAbility.getAbilities(RaiseEarth.class)) {
			raiseEarth.affectedBlocks.remove(block);
		}
	}

	@Override
	public String getName() {
		return "RaiseEarth";
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

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public ConcurrentHashMap<Block, Block> getAffectedBlocks() {
		return affectedBlocks;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
