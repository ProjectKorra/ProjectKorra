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
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.attribute.Attribute;

public class AirSpout extends AirAbility {

	private static final Integer[] DIRECTIONS = { 0, 1, 2, 3, 5, 6, 7, 8 };

	private int angle;
	private long animTime;
	private long interval;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.HEIGHT)
	private double height;

	public AirSpout(final Player player) {
		super(player);

		final AirSpout spout = getAbility(player, AirSpout.class);
		if (spout != null) {
			spout.remove();
			return;
		}

		if (!this.bPlayer.canBend(this)) {
			return;
		}

		this.angle = 0;
		this.cooldown = getConfig().getLong("Abilities.Air.AirSpout.Cooldown");
		this.duration = getConfig().getLong("Abilities.Air.AirSpout.Duration");
		this.animTime = System.currentTimeMillis();
		this.interval = getConfig().getLong("Abilities.Air.AirSpout.Interval");
		this.height = getConfig().getDouble("Abilities.Air.AirSpout.Height");

		final double heightRemoveThreshold = 2;
		if (!this.isWithinMaxSpoutHeight(heightRemoveThreshold)) {
			return;
		}

		this.flightHandler.createInstance(player, this.getName());

		if (this.bPlayer.isAvatarState()) {
			this.height = getConfig().getDouble("Abilities.Avatar.AvatarState.Air.AirSpout.Height");
		}

		this.start();
	}

	/**
	 * This method was used for the old collision detection system. Please see
	 * {@link Collision} for the new system.
	 */
	@Deprecated
	public static boolean removeSpouts(Location loc0, final double radius, final Player sourceplayer) {
		boolean removed = false;
		for (final AirSpout spout : getAbilities(AirSpout.class)) {
			if (!spout.player.equals(sourceplayer)) {
				final Location loc1 = spout.player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				final double dx = loc1.getX() - loc0.getX();
				final double dy = loc1.getY() - loc0.getY();
				final double dz = loc1.getZ() - loc0.getZ();

				final double distance = Math.sqrt(dx * dx + dz * dz);

				if (distance <= radius && dy > 0 && dy < spout.height) {
					spout.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	private void allowFlight() {
		if (!this.player.getAllowFlight()) {
			this.player.setAllowFlight(true);
		}
		if (!this.player.isFlying()) {
			this.player.setFlying(true);
		}
	}

	private void removeFlight() {
		if (this.player.isFlying()) {
			this.player.setFlying(false);
		}
		if (this.player.getAllowFlight()) {
			this.player.setAllowFlight(false);
		}
	}

	private boolean isWithinMaxSpoutHeight(final double threshold) {
		final Block ground = this.getGround();
		if (ground == null) {
			return false;
		}
		final double playerHeight = this.player.getLocation().getY();
		if (playerHeight > ground.getLocation().getY() + this.height + threshold) {
			return false;
		}
		return true;
	}

	private Block getGround() {
		final Block standingblock = this.player.getLocation().getBlock();
		for (int i = 0; i <= this.height + 5; i++) {
			final Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || ElementalAbility.isWater(block)) {
				return block;
			}
		}
		return null;
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline() || !this.bPlayer.canBendIgnoreBinds(this) || !this.bPlayer.canBind(this)) {
			this.remove();
			return;
		} else if (this.duration != 0 && System.currentTimeMillis() > this.getStartTime() + this.duration) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		final double heightRemoveThreshold = 2;
		if (!this.isWithinMaxSpoutHeight(heightRemoveThreshold)) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		final Block eyeBlock = this.player.getEyeLocation().getBlock();
		if (ElementalAbility.isWater(eyeBlock) || GeneralMethods.isSolid(eyeBlock)) {
			this.remove();
			return;
		}

		this.player.setFallDistance(0);
		this.player.setSprinting(false);
		if ((new Random()).nextInt(4) == 0) {
			playAirbendingSound(this.player.getLocation());
		}

		final Block block = this.getGround();
		if (block != null) {
			final double dy = this.player.getLocation().getY() - block.getY();
			if (dy > this.height) {
				this.removeFlight();
			} else {
				this.allowFlight();
			}
			this.rotateAirColumn(block);
		} else {
			this.remove();
		}
	}

	@Override
	public void remove() {
		super.remove();
		this.flightHandler.removeInstance(this.player, this.getName());
	}

	private void rotateAirColumn(final Block block) {
		if (!this.player.getWorld().equals(block.getWorld())) {
			return;
		}
		if (System.currentTimeMillis() >= this.animTime + this.interval) {
			this.animTime = System.currentTimeMillis();
			Location location = block.getLocation();
			final Location playerloc = this.player.getLocation();
			location = new Location(location.getWorld(), playerloc.getX(), location.getY(), playerloc.getZ());

			int index = this.angle;
			final double dy = Math.min(playerloc.getY() - block.getY(), this.height);
			this.angle = this.angle >= DIRECTIONS.length ? 0 : this.angle + 1;

			for (int i = 1; i <= dy; i++) {
				index = index >= DIRECTIONS.length ? 0 : index + 1;
				final Location effectloc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());
				playAirbendingParticles(this, effectloc2, 3, 0.4F, 0.4F, 0.4F);
			}
		}
	}

	@Override
	public String getName() {
		return "AirSpout";
	}

	@Override
	public Location getLocation() {
		return this.player != null ? this.player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
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
		final ArrayList<Location> locations = new ArrayList<>();
		final Location topLoc = this.player.getLocation().getBlock().getLocation();
		final double ySpacing = 3;
		for (double i = 0; i <= this.height; i += ySpacing) {
			locations.add(topLoc.clone().add(0, -i, 0));
		}
		return locations;
	}

	public int getAngle() {
		return this.angle;
	}

	public void setAngle(final int angle) {
		this.angle = angle;
	}

	public long getAnimTime() {
		return this.animTime;
	}

	public void setAnimTime(final long animTime) {
		this.animTime = animTime;
	}

	public long getInterval() {
		return this.interval;
	}

	public void setInterval(final long interval) {
		this.interval = interval;
	}

	public double getHeight() {
		return this.height;
	}

	public void setHeight(final double height) {
		this.height = height;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

}
