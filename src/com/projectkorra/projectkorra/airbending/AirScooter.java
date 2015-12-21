package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class AirScooter implements ConfigLoadable {

	public static ConcurrentHashMap<Player, AirScooter> instances = new ConcurrentHashMap<>();

	private static double configSpeed = config.get().getDouble("Abilities.Air.AirScooter.Speed");
	private static final long interval = 100;
	private static final double scooterradius = 1;

	private Player player;
	private Block floorblock;
	private long time;
	private double speed;
	private ArrayList<Double> angles = new ArrayList<Double>();

	public AirScooter(Player player) {
		/* Initial Check */
		if (check(player)) {
			return;
		}
		if (!player.isSprinting() || GeneralMethods.isSolid(player.getEyeLocation().getBlock())
				|| player.getEyeLocation().getBlock().isLiquid())
			return;
		if (GeneralMethods.isSolid(player.getLocation().add(0, -.5, 0).getBlock()))
			return;
		/* End Initial Check */
		// reloadVariables();
		this.player = player;
		// wasflying = player.isFlying();
		// canfly = player.getAllowFlight();
		new Flight(player);
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setSprinting(false);
		time = System.currentTimeMillis();
		for (int i = 0; i < 5; i++) {
			angles.add((double) (60 * i));
		}
		instances.put(player, this);
		speed = configSpeed;
		progress();
	}

	/**
	 * Checks if player has an instance already and removes if they do.
	 * 
	 * @param player The player to check
	 * @return false If player doesn't have an instance
	 */
	public static boolean check(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return true;
		}
		return false;
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (AirScooter scooter : instances.values()) {
			players.add(scooter.getPlayer());
		}
		return players;
	}

	private void getFloor() {
		floorblock = null;
		for (int i = 0; i <= 7; i++) {
			Block block = player.getEyeLocation().getBlock().getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				floorblock = block;
				return;
			}
		}
	}

	public Player getPlayer() {
		return player;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double value) {
		this.speed = value; // Used in PK Items
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}
		getFloor();
		// Methods.verbose(player);
		if (floorblock == null) {
			remove();
			return false;
		}
		if (!GeneralMethods.canBend(player.getName(), "AirScooter")) {
			remove();
			return false;
		}
		if (!player.isOnline() || player.isDead() || !player.isFlying()) {
			remove();
			return false;
		}

		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirScooter", player.getLocation())) {
			remove();
			return false;
		}

		// if (Methods
		// .isSolid(player
		// .getEyeLocation()
		// .clone()
		// .add(player.getEyeLocation().getDirection().clone()
		// .normalize()).getBlock())) {
		// remove();
		// return;
		// }
		// player.sendBlockChange(floorblock.getLocation(), 89, (byte) 1);
		// player.getLocation().setY((double) floorblock.getY() + 2.5);

		Vector velocity = player.getEyeLocation().getDirection().clone();
		velocity.setY(0);
		velocity = velocity.clone().normalize().multiply(speed);
		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();
			if (player.getVelocity().length() < speed * .5) {
				remove();
				return false;
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
		if (!WaterMethods.isWater(player.getLocation().add(0, 2, 0).getBlock()))
			loc.setY((double) floorblock.getY() + 1.5);
		else
			return false;
		// player.setFlying(true);
		// player.teleport(loc.add(velocity));
		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.setVelocity(velocity);
		if (GeneralMethods.rand.nextInt(4) == 0) {
			AirMethods.playAirbendingSound(player.getLocation());
		}
		return true;
	}

	public static void progressAll() {
		for (AirScooter ability : instances.values()) {
			ability.progress();
		}
	}

	@Override
	public void reloadVariables() {
		configSpeed = config.get().getDouble("Abilities.Air.AirScooter.Speed");
		this.speed = configSpeed;
	}

	public void remove() {
		instances.remove(player);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setSprinting(false);
	}

	public static void removeAll() {
		for (AirScooter ability : instances.values()) {
			ability.remove();
		}
	}

	private void spinScooter() {
		Location origin = player.getLocation().clone();
		origin.add(0, -scooterradius, 0);
		for (int i = 0; i < 5; i++) {
			double x = Math.cos(Math.toRadians(angles.get(i))) * scooterradius;
			double y = ((double) i) / 2 * scooterradius - scooterradius;
			double z = Math.sin(Math.toRadians(angles.get(i))) * scooterradius;
			AirMethods.playAirbendingParticles(origin.clone().add(x, y, z), 7);
			// player.getWorld().playEffect(origin.clone().add(x, y, z),
			// Effect.SMOKE, 4, (int) AirBlast.defaultrange);
		}
		for (int i = 0; i < 5; i++) {
			angles.set(i, angles.get(i) + 10);
		}
	}

}
