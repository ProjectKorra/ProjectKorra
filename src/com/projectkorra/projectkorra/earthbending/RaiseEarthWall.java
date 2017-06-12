package com.projectkorra.projectkorra.earthbending;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

public class RaiseEarthWall extends EarthAbility {

	private int selectRange;
	private int height;
	private int width;
	private long cooldown;
	private Location location;

	public RaiseEarthWall(Player player) {
		super(player);
		this.selectRange = getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.SelectRange");
		this.height = getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Height");
		this.width = getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Width");
		this.cooldown = getConfig().getLong("Abilities.Earth.RaiseEarth.Wall.Cooldown");

		if (!bPlayer.canBend(this) || bPlayer.isOnCooldown("RaiseEarthWall")) {
			return;
		}

		if (bPlayer.isAvatarState()) {
			height = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.RaiseEarth.Wall.Height");
			width = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.RaiseEarth.Wall.Width");
		}

		Vector direction = player.getEyeLocation().getDirection().normalize();
		double ox, oy, oz;
		direction.setY(0);
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();
		orth = getDegreeRoundedVector(orth, 0.25);

		Block sblock = BlockSource.getEarthSourceBlock(player, selectRange, ClickType.SHIFT_DOWN);

		if (sblock == null) {
			location = getTargetEarthBlock(selectRange).getLocation();
		} else {
			location = sblock.getLocation();
		}

		World world = location.getWorld();
		boolean shouldAddCooldown = false;

		for (int i = 0; i < width; i++) {
			double adjustedI = i - width / 2.0;
			Block block = world.getBlockAt(location.clone().add(orth.clone().multiply(adjustedI)));

			if (isTransparent(block)) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.DOWN);
					if (isEarthbendable(block)) {
						shouldAddCooldown = true;
						new RaiseEarth(player, block.getLocation(), height);
					} else if (!isTransparent(block)) {
						break;
					}
				}
			} else if (isEarthbendable(block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.UP);
					if (isTransparent(block)) {
						shouldAddCooldown = true;
						new RaiseEarth(player, block.getRelative(BlockFace.DOWN).getLocation(), height);
					} else if (!isEarthbendable(block)) {
						break;
					}
				}
			} else if (isEarthbendable(block)) {
				shouldAddCooldown = true;
				new RaiseEarth(player, block.getLocation(), height);
			}
		}

		if (shouldAddCooldown) {
			bPlayer.addCooldown("RaiseEarthWall", cooldown);
		}
	}

	private static Vector getDegreeRoundedVector(Vector vec, double degreeIncrement) {
		if (vec == null) {
			return null;
		}
		vec = vec.normalize();
		double[] dims = { vec.getX(), vec.getY(), vec.getZ() };

		for (int i = 0; i < dims.length; i++) {
			double dim = dims[i];
			int sign = dim >= 0 ? 1 : -1;
			int dimDivIncr = (int) (dim / degreeIncrement);

			double lowerBound = dimDivIncr * degreeIncrement;
			double upperBound = (dimDivIncr + (1 * sign)) * degreeIncrement;

			if (Math.abs(dim - lowerBound) < Math.abs(dim - upperBound)) {
				dims[i] = lowerBound;
			} else {
				dims[i] = upperBound;
			}
		}
		return new Vector(dims[0], dims[1], dims[2]);
	}

	@Override
	public String getName() {
		return "RaiseEarth";
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

	public int getRange() {
		return selectRange;
	}

	public void setRange(int range) {
		this.selectRange = range;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(int selectRange) {
		this.selectRange = selectRange;
	}

}
