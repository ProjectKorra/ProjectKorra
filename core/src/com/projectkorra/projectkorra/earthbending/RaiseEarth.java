package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.TempBlock;

public class RaiseEarth extends EarthAbility {

	private int distance;
	@Attribute(Attribute.HEIGHT)
	private int height;
	private long time;
	private long interval;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.SELECT_RANGE)
	private double selectRange;
	@Attribute(Attribute.SPEED)
	private double speed;
	private Block block;
	private Vector direction;
	private Location origin;
	private Location location;
	private ConcurrentHashMap<Block, Block> affectedBlocks;

	public RaiseEarth(final Player player) {
		super(player);
		this.setFields();

		if (!this.bPlayer.canBend(this) || this.bPlayer.isOnCooldown("RaiseEarthPillar")) {
			return;
		}

		try {
			if (this.bPlayer.isAvatarState()) {
				this.height = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.RaiseEarth.Column.Height");
			}
			this.block = BlockSource.getEarthSourceBlock(player, this.selectRange, ClickType.LEFT_CLICK);
			if (this.block == null) {
				return;
			}

			this.origin = this.block.getLocation();
			this.location = this.origin.clone();
			this.distance = this.getEarthbendableBlocksLength(this.block, this.direction.clone().multiply(-1), this.height);
		} catch (final IllegalStateException e) {
			return;
		}

		this.loadAffectedBlocks();

		if (this.distance != 0 && this.canInstantiate()) {
			this.bPlayer.addCooldown("RaiseEarthPillar", this.cooldown);
			this.time = System.currentTimeMillis() - this.interval;
			this.start();
		}
	}

	public RaiseEarth(final Player player, final Location origin) {
		this(player, origin, ConfigManager.getConfig().getInt("Abilities.Earth.RaiseEarth.Column.Height"));
	}

	public RaiseEarth(final Player player, final Location origin, final int height) {
		super(player);
		this.setFields();

		this.height = height;
		this.origin = origin;
		this.location = origin.clone();
		this.block = this.location.getBlock();
		this.distance = this.getEarthbendableBlocksLength(this.block, this.direction.clone().multiply(-1), height);

		this.loadAffectedBlocks();

		if (this.distance != 0 && this.canInstantiate()) {
			this.time = System.currentTimeMillis() - this.interval;
			this.start();
		}
	}

	private void setFields() {
		this.speed = getConfig().getDouble("Abilities.Earth.RaiseEarth.Speed");
		this.height = getConfig().getInt("Abilities.Earth.RaiseEarth.Column.Height");
		this.selectRange = getConfig().getDouble("Abilities.Earth.RaiseEarth.Column.SelectRange");
		this.cooldown = getConfig().getLong("Abilities.Earth.RaiseEarth.Column.Cooldown");
		this.direction = new Vector(0, 1, 0);
		this.interval = (long) (1000.0 / this.speed);
		this.affectedBlocks = new ConcurrentHashMap<>();
	}

	private boolean canInstantiate() {
		for (final Block block : this.affectedBlocks.keySet()) {
			if (!this.isEarthbendable(block) || (TempBlock.isTempBlock(block) && !EarthAbility.isBendableEarthTempBlock(block))) {
				return false;
			}
		}
		return true;
	}

	private void loadAffectedBlocks() {
		this.affectedBlocks.clear();
		Block thisBlock;
		for (int i = 0; i <= this.distance; i++) {
			thisBlock = this.block.getWorld().getBlockAt(this.location.clone().add(this.direction.clone().multiply(-i)));
			this.affectedBlocks.put(thisBlock, thisBlock);
			if (Collapse.blockInAllAffectedBlocks(thisBlock)) {
				Collapse.revert(thisBlock);
			}
		}
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - this.time >= this.interval) {
			this.time = System.currentTimeMillis();
			final Block block = this.location.getBlock();
			this.location = this.location.add(this.direction);
			if (!block.isLiquid()) {
				this.moveEarth(block, this.direction, this.distance);
			}

			this.loadAffectedBlocks();

			if (this.location.distanceSquared(this.origin) >= this.distance * this.distance) {
				this.remove();
				return;
			}
		}
	}

	public static boolean blockInAllAffectedBlocks(final Block block) {
		for (RaiseEarth raiseEarth : getAbilities(RaiseEarth.class)) {
			if (raiseEarth.affectedBlocks.contains(block)) {
				return true;
			}
		}
		return false;
	}

	public static void revertAffectedBlock(final Block block) {
		for (final RaiseEarth raiseEarth : getAbilities(RaiseEarth.class)) {
			raiseEarth.affectedBlocks.remove(block);
		}
	}

	@Override
	public String getName() {
		return "RaiseEarth";
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

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public Block getBlock() {
		return this.block;
	}

	public void setBlock(final Block block) {
		this.block = block;
	}

	public Vector getDirection() {
		return this.direction;
	}

	public void setDirection(final Vector direction) {
		this.direction = direction;
	}

	public Location getOrigin() {
		return this.origin;
	}

	public void setOrigin(final Location origin) {
		this.origin = origin;
	}

	public ConcurrentHashMap<Block, Block> getAffectedBlocks() {
		return this.affectedBlocks;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(final Location location) {
		this.location = location;
	}

	public double getSelectRange() {
		return this.selectRange;
	}

	public void setSelectRange(final double selectRange) {
		this.selectRange = selectRange;
	}

}
