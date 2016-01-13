package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RaiseEarthWall extends EarthAbility {

	private int range;
	private int height;
	private int width;
	private long cooldown;
	private Location location;
	
	public RaiseEarthWall(Player player) {
		super(player);
		this.range = getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Range");
		this.height = getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Height");
		this.width = getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Width");
		this.cooldown = GeneralMethods.getGlobalCooldown();

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (bPlayer.isAvatarState()) {
			height = (int) (2.0 / 5.0 * (double) AvatarState.getValue(height));
			width = AvatarState.getValue(width);
		}

		Vector direction = player.getEyeLocation().getDirection().normalize();
		double ox, oy, oz;
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();

		Block sblock = BlockSource.getEarthSourceBlock(player, range, ClickType.SHIFT_DOWN);
		
		if (sblock == null) {
			location = getTargetEarthBlock(range).getLocation();
		} else {
			location = sblock.getLocation();
		}
		
		World world = location.getWorld();
		boolean cooldown = false;

		for (int i = -width / 2; i <= width / 2; i++) {
			Block block = world.getBlockAt(location.clone().add(orth.clone().multiply((double) i)));

			if (isTransparent(block)) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.DOWN);
					if (isEarthbendable(block)) {
						cooldown = true;
						new RaiseEarth(player, block.getLocation(), height);
					} else if (!isTransparent(block)) {
						break;
					}
				}
			} else if (isEarthbendable(block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.UP);
					if (isTransparent(block)) {
						cooldown = true;
						new RaiseEarth(player, block.getRelative(BlockFace.DOWN).getLocation(), height);
					} else if (!isEarthbendable(block)) {
						break;
					}
				}
			} else if (isEarthbendable(block)) {
				cooldown = true;
				new RaiseEarth(player, block.getLocation(), height);
			}
		}

		if (cooldown) {
			bPlayer.addCooldown(this);
		}
	}

	@Override
	public String getName() {
		return "RaiseEarth";
	}

	@Override
	public void progress() {}

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
		return range;
	}

	public void setRange(int range) {
		this.range = range;
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

}
