package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class AirScooter {

	public static ConcurrentHashMap<Player, AirScooter> instances = new ConcurrentHashMap<Player, AirScooter>();

	private static final double SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Air.AirScooter.Speed");
	private static final long interval = 100;
	private static final double scooterradius = 1;

	private Player player;
	private Block floorblock;
	private long time;
	private double speed = SPEED;
	private ArrayList<Double> angles = new ArrayList<Double>();

	public AirScooter(Player player) {

		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return;
		}
		if (!player.isSprinting()
				|| Methods.isSolid(player.getEyeLocation().getBlock())
				|| player.getEyeLocation().getBlock().isLiquid())
			return;
		if (Methods.isSolid(player.getLocation().add(0, -.5, 0).getBlock()))
			return;
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
		progress();
	}

	private void progress() {
		getFloor();
		// Methods.verbose(player);
		if (floorblock == null) {
			remove();
			return;
		}
		if (!Methods.canBend(player.getName(), "AirScooter")) {
			remove();
			return;
		}
		if (!player.isOnline() || player.isDead() || !player.isFlying()) {
			remove();
			return;
		}

		if (Methods.isRegionProtectedFromBuild(player, "AirScooter",
				player.getLocation())) {
			remove();
			return;
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
				return;
			}
			spinScooter();
		}
		double distance = player.getLocation().getY()
				- (double) floorblock.getY();
		double dx = Math.abs(distance - 2.4);
		if (distance > 2.75) {
			velocity.setY(-.25 * dx * dx);
		} else if (distance < 2) {
			velocity.setY(.25 * dx * dx);
		} else {
			velocity.setY(0);
		}
		Location loc = player.getLocation();
		loc.setY((double) floorblock.getY() + 1.5);
		// player.setFlying(true);
		// player.teleport(loc.add(velocity));
		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.setVelocity(velocity);
		if (Methods.rand.nextInt(4) == 0) {
			Methods.playAirbendingSound(player.getLocation());
		}
	}

	private void spinScooter() {
		Location origin = player.getLocation().clone();
		origin.add(0, -scooterradius, 0);
		for (int i = 0; i < 5; i++) {
			double x = Math.cos(Math.toRadians(angles.get(i))) * scooterradius;
			double y = ((double) i) / 2 * scooterradius - scooterradius;
			double z = Math.sin(Math.toRadians(angles.get(i))) * scooterradius;
			Methods.playAirbendingParticles(origin.clone().add(x, y, z), 10);
//			player.getWorld().playEffect(origin.clone().add(x, y, z),
//					Effect.SMOKE, 4, (int) AirBlast.defaultrange);
		}
		for (int i = 0; i < 5; i++) {
			angles.set(i, angles.get(i) + 10);
		}
	}

	private void getFloor() {
		floorblock = null;
		for (int i = 0; i <= 7; i++) {
			Block block = player.getEyeLocation().getBlock()
					.getRelative(BlockFace.DOWN, i);
			if (Methods.isSolid(block) || block.isLiquid()) {
				floorblock = block;
				return;
			}
		}
	}

	private void remove() {
		instances.remove(player);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setSprinting(false);
	}

	public static void check(Player player) {
		if (instances.containsKey(player)) {
			instances.get(player).remove();
		}
	}

	public static void progressAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).progress();
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).remove();
		}
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}

	public Player getPlayer() {
		return player;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
}