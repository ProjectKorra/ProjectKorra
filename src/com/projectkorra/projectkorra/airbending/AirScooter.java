package com.projectkorra.projectkorra.airbending;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.Attribute;
import com.projectkorra.projectkorra.util.Attribute.Attributable;

import com.projectkorra.projectkorra.util.Flight;

public class AirScooter extends AirAbility implements Attributable{

	private double speed;
	private double interval;
	private double radius;
	private long cooldown;
	private double maxHeightFromGround;
	private static Attribute<Double> speedA;
	private static Attribute<Double> intervalA;
	private static Attribute<Double> radiusA;
	private static Attribute<Long> cooldownA;
	private static Attribute<Double> heightA;
	private Block floorblock;
	private Random random;
	private double phi = 0;

	private boolean canFly;
	private boolean hadFly;

	public AirScooter(Player player) {
		super(player);

		if (check(player))
			return;
		else if (!player.isSprinting() || GeneralMethods.isSolid(player.getEyeLocation().getBlock()) || player.getEyeLocation().getBlock().isLiquid())
			return;
		else if (GeneralMethods.isSolid(player.getLocation().add(0, -.5, 0).getBlock()))
			return;
		else if (bPlayer.isOnCooldown(this))
			return;

		this.speed = speedA.getModified(bPlayer);
		this.interval = intervalA.getModified(bPlayer);
		this.radius = radiusA.getModified(bPlayer);
		this.cooldown = cooldownA.getModified(bPlayer);
		this.maxHeightFromGround = heightA.getModified(bPlayer);
		this.random = new Random();
		canFly = player.getAllowFlight();
		hadFly = player.isFlying();

		new Flight(player);
		player.setAllowFlight(true);
		player.setFlying(true);

		player.setSprinting(false);
		player.setSneaking(false);
		
		start();
	}

	/**
	 * Checks if player has an instance already and removes if they do.
	 * 
	 * @param player The player to check
	 * @return false If player doesn't have an instance
	 */
	public static boolean check(Player player) {
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
		floorblock = null;
		for (int i = 0; i <= maxHeightFromGround; i++) {
			Block block = player.getEyeLocation().getBlock().getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				floorblock = block;
				return;
			}
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		getFloor();
		if (floorblock == null) {
			remove();
			return;
		}

		Vector velocity = player.getEyeLocation().getDirection().clone().normalize();
		velocity = velocity.multiply(speed);
		/*
		 * checks the players speed and ends the move if they are going too slow
		 */
		if (System.currentTimeMillis() > getStartTime() + interval) {
			if (player.getVelocity().length() < speed * 0.3) {
				remove();
				return;
			}
			spinScooter();
		}
		/*
		 * Checks for how far the ground is away from the player it elevates or
		 * lowers the player based on their distance from the ground.
		 */
		double distance = player.getLocation().getY() - (double) floorblock.getY();
		if (distance > 2.3) {
			velocity.setY(-0.15);
		} else if (distance < 1.95) {
			velocity.setY(0.15);
		} else {
			velocity.setY(0);
		}
		
		Vector v = velocity.clone().setY(0);
		Block b = floorblock.getLocation().clone().add(v.multiply(1.2)).getBlock();
		if (!GeneralMethods.isSolid(b)) {
			velocity.add(new Vector(0, -0.5, 0));
		} else if (b.getRelative(BlockFace.UP).getType() != Material.AIR) {
			velocity.add(new Vector(0, 0.5, 0));
		}

		Location loc = player.getLocation();
		if (!WaterAbility.isWater(loc.clone().add(0, 2, 0).getBlock())) {
			loc.setY((double) floorblock.getY() + 1.5);
		} else {
			return;
		}

		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.setVelocity(velocity);
		if (random.nextInt(4) == 0) {
			playAirbendingSound(player.getLocation());
		}
	}

	/*
	 * Updates the players flight, also adds the cooldown.
	 */
	@Override
	public void remove() {
		super.remove();
		player.setAllowFlight(canFly);
		player.setFlying(hadFly);
		bPlayer.addCooldown(this);
	}
	
	private void spinScooter() {
		Location origin = player.getLocation().clone();
		Location origin2 = player.getLocation().clone();
		phi += Math.PI / 10 * 4;
		for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 10) {
			double r = 0.6;
			double x = r * Math.cos(theta) * Math.sin(phi);
			double y = r * Math.cos(phi);
			double z = r * Math.sin(theta) * Math.sin(phi);
			origin.add(x, y, z);
			playAirbendingParticles(origin, 1, 0F, 0F, 0F);
			origin.subtract(x, y, z);
		}
		for (double theta = 0; theta <= 2 * Math.PI; theta += Math.PI / 10) {
			double r = 0.6;
			double x = r * Math.cos(theta) * Math.sin(phi);
			double y = r * Math.cos(phi);
			double z = r * Math.sin(theta) * Math.sin(phi);
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
	public double getCollisionRadius() {
		return getRadius();
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getInterval() {
		return interval;
	}

	public void setInterval(double interval) {
		this.interval = interval;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getMaxHeightFromGround() {
		return maxHeightFromGround;
	}

	public void setMaxHeightFromGround(double maxHeightFromGround) {
		this.maxHeightFromGround = maxHeightFromGround;
	}

	public Block getFloorblock() {
		return floorblock;
	}

	public void setFloorblock(Block floorblock) {
		this.floorblock = floorblock;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public void registerAttributes() {
		speedA = new Attribute<Double>(this, "speed", getConfig().getDouble("Abilities.Air.AirScooter.Speed"));
		intervalA = new Attribute<Double>(this, "interval", getConfig().getDouble("Abilities.Air.AirScooter.Interval"));
		radiusA = new Attribute<Double>(this, "radius", getConfig().getDouble("Abilities.Air.AirScooter.Radius"));
		heightA = new Attribute<Double>(this, "height", getConfig().getDouble("Abilities.Air.AirScooter.MaxHeightFromGround"));
		cooldownA = new Attribute<Long>(this, "cooldown", getConfig().getLong("Abilities.Air.AirScooter.Cooldown"));
	}
}
