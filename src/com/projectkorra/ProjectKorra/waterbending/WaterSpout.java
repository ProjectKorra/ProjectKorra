package com.projectkorra.ProjectKorra.waterbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.chiblocking.Paralyze;

public class WaterSpout {

	public static ConcurrentHashMap<Player, WaterSpout> instances = new ConcurrentHashMap<Player, WaterSpout>();
	public static ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Block, Block> newaffectedblocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Block, Block> baseblocks = new ConcurrentHashMap<Block, Block>();

	private static final int HEIGHT = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.WaterSpout.Height");

	// private static final double threshold = .05;
	// private static final byte half = 0x4;
	private static final byte full = 0x0;
	private Player player;
	private Block base;
	private TempBlock baseblock;
	private int defaultheight = HEIGHT;

	public WaterSpout(Player player) {
		//		if (BendingPlayer.getBendingPlayer(player).isOnCooldown(
		//				Abilities.WaterSpout))
		//			return;

		if (instances.containsKey(player)) {
			instances.get(player).remove();
			return;
		}
		this.player = player;
		
		WaterWave wwave = new WaterWave(player, WaterWave.AbilityType.CLICK);
		if(WaterWave.instances.contains(wwave))
			return;
		
		Block topBlock = Methods.getTopBlock(player.getLocation(), 0, -50);
		if(topBlock == null)
			topBlock = player.getLocation().getBlock();
		Material mat = topBlock.getType();
		if(mat != Material.WATER && mat != Material.STATIONARY_WATER
				&& mat != Material.ICE && mat != Material.PACKED_ICE && mat != Material.SNOW 
				&& mat != Material.SNOW_BLOCK)
			return;
		
		new Flight(player);
		player.setAllowFlight(true);
		instances.put(player, this);
		spout(player);
	}

	private void remove() {
		revertBaseBlock(player);
		instances.remove(player);
	}

	public static void handleSpouts(Server server) {
		// affectedblocks.clear();
		newaffectedblocks.clear();

		for (Player player : instances.keySet()) {
			if (!player.isOnline() || player.isDead()) {
				instances.get(player).remove();
			} else if (Methods.canBend(player.getName(), "WaterSpout")) {
				spout(player);
			} else {
				instances.get(player).remove();
			}
		}

		for (Block block : affectedblocks.keySet()) {
			if (!newaffectedblocks.containsKey(block)) {
				remove(block);
			}
		}

		// for (Block block : affectedblocks.keySet()) {
		// boolean remove = true;
		// for (Player player : instances.keySet()) {
		// if (Methods.hasAbility(player, Abilities.WaterSpout)
		// && Methods.canBend(player, Abilities.WaterSpout)
		// && player.getWorld() == block.getWorld()) {
		// Location loc1 = player.getLocation().clone();
		// loc1.setY(0);
		// Location loc2 = block.getLocation().clone();
		// loc2.setY(0);
		// if (loc1.distance(loc2) < 1)
		// remove = false;
		// }
		// }
		// if (remove)
		// remove(block);
		// }

	}

	private static void remove(Block block) {
		affectedblocks.remove(block);
		TempBlock.revertBlock(block, Material.AIR);
		// block.setType(Material.AIR);
		// block.setData(half);
	}

