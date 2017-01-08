package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

public class Collapse extends EarthAbility {

	private int distance;
	private int height;
	private long time;
	private long cooldown;
	private double selectRange;
	private double speed;
	private Location origin;
	private Location location;
	private Vector direction;
	private Block block;
	private Map<Block, Block> affectedBlocks;

	public Collapse(Player player) {
		super(player);
		setFields();

		if (!bPlayer.canBend(this) || bPlayer.isOnCooldown("CollapsePillar")) {
			return;
		}

		block = BlockSource.getEarthSourceBlock(player, selectRange, ClickType.LEFT_CLICK);
		if (block == null) {
			return;
		}

		this.origin = block.getLocation();
		this.location = origin.clone();
		this.distance = getEarthbendableBlocksLength(block, direction.clone().multiply(-1), height);
		loadAffectedBlocks();

		if (bPlayer.isAvatarState()) {
			this.height = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.Collapse.Column.Height");
		}
		if (distance != 0) {
			start();
			bPlayer.addCooldown("CollapsePillar", cooldown);
			time = System.currentTimeMillis() - (long) (1000.0 / speed);
		} else {
			remove();
		}
	}

	public Collapse(Player player, Location origin) {
		super(player);
		setFields();
		this.origin = origin;
		this.player = player;
		this.block = origin.getBlock();
		this.location = origin.clone();
		this.distance = getEarthbendableBlocksLength(block, direction.clone().multiply(-1), height);
		loadAffectedBlocks();

		if (distance != 0) {
			start();
			time = System.currentTimeMillis() - (long) (1000.0 / speed);
		} else {
			remove();
		}
	}

	private void setFields() {
		this.height = getConfig().getInt("Abilities.Earth.Collapse.Column.Height");
		this.selectRange = getConfig().getInt("Abilities.Earth.Collapse.SelectRange");
		this.speed = getConfig().getDouble("Abilities.Earth.Collapse.Speed");
		this.cooldown = getConfig().getLong("Abilities.Earth.Collapse.Column.Cooldown");
		this.direction = new Vector(0, -1, 0);
		this.affectedBlocks = new ConcurrentHashMap<>();
	}

	private void loadAffectedBlocks() {
		affectedBlocks.clear();
		Block thisBlock;

		for (int i = 0; i <= distance; i++) {
			thisBlock = block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(-i)));
			affectedBlocks.put(thisBlock, thisBlock);
			if (RaiseEarth.blockInAllAffectedBlocks(thisBlock)) {
				RaiseEarth.revertBlock(thisBlock);
			}
		}
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (Collapse collapse : getAbilities(Collapse.class)) {
			if (collapse.affectedBlocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}

	public static void revert(Block block) {
		for (Collapse collapse : getAbilities(Collapse.class)) {
			collapse.affectedBlocks.remove(block);
		}
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - time >= (long) (1000.0 / speed)) {
			time = System.currentTimeMillis();
			if (!tryToMoveEarth()) {
				remove();
				return;
			}
		}
	}

	private boolean tryToMoveEarth() {
		Block block = location.getBlock();
		location = location.add(direction);
		if (distance == 0) {
			return false;
		}

		moveEarth(block, direction, distance);
		loadAffectedBlocks();
		return location.distanceSquared(origin) < distance * distance;
	}

	@Override
	public String getName() {
		return "Collapse";
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

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		for (Block block : affectedBlocks.values()) {
			locations.add(block.getLocation());
		}
		return locations;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
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

	public double getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(double selectRange) {
		this.selectRange = selectRange;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public Map<Block, Block> getAffectedBlocks() {
		return affectedBlocks;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
}
