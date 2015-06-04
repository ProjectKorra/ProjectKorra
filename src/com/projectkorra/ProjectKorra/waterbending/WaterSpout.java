package com.projectkorra.ProjectKorra.waterbending;

import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.TempBlock;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.chiblocking.Paralyze;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class WaterSpout {

	public static ConcurrentHashMap<Player, WaterSpout> instances = new ConcurrentHashMap<Player, WaterSpout>();
	public static ConcurrentHashMap<Block, Block> affectedblocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Block, Block> newaffectedblocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Block, Block> baseblocks = new ConcurrentHashMap<Block, Block>();
	public static ConcurrentHashMap<Block, Long> revert = new ConcurrentHashMap<Block, Long>();
	
	private static final int HEIGHT = ProjectKorra.plugin.getConfig().getInt("Abilities.Water.WaterSpout.Height");
	private static final boolean PARTICLES = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.WaterSpout.Particles");
	private static final boolean BLOCKS = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Water.WaterSpout.BlockSpiral");

	// private static final double threshold = .05;
	// private static final byte half = 0x4;
	@SuppressWarnings("unused")
	private static final byte full = 0x0;
	private Player player;
	private Block base;
	private TempBlock baseblock;
	private int defaultheight = HEIGHT;
	private long time = 0;
	private long interval = 50;
	private int angle = 0;
	private double rotation;
	
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
		
		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), 0, -50);
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
	
	private static void progressRevert(boolean ignoreTime){
		for(Block block : revert.keySet()){
			long time = revert.get(block);
			if(System.currentTimeMillis() > time || ignoreTime){
				if(TempBlock.isTempBlock(block))
					TempBlock.revertBlock(block, Material.AIR);
				revert.remove(block);
			}
		}
	}

	public static void handleSpouts(Server server) {
		// affectedblocks.clear();
		newaffectedblocks.clear();
		progressRevert(false);
		
		for (Player player : instances.keySet()) {
			if (!player.isOnline() || player.isDead()) {
				instances.get(player).remove();
			} else if (GeneralMethods.canBend(player.getName(), "WaterSpout")) {
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
			if (GeneralMethods.rand.nextInt(4) == 0) {
				WaterMethods.playWaterbendingSound(player.getLocation());
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
					instances.get(player).rotateParticles(block);
					newaffectedblocks.put(block, block);
				}
				instances.get(player).displayWaterSpiral(location.clone().add(.5,0,.5));
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
	
	public void rotateParticles(Block block)
	{
		if(!PARTICLES)
			return;
		
		if (System.currentTimeMillis() >= time + interval)
		{
			time = System.currentTimeMillis();

			Location location = block.getLocation();
			Location playerloc = player.getLocation();
			location = new Location(location.getWorld(), playerloc.getX(), location.getY(), playerloc.getZ());

			double dy = playerloc.getY() - block.getY();
			if (dy > HEIGHT)
				dy = HEIGHT;
			float[] directions = { -0.5f, 0.325f, 0.25f, 0.125f, 0.f, 0.125f, 0.25f, 0.325f, 0.5f };
			int index = angle;

			angle++;
			if (angle >= directions.length)
				angle = 0;
			for (int i = 1; i <= dy; i++)
			{

				index += 1;
				if (index >= directions.length)
					index = 0;

				Location effectloc2 = new Location(location.getWorld(), location.getX(), block.getY() + i,
						location.getZ());

				ParticleEffect.WATER_SPLASH.display(effectloc2, directions[index], directions[index],
						directions[index], 5, HEIGHT + 5);
			}
		}
	}

	private static int spoutableWaterHeight(Location location, Player player) {
		WaterSpout spout = instances.get(player);
		int height = spout.defaultheight;
		if (WaterMethods.isNight(player.getWorld()))
			height = (int) WaterMethods.waterbendingNightAugment((double) height, player.getWorld());
		int maxheight = (int) ((double) spout.defaultheight * ProjectKorra.plugin.getConfig().getDouble("Properties.Water.NightFactor")) + 5;
		Block blocki;
		for (int i = 0; i < maxheight; i++) {
			blocki = location.clone().add(0, -i, 0).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(player, "WaterSpout", blocki.getLocation()))
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
				if ((blocki.getType() != Material.AIR && (!WaterMethods.isPlant(blocki) || !WaterMethods.canPlantbend(player)))) {
					revertBaseBlock(player);
					return -1;
				}
			}
		}
		revertBaseBlock(player);
		return -1;
	}
	
	private void displayWaterSpiral(Location location) {

		if (!BLOCKS) {
			return;
		}

		double maxHeight = player.getLocation().getY() - location.getY() - .5;
		double height = 0;
		rotation += .4;
		int i = 0;
		while (height < maxHeight) {
			i += 20;
			height += .4;
			double angle = (i * Math.PI / 180);
			double x = 1 * Math.cos(angle + rotation);
			double z = 1 * Math.sin(angle + rotation);
			Location loc = location.clone().getBlock().getLocation()
					.add(.5, .5, .5);
			loc.add(x, height, z);

			Block block = loc.getBlock();
			if (block.getType().equals(Material.AIR)
					|| !GeneralMethods.isSolid(block)) {
				revert.put(block, 0L);
				new TempBlock(block, Material.STATIONARY_WATER, (byte) 1);
			}
		}
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
		progressRevert(true);
		revert.clear();
		
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