	public static void spout(Player player) {
		WaterSpout spout = instances.get(player);
		if (Bloodbending.isBloodbended(player) || Paralyze.isParalyzed(player)) {
			instances.get(player).remove();
		} else {

			player.setFallDistance(0);
			player.setSprinting(false);
			if (Methods.rand.nextInt(4) == 0) {
				Methods.playWaterbendingSound(player.getLocation());
			}		
			// if (player.getVelocity().length() > threshold) {
			// // Methods.verbose("Too fast!");
			// player.setVelocity(player.getVelocity().clone().normalize()
			// .multiply(threshold * .5));
			// }
			player.removePotionEffect(PotionEffectType.SPEED);
			Location location = player.getLocation().clone().add(0, .2, 0);
			Block block = location.clone().getBlock();
			int height = spoutableWaterHeight(location, player);

			// Methods.verbose(height + " " + WaterSpout.height + " "
			// + affectedblocks.size());
			if (height != -1) {
				location = spout.base.getLocation();
				for (int i = 1; i <= height; i++) {
					block = location.clone().add(0, i, 0).getBlock();
					if (!TempBlock.isTempBlock(block)) {
						new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
					}
					// block.setType(Material.WATER);
					// block.setData(full);
					if (!affectedblocks.containsKey(block)) {
						affectedblocks.put(block, block);
					}
					newaffectedblocks.put(block, block);
				}
				if (player.getLocation().getBlockY() > block.getY()) {
					player.setFlying(false);
				} else {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFlying(true);
				}
			} else {
				instances.get(player).remove();
			}
		}
	}

	private static int spoutableWaterHeight(Location location, Player player) {
		WaterSpout spout = instances.get(player);
		int height = spout.defaultheight;
		if (Methods.isNight(player.getWorld()))
			height = (int) Methods.waterbendingNightAugment((double) height, player.getWorld());
		int maxheight = (int) ((double) spout.defaultheight * ProjectKorra.plugin.getConfig().getDouble("Properties.Water.NightFactor")) + 5;
		Block blocki;
		for (int i = 0; i < maxheight; i++) {
			blocki = location.clone().add(0, -i, 0).getBlock();
			if (Methods.isRegionProtectedFromBuild(player, "WaterSpout", blocki.getLocation()))
				return -1;
			if (!affectedblocks.contains(blocki)) {
				if (blocki.getType() == Material.WATER || blocki.getType() == Material.STATIONARY_WATER) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock(player);
					}
					spout.base = blocki;
					if (i > height)
						return height;
					return i;
				}
				if (blocki.getType() == Material.ICE || blocki.getType() == Material.SNOW || blocki.getType() == Material.SNOW_BLOCK) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock(player);
						instances.get(player).baseblock = new TempBlock(blocki,	Material.STATIONARY_WATER, (byte) 8);
					}
					// blocki.setType(Material.WATER);
					// blocki.setData(full);
					spout.base = blocki;
					if (i > height)
						return height;
					return i;
				}
				if ((blocki.getType() != Material.AIR && (!Methods.isPlant(blocki) || !Methods.canPlantbend(player)))) {
					revertBaseBlock(player);
					return -1;
				}
			}
		}
		revertBaseBlock(player);
		return -1;
	}

	public static void revertBaseBlock(Player player) {
		if (instances.containsKey(player)) {
			if (instances.get(player).baseblock != null) {
				instances.get(player).baseblock.revertBlock();
				instances.get(player).baseblock = null;
			}
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).remove();
		}
		for (Block block : affectedblocks.keySet()) {
			// block.setType(Material.AIR);
			TempBlock.revertBlock(block, Material.AIR);
			affectedblocks.remove(block);
		}
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet())
			players.add(player);
		return players;
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

				if (distance <= radius && dy > 0 && dy < instances.get(player).defaultheight){
					removed = true;
					instances.get(player).remove();
				}
			}
		}
		return removed;
	}

	public static String getDescription() {
		return "To use this ability, click while over or in water. "
				+ "You will spout water up from beneath you to experience controlled levitation. "
				+ "This ability is a toggle, so you can activate it then use other abilities and it "
				+ "will remain on. If you try to spout over an area with no water, snow or ice, "
				+ "the spout will dissipate and you will fall. Click again with this ability selected to deactivate it.";
	}

	public Player getPlayer() {
		return player;
	}

	public int getDefaultheight() {
		return defaultheight;
	}

	public void setDefaultheight(int defaultheight) {
		this.defaultheight = defaultheight;
	}
}