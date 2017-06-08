package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.Flight;

public class AirSpout extends AirAbility {

	private static final Integer[] DIRECTIONS = { 0, 1, 2, 3, 5, 6, 7, 8 };

	private int angle;
	private long animTime;
	private long interval;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.HEIGHT)
	private double height;

	public AirSpout(Player player) {
		super(player);

		AirSpout spout = getAbility(player, AirSpout.class);
		if (spout != null) {
			spout.remove();
			return;
		}

		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}

		this.angle = 0;
		this.cooldown = 0;
		this.animTime = System.currentTimeMillis();
		this.interval = getConfig().getLong("Abilities.Air.AirSpout.Interval");
		this.height = getConfig().getDouble("Abilities.Air.AirSpout.Height");

		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(heightRemoveThreshold)) {
			return;
		}

		new Flight(player);

		if (bPlayer.isAvatarState()) {
			this.height = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSpout.Height");
		}

		start();
		bPlayer.addCooldown(this);
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeSpouts(Location loc0, double radius, Player sourceplayer) {
		boolean removed = false;
		for (AirSpout spout : getAbilities(AirSpout.class)) {
			if (!spout.player.equals(sourceplayer)) {
				Location loc1 = spout.player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				double dx = loc1.getX() - loc0.getX();
				double dy = loc1.getY() - loc0.getY();
				double dz = loc1.getZ() - loc0.getZ();

				double distance = Math.sqrt(dx * dx + dz * dz);

				if (distance <= radius && dy > 0 && dy < spout.height) {
					spout.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	private void allowFlight() {
		player.setAllowFlight(true);
		player.setFlying(true);
	}

	private boolean isWithinMaxSpoutHeight(double threshold) {
		Block ground = getGround();
		if (ground == null) {
			return false;
		}
		double playerHeight = player.getLocation().getY();
		if (playerHeight > ground.getLocation().getY() + height + threshold) {
			return false;
		}
		return true;
	}

	private Block getGround() {
		Block standingblock = player.getLocation().getBlock();
		for (int i = 0; i <= height + 5; i++) {
			Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				return block;
			}
		}
		return null;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || !bPlayer.canBendIgnoreBindsCooldowns(this) || !bPlayer.canBind(this)) {
			remove();
			return;
		}

		double heightRemoveThreshold = 2;
		if (!isWithinMaxSpoutHeight(heightRemoveThreshold)) {
			remove();
			return;
		}

		if (!bPlayer.canBind(this)) {
			remove();
			return;
		}

		Block eyeBlock = player.getEyeLocation().getBlock();
		if (eyeBlock.isLiquid() || GeneralMethods.isSolid(eyeBlock)) {
			remove();
			return;
		}

		player.setFallDistance(0);
		player.setSprinting(false);
		if ((new Random()).nextInt(4) == 0) {
			playAirbendingSound(player.getLocation());
		}

		Block block = getGround();
		if (block != null) {
			double dy = player.getLocation().getY() - block.getY();
			if (dy > height) {
				removeFlight();
			} else {
				allowFlight();
			}
			rotateAirColumn(block);
		} else {
			remove();
		}
	}

	public void remove() {
		super.remove();
		removeFlight();
	}

	private void removeFlight() {
		player.setAllowFlight(false);
		player.setFlying(false);
	}

	private void rotateAirColumn(Block block) {
		if (!player.getWorld().equals(block.getWorld())) {
			return;
		}
		if (System.currentTimeMillis() >= animTime + interval) {
			animTime = System.currentTimeMillis();
			Location location = block.getLocation();
			Location playerloc = player.getLocation();
			location = new Location(location.getWorld(), playerloc.getX(), location.getY(), playerloc.getZ());

			int index = angle;
			double dy = Math.min(playerloc.getY() - block.getY(), height);
			angle = angle >= DIRECTIONS.length ? 0 : angle + 1;

			for (int i = 1; i <= dy; i++) {
				index = index >= DIRECTIONS.length ? 0 : index + 1;
				Location effectloc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());
				playAirbendingParticles(effectloc2, 3, 0.4F, 0.4F, 0.4F);
			}
		}
	}

	@Override
	public String getName() {
		return "AirSpout";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isCollidable() {
		return true;
	}

	@Override
	public List<Location> getLocations() {
		ArrayList<Location> locations = new ArrayList<>();
		Location topLoc = player.getLocation().getBlock().getLocation();
		double ySpacing = 3;
		for (double i = 0; i <= height; i += ySpacing) {
			locations.add(topLoc.clone().add(0, -i, 0));
		}
		return locations;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public long getAnimTime() {
		return animTime;
	}

	public void setAnimTime(long animTime) {
		this.animTime = animTime;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
