package com.projectkorra.projectkorra.airbending;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.Ability;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.attribute.Attribute;

public class AirScooter extends AirAbility {

	@Attribute(Attribute.SPEED)
	private double speed;
	private double interval;
	@Attribute(Attribute.RADIUS)
	private double radius;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private long duration;
	@Attribute(Attribute.HEIGHT)
	private double maxHeightFromGround;
	private Block floorblock;
	private Random random;
	private ArrayList<Double> angles;
	private Slime slime;
	private Boolean useslime;

	private double phi = 0;

	public AirScooter(final Player player) {
		super(player);

		if (check(player)) {
			return;
		} else if (!player.isSprinting() || GeneralMethods.isSolid(player.getEyeLocation().getBlock()) || ElementalAbility.isWater(player.getEyeLocation().getBlock())) {
			return;
		} else if (GeneralMethods.isSolid(player.getLocation().add(0, -.5, 0).getBlock())) {
			return;
		} else if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.speed = getConfig().getDouble("Abilities.Air.AirScooter.Speed");
		this.interval = getConfig().getDouble("Abilities.Air.AirScooter.Interval");
		this.radius = getConfig().getDouble("Abilities.Air.AirScooter.Radius");
		this.cooldown = getConfig().getLong("Abilities.Air.AirScooter.Cooldown");
		this.duration = getConfig().getLong("Abilities.Air.AirScooter.Duration");
		this.maxHeightFromGround = getConfig().getDouble("Abilities.Air.AirScooter.MaxHeightFromGround");
		this.useslime = getConfig().getBoolean("Abilities.Air.AirScooter.ShowSitting");
		this.random = new Random();
		this.angles = new ArrayList<>();

		this.flightHandler.createInstance(player, this.getName());
		player.setAllowFlight(true);
		player.setFlying(true);

		player.setSprinting(false);
		player.setSneaking(false);

		for (int i = 0; i < 5; i++) {
			this.angles.add((double) (60 * i));
		}
		if (player.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
			this.useslime = false;
		}
		if (this.useslime) {
			this.slime = (Slime) player.getWorld().spawnEntity(player.getLocation(), EntityType.SLIME);
			if (this.slime != null) {
				this.slime.setSize(1);
				this.slime.setSilent(true);
				this.slime.setInvulnerable(true);
				this.slime.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, true, false));
				this.slime.addPassenger(player);
			} else {
				this.useslime = false;
			}
		}

		this.start();
	}

	/**
	 * Checks if player has an instance already and removes if they do.
	 *
	 * @param player The player to check
	 * @return false If player doesn't have an instance
	 */
	public static boolean check(final Player player) {
		if (hasAbility(player, AirScooter.class)) {
			getAbility(player, AirScooter.class).remove();
			return true;
		}
		return false;
	}

	/*
	 * Looks for a block under the player and sets "floorBlock" to a block under
	 * the player if it within the maximum height
	 */
	private void getFloor() {
		this.floorblock = null;
		for (int i = 0; i <= this.maxHeightFromGround; i++) {
			final Block block = this.player.getEyeLocation().getBlock().getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || ElementalAbility.isWater(block)) {
				this.floorblock = block;
				return;
			}
		}
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			this.remove();
			return;
		} else if (this.duration != 0 && System.currentTimeMillis() > this.getStartTime() + this.duration) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		this.getFloor();
		if (this.floorblock == null) {
			this.remove();
			return;
		}

		if (this.player.isSneaking()) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		if (this.useslime && (this.slime == null || !this.slime.getPassengers().contains(this.player))) {
			this.bPlayer.addCooldown(this);
			this.remove();
			return;
		}

		Vector velocity = this.player.getEyeLocation().getDirection().clone().normalize();
		velocity = velocity.clone().normalize().multiply(this.speed);
		/*
		 * checks the players speed and ends the move if they are going too slow
		 */
		if (System.currentTimeMillis() > this.getStartTime() + this.interval) {
			if (this.useslime) {
				if (this.slime.getVelocity().length() < this.speed * 0.3) {
					this.remove();
					return;
				}
			} else {
				if (this.player.getVelocity().length() < this.speed * 0.3) {
					this.remove();
					return;
				}
			}
			this.spinScooter();
		}
		/*
		 * Checks for how far the ground is away from the player it elevates or
		 * lowers the player based on their distance from the ground.
		 */
		final double distance = this.player.getLocation().getY() - this.floorblock.getY();
		Math.abs(distance - 2.4);
		if (distance > 2.75) {
			velocity.setY(-.25);
		} else if (distance < 2) {
			velocity.setY(.25);
		} else {
			velocity.setY(0);
		}

		final Vector v = velocity.clone().setY(0);
		final Block b = this.floorblock.getLocation().clone().add(v.multiply(1.2)).getBlock();
		if (!GeneralMethods.isSolid(b) && !ElementalAbility.isWater(b)) {
			velocity.add(new Vector(0, -0.1, 0));
		} else if (GeneralMethods.isSolid(b.getRelative(BlockFace.UP)) || ElementalAbility.isWater(b.getRelative(BlockFace.UP))) {
			velocity.add(new Vector(0, 0.7, 0));
		}

		final Location loc = this.player.getLocation();
		if (!ElementalAbility.isWater(this.player.getLocation().add(0, 2, 0).getBlock())) {
			loc.setY(this.floorblock.getY() + 1.5);
		} else {
			return;
		}

		this.player.setSprinting(false);
		this.player.removePotionEffect(PotionEffectType.SPEED);
		if (this.useslime) {
			GeneralMethods.setVelocity((Ability)this,this.slime, velocity);
		} else {
			GeneralMethods.setVelocity((Ability)this,this.player, velocity);
		}

		if (this.random.nextInt(4) == 0) {
			playAirbendingSound(this.player.getLocation());
		}
	}

	/*
	 * Updates the players flight, also adds the cooldown.
	 */
	@Override
	public void remove() {
		super.remove();
		if (this.slime != null) {
			this.slime.remove();
		}
		this.flightHandler.removeInstance(this.player, this.getName());
		this.bPlayer.addCooldown(this);
	}

	/*
	 * The particles used for AirScooter phi = how many rings of particles the
	 * sphere has. theta = how dense the rings are. r = Radius of the sphere
	 */
	private void spinScooter() {
		final Location origin = this.player.getLocation();
		final Location origin2 = this.player.getLocation();
		this.phi += Math.PI / 10 * 4;
		for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 10) {
			final double r = 0.6;
			final double x = r * Math.cos(theta) * Math.sin(this.phi);
			final double y = r * Math.cos(this.phi);
			final double z = r * Math.sin(theta) * Math.sin(this.phi);
			origin.add(x, y, z);
			playAirbendingParticles(origin, 1, 0F, 0F, 0F);
			origin.subtract(x, y, z);
		}
		for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 10) {
			final double r = 0.6;
			final double x = r * Math.cos(theta) * Math.sin(this.phi);
			final double y = r * Math.cos(this.phi);
			final double z = r * Math.sin(theta) * Math.sin(this.phi);
			origin2.subtract(x, y, z);
			playAirbendingParticles(origin2, 1, 0F, 0F, 0F);
			origin2.add(x, y, z);
		}
	}

	@Override
	public String getName() {
		return "AirScooter";
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
	public double getCollisionRadius() {
		return this.getRadius();
	}

	public double getSpeed() {
		return this.speed;
	}

	public void setSpeed(final double speed) {
		this.speed = speed;
	}

	public double getInterval() {
		return this.interval;
	}

	public void setInterval(final double interval) {
		this.interval = interval;
	}

	public double getRadius() {
		return this.radius;
	}

	public void setRadius(final double radius) {
		this.radius = radius;
	}

	public double getMaxHeightFromGround() {
		return this.maxHeightFromGround;
	}

	public void setMaxHeightFromGround(final double maxHeightFromGround) {
		this.maxHeightFromGround = maxHeightFromGround;
	}

	public Block getFloorblock() {
		return this.floorblock;
	}

	public void setFloorblock(final Block floorblock) {
		this.floorblock = floorblock;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}
}
