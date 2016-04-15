package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

public class AirScooter extends AirAbility {

	private double speed;
	private double interval;
	private double radius;
	private long cooldown;
	private double maxHeightFromGround;
	private Block floorblock;
	private Random random;
	private ArrayList<Double> angles;
	
	private boolean canFly;
	private boolean hadFly;

	public AirScooter(Player player) {
		super(player);
		
		if (check(player)) 
			return;
		else if (!player.isSprinting() || GeneralMethods.isSolid(player.getEyeLocation().getBlock())
				|| player.getEyeLocation().getBlock().isLiquid()) 
			return;
		else if (GeneralMethods.isSolid(player.getLocation().add(0, -.5, 0).getBlock()))
			return;
		else if (bPlayer.isOnCooldown(this))
			return;

		this.speed = getConfig().getDouble("Abilities.Air.AirScooter.Speed");
		this.interval = getConfig().getDouble("Abilities.Air.AirScooter.Interval");
		this.radius = getConfig().getDouble("Abilities.Air.AirScooter.Radius");
		this.cooldown = getConfig().getLong("Abilities.Air.AirScooter.Cooldown");
		this.maxHeightFromGround = getConfig().getDouble("Abilities.Air.AirScooter.MaxHeightFromGround");
		this.random = new Random();
		this.angles = new ArrayList<>();
		canFly = player.getAllowFlight();
		hadFly = player.isFlying();
		
		new Flight(player);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setSprinting(false);

		for (int i = 0; i < 5; i++) {
			angles.add((double) (60 * i));
		}

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

		Vector velocity = player.getEyeLocation().getDirection().clone();
		velocity.setY(0);
		velocity = velocity.clone().normalize().multiply(speed);

		if (System.currentTimeMillis() > startTime + interval) {
			if (player.getVelocity().length() < speed * .5) {
				remove();
				return;
			}
			spinScooter();
		}

		double distance = player.getLocation().getY() - (double) floorblock.getY();
		double dx = Math.abs(distance - 2.4);
		if (distance > 2.75) {
			velocity.setY(-.25 * dx * dx);
		} else if (distance < 2) {
			velocity.setY(.25 * dx * dx);
		} else {
			velocity.setY(0);
		}

		Location loc = player.getLocation();
		if (!WaterAbility.isWater(player.getLocation().add(0, 2, 0).getBlock())) {
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

	@Override
	public void remove() {
		super.remove();
		bPlayer.addCooldown(this);
		player.setAllowFlight(canFly);
		player.setFlying(hadFly);
	}

	private void spinScooter() {
		Location origin = player.getLocation().clone();
		origin.add(0, -radius, 0);

		for (int i = 0; i < 5; i++) {
			double x = Math.cos(Math.toRadians(angles.get(i))) * radius;
			double y = ((double) i) / 2 * radius - radius;
			double z = Math.sin(Math.toRadians(angles.get(i))) * radius;
			playAirbendingParticles(origin.clone().add(x, y, z), 7);
		}
		for (int i = 0; i < 5; i++) {
			angles.set(i, angles.get(i) + 10);
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
}
