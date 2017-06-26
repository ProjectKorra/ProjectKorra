package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class EarthGrab extends EarthAbility {

	private long cooldown;
	private double lowestDistance;
	private double selectRange;
	private int height;
	private int radius;
	private Location origin;
	private Vector direction;
	private Entity closestEntity;
	private Location startLoc;
	private Location loc;
	private Vector dir;
	private Block groundBlock;
	private Material blockType;
	private Byte blockByte;
	private Random random;

	public EarthGrab(Player player) {
		super(player);

		this.selectRange = getConfig().getDouble("Abilities.Earth.EarthGrab.SelectRange");
		this.height = getConfig().getInt("Abilities.Earth.EarthGrab.Height");
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthGrab.Cooldown");
		this.radius = getConfig().getInt("Abilities.Earth.EarthGrab.Radius");
		this.origin = player.getEyeLocation();
		this.direction = origin.getDirection();
		this.lowestDistance = selectRange + 1;
		this.closestEntity = null;
		this.startLoc = player.getLocation();
		this.loc = player.getLocation();
		this.dir = player.getLocation().getDirection().clone().normalize().multiply(1.5);
		this.random = new Random();

		if (!bPlayer.canBend(this)) {
			return;
		} else if (player.getLocation().clone().add(0, -1, 0).getBlock().getType().isTransparent()) {
			return;
		}

		if (bPlayer.isAvatarState()) {
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.EarthGrab.Cooldown");
			this.height = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.EarthGrab.Height");
			this.radius = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.EarthGrab.Radius");
		}

		if (player.isSneaking()) {
			start();
		} else {
			Location targetLocation = GeneralMethods.getTargetedLocation(player, 1);
			Block block = GeneralMethods.getTopBlock(targetLocation, 1, 1);
			if (isEarthbendable(block) && block.getWorld().equals(player.getWorld()) && block.getLocation().distance(player.getLocation()) <= 1.6) {
				earthGrabSelf();
				remove();
			}
		}
	}

	public void formDome() {
		if (closestEntity != null) {
			ArrayList<Block> blocks = new ArrayList<Block>();
			Location location = closestEntity.getLocation().clone().subtract(0, 1, 0);
			location.setPitch(0);
			for (float theta = -180; theta < 180; theta += 1) {
				Location loc = location.clone();
				loc.setYaw(theta);
				Vector dir = loc.getDirection();
				Block a = GeneralMethods.getTopBlock(loc.add(dir.clone().multiply(radius)), height/2);
				while (!GeneralMethods.isSolid(a) && !isEarthbendable(a.getType(), true, true, false)) {
					a = a.getRelative(BlockFace.DOWN);
				}
				if (!blocks.contains(a)) {
					blocks.add(a);
					new RaiseEarth(player, a.getLocation(), (int) height);
				}
				
				Block b = GeneralMethods.getTopBlock(loc.add(dir.clone().multiply(1)), height/2);
				while (!GeneralMethods.isSolid(b) && !isEarthbendable(b.getType(), true, true, false)) {
					b = b.getRelative(BlockFace.DOWN);
				}
				if (!blocks.contains(b)) {
					blocks.add(b);
					new RaiseEarth(player, b.getLocation(), (int) height/2);
				}
			}

			bPlayer.addCooldown(this);
		}
	}

	public void earthGrabSelf() {
		closestEntity = player;
		getGround();
		ParticleEffect.BLOCK_CRACK.display((ParticleEffect.ParticleData) new ParticleEffect.BlockData(blockType, blockByte), 1F, 1F, 1F, 0.1F, 100, player.getLocation(), 500);
		formDome();
	}

	@Override
	public String getName() {
		return "EarthGrab";
	}

	@SuppressWarnings("deprecation")
	private Block getGround() {
		Block b = GeneralMethods.getTopBlock(loc, 3);
		if (isEarthbendable(b)) {
			blockType = b.getType();
			blockByte = b.getData();
			return b;
		} else {
			while (!isEarthbendable(b)) {
				b = b.getRelative(BlockFace.DOWN);
				if (player.getLocation().getBlockY() - b.getY() > 5) {
					break;
				}
			}
			if (isEarthbendable(b)) {
				blockType = b.getType();
				blockByte = b.getData();
				return b;
			}
		}
		return null;
	}

	@Override
	public void progress() {
		groundBlock = getGround();
		if (groundBlock == null) {
			remove();
			return;
		}
		bPlayer.addCooldown(this);
		dir = dir.clone().normalize().multiply(1.5);
		dir.setY(0);
		double distance = loc.getY() - (double) groundBlock.getY();
		double dx = Math.abs(distance - 2.4);
		if (distance > 1.75) {
			dir.setY(-.50 * dx * dx);
		} else if (distance < 1) {
			dir.setY(.50 * dx * dx);
		} else {
			dir.setY(0);
		}
		loc.add(dir);
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!player.isSneaking()) {
			remove();
			return;
		}
		if (loc.getWorld().equals(startLoc.getWorld()) && loc.distance(startLoc) >= selectRange) {
			remove();
			return;
		}
		if (blockType == null) {
			return;
		} else if (blockByte == null) {
			return;
		}
		ParticleEffect.BLOCK_CRACK.display((ParticleEffect.ParticleData) new ParticleEffect.BlockData(blockType, blockByte), 1F, 0.1F, 1F, 0.1F, 100, loc.add(0, -1, 0), 500);

		for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 2.5)) {
			if (e.getEntityId() != player.getEntityId() && e instanceof LivingEntity) {
				closestEntity = e;
				formDome();
				remove();
				return;
			}
		}
		if (random.nextInt(2) == 0) {
			playEarthbendingSound(loc);
		}

	}

	@Override
	public Location getLocation() {
		return origin;
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

	public double getLowestDistance() {
		return lowestDistance;
	}

	public void setLowestDistance(double lowestDistance) {
		this.lowestDistance = lowestDistance;
	}

	public double getRange() {
		return selectRange;
	}

	public void setRange(double range) {
		this.selectRange = range;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
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

	public Entity getClosestEntity() {
		return closestEntity;
	}

	public void setClosestEntity(Entity closestEntity) {
		this.closestEntity = closestEntity;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
