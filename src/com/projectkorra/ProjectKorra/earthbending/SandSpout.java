package com.projectkorra.ProjectKorra.earthbending;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class SandSpout {

	public static ConcurrentHashMap<Player, SandSpout> instances = new ConcurrentHashMap<Player, SandSpout>();

	private static final double HEIGHT = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.SandSpout.Height");
	private static final int BTIME = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.SandSpout.BlindnessTime");
	private static final int SPOUTDAMAGE = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.SandSpout.SpoutDamage");
	private static final boolean SPIRAL = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.SandSpout.Spiral");
	private static final long interval = 100;

	private Player player;
	private long time;
	private int angle = 0;
	private double height = HEIGHT;
	private int bTime = BTIME;
	private double spoutDamage = SPOUTDAMAGE;
	private double y = 0D;

	public SandSpout(Player player) {

		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return;
		}
		this.player = player;
		time = System.currentTimeMillis();
		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), 0, -50);
		if (topBlock == null) topBlock = player.getLocation().getBlock();
		Material mat = topBlock.getType();
		if (mat != Material.SAND && mat != Material.SANDSTONE && mat != Material.RED_SANDSTONE) return;
		new Flight(player);
		instances.put(player, this);
		spout();
	}

	public static void spoutAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).spout();
		}
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		players.addAll(instances.keySet());
		return players;
	}

	private void spout() {
		if (!GeneralMethods.canBend(player.getName(), "SandSpout")
		//				|| !Methods.hasAbility(player, Abilities.SandSpout)
		|| player.getEyeLocation().getBlock().isLiquid() || GeneralMethods.isSolid(player.getEyeLocation().getBlock()) || player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		player.setFallDistance(0);
		player.setSprinting(false);
		if (GeneralMethods.rand.nextInt(5) == 0) {
			EarthMethods.playSandBendingSound(player.getLocation());
		}
		Block block = getGround();
		if (block != null && (block.getType() == Material.SAND || block.getType() == Material.SANDSTONE || block.getType() == Material.RED_SANDSTONE)) {
			double dy = player.getLocation().getY() - block.getY();
			if (dy > height) {
				removeFlight();
			} else {
				allowFlight();
			}
			rotateSandColumn(block);
		} else {
			remove();
		}
	}

	private void allowFlight() {
		player.setAllowFlight(true);
		player.setFlying(true);
		player.setFlySpeed(.05f);
	}

	private void removeFlight() {
		player.setAllowFlight(false);
		player.setFlying(false);
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

	@SuppressWarnings("deprecation")
	private void rotateSandColumn(Block block) {

		if (System.currentTimeMillis() >= time + interval) {
			time = System.currentTimeMillis();

			Location location = block.getLocation();
			Location playerloc = player.getLocation();
			location = new Location(location.getWorld(), playerloc.getX(), location.getY(), playerloc.getZ());

			double dy = playerloc.getY() - block.getY();
			if (dy > height) dy = height;
			Integer[] directions = { 0, 1, 2, 3, 5, 6, 7, 8 };
			int index = angle;

			angle++;
			if (angle >= directions.length) angle = 0;
			for (int i = 1; i <= dy; i++) {

				index += 1;
				if (index >= directions.length) index = 0;

				Location effectloc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());

				if (SPIRAL) {
					displayHelix(block.getLocation(), this.player.getLocation(), block);
				}
				if (i % 2 == 0 && block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 0) || block.getType() == Material.SANDSTONE)) {
					EarthMethods.displaySandParticle(effectloc2, 1f, 3f, 1f, 20, .2f, false);
				} else if (i % 2 == 0 && block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 1) || block.getType() == Material.RED_SANDSTONE)) {
					EarthMethods.displaySandParticle(effectloc2, 1f, 3f, 1f, 20, .2f, true);
				}

				Collection<Player> players = GeneralMethods.getPlayersAroundPoint(effectloc2, 1.5f);
				if (!players.isEmpty()) for (Player sPlayer : players) {
					if (!sPlayer.equals(player)) {
						sPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, bTime * 20, 1));
						GeneralMethods.damageEntity(player, sPlayer, spoutDamage);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void displayHelix(Location location, Location player, Block block) {
		// TODO Auto-generated method stub
		//double radius = 1.5;
		this.y += 0.1;
		if (this.y >= player.getY() - location.getY()) {
			this.y = 0D;
		}
		for (int points = 0; points <= 5; points++) {
			double x = Math.cos(y);
			double z = Math.sin(y);
			double nx = x * -1;
			double nz = z * -1;
			Location newloc = new Location(player.getWorld(), location.getX() + x, location.getY() + y, location.getZ() + z);
			Location secondloc = new Location(player.getWorld(), location.getX() + nx, location.getY() + y, location.getZ() + nz);
			if (points % 2 == 0 && block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 0) || block.getType() == Material.SANDSTONE)) {
				EarthMethods.displaySandParticle(newloc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, false);
				EarthMethods.displaySandParticle(secondloc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, false);
			} else if (points % 2 == 0 && block != null && ((block.getType() == Material.SAND && block.getData() == (byte) 1) || block.getType() == Material.RED_SANDSTONE)) {
				EarthMethods.displaySandParticle(newloc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, true);
				EarthMethods.displaySandParticle(secondloc.add(0.5, 0.5, 0.5), 0.1F, 0.1F, 0.1F, 2, 1, true);
			}
		}
	}

	public static boolean removeSpouts(Location loc0, double radius, Player sourceplayer) {
		boolean removed = false;
		for (Player player : instances.keySet()) {
			if (!player.equals(sourceplayer)) {
				Location loc1 = player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				double dx = loc1.getX() - loc0.getX();
				double dy = loc1.getY() - loc0.getY();
				double dz = loc1.getZ() - loc0.getZ();

				double distance = Math.sqrt(dx * dx + dz * dz);

				if (distance <= radius && dy > 0 && dy < HEIGHT) {
					instances.get(player).remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	private void remove() {
		removeFlight();
		player.setFlySpeed(.1f);
		instances.remove(player);
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).remove();
		}
	}

	public Player getPlayer() {
		return player;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

}