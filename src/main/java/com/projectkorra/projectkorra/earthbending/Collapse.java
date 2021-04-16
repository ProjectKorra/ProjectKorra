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
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

public class Collapse extends EarthAbility {

	private int distance;
	@Attribute(Attribute.HEIGHT)
	private int height;
	private long time;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute(Attribute.SPEED)
	private double speed;
	private Location origin;
	private Location location;
	private Vector direction;
	private Block block;
	private Map<Block, Block> affectedBlocks;

	public Collapse(final Player player) {
		super(player);
		this.setFields();

		if (!this.bPlayer.canBend(this) || this.bPlayer.isOnCooldown("CollapsePillar")) {
			return;
		}

		this.block = BlockSource.getEarthSourceBlock(player, this.selectRange, ClickType.LEFT_CLICK);
		if (this.block == null) {
			return;
		}

		this.origin = this.block.getLocation();
		this.location = this.origin.clone();
		this.distance = this.getEarthbendableBlocksLength(this.block, this.direction.clone().multiply(-1), this.height);
		this.loadAffectedBlocks();

		if (this.bPlayer.isAvatarState()) {
			this.height = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.Collapse.Column.Height");
		}
		if (this.distance != 0) {
			this.start();
			this.bPlayer.addCooldown("CollapsePillar", this.cooldown);
			this.time = System.currentTimeMillis() - (long) (1000.0 / this.speed);
		} else {
			this.remove();
		}
	}

	public Collapse(final Player player, final Location origin) {
		super(player);
		this.setFields();
		this.origin = origin;
		this.player = player;
		this.block = origin.getBlock();
		this.location = origin.clone();
		this.distance = this.getEarthbendableBlocksLength(this.block, this.direction.clone().multiply(-1), this.height);
		this.loadAffectedBlocks();

		if (this.distance != 0) {
			this.start();
			this.time = System.currentTimeMillis() - (long) (1000.0 / this.speed);
		} else {
			this.remove();
		}
	}

	private void setFields() {
		this.height = this.bPlayer.isAvatarState() ? getConfig().getInt("Abilities.Avatar.AvatarState.Earth.Collapse.Column.Height") : getConfig().getInt("Abilities.Earth.Collapse.Column.Height");
		this.selectRange = getConfig().getInt("Abilities.Earth.Collapse.SelectRange");
		this.speed = getConfig().getDouble("Abilities.Earth.Collapse.Speed");
		this.cooldown = getConfig().getLong("Abilities.Earth.Collapse.Column.Cooldown");
		this.direction = new Vector(0, -1, 0);
		this.affectedBlocks = new ConcurrentHashMap<>();
	}

	private void loadAffectedBlocks() {
		this.affectedBlocks.clear();
		Block thisBlock;

		for (int i = 0; i <= this.distance; i++) {
			thisBlock = this.block.getWorld().getBlockAt(this.location.clone().add(this.direction.clone().multiply(-i)));
			this.affectedBlocks.put(thisBlock, thisBlock);
			if (RaiseEarth.blockInAllAffectedBlocks(thisBlock)) {
				RaiseEarth.revertAffectedBlock(thisBlock);
			}
		}
	}

	public static boolean blockInAllAffectedBlocks(final Block block) {
		for (final Collapse collapse : getAbilities(Collapse.class)) {
			if (collapse.affectedBlocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}

	public static void revert(final Block block) {
		for (final Collapse collapse : getAbilities(Collapse.class)) {
			collapse.affectedBlocks.remove(block);
		}
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - this.time >= (long) (1000.0 / this.speed)) {
			this.time = System.currentTimeMillis();
			if (!this.tryToMoveEarth()) {
				this.remove();
				return;
			}
		}
	}

	private boolean tryToMoveEarth() {
		final Block block = this.location.getBlock();
		this.location = this.location.add(this.direction);
		if (this.distance == 0) {
			return false;
		}

		this.moveEarth(block, this.direction, this.distance);
		this.loadAffectedBlocks();
		return this.location.distanceSquared(this.origin) < this.distance * this.distance;
	}

	@Override
	public String getName() {
		return "Collapse";
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
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
		final ArrayList<Location> locations = new ArrayList<>();
		for (final Block block : this.affectedBlocks.values()) {
			locations.add(block.getLocation());
		}
		return locations;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public Block getBlock() {
		return this.block;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public int getDistance() {
		return this.distance;
	}

	public void setDistance(final int distance) {
		this.distance = distance;
	}

	public int getHeight() {
		return this.height;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(final long time) {
		this.time = time;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public Map<Block, Block> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
}
