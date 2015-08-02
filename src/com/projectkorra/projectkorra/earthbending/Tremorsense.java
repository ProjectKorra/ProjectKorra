package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class Tremorsense {

	private static FileConfiguration config = ProjectKorra.plugin.getConfig();

	public static ConcurrentHashMap<Player, Tremorsense> instances = new ConcurrentHashMap<Player, Tremorsense>();
	public static ConcurrentHashMap<Block, Player> blocks = new ConcurrentHashMap<Block, Player>();

	private static final int maxdepth = config.getInt("Abilities.Earth.Tremorsense.MaxDepth");
	private static final int radius = config.getInt("Abilities.Earth.Tremorsense.Radius");
	private static final byte lightthreshold = (byte) config.getInt("Abilities.Earth.Tremorsense.LightThreshold");
	private static long cooldown = config.getLong("Abilities.Earth.Tremorsense.Cooldown");

	private Player player;
	private Block block;

	public Tremorsense(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Tremorsense"))
			return;

		if (EarthMethods.isEarthbendable(player, player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			this.player = player;
			bPlayer.addCooldown("Tremorsense", cooldown);
			activate();
		}
	}

	public Tremorsense(Player player, boolean value) {
		this.player = player;
		set();
	}

	private void activate() {
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				boolean earth = false;
				boolean foundair = false;
				Block smokeblock = null;
				for (int k = 0; k <= maxdepth; k++) {
					Block blocki = block.getRelative(BlockFace.EAST, i).getRelative(BlockFace.NORTH, j).getRelative(BlockFace.DOWN, k);
					if (GeneralMethods.isRegionProtectedFromBuild(player, "RaiseEarth", blocki.getLocation()))
						continue;
					if (EarthMethods.isEarthbendable(player, blocki) && !earth) {
						earth = true;
						smokeblock = blocki;
					} else if (!EarthMethods.isEarthbendable(player, blocki) && earth) {
						foundair = true;
						break;
					} else if (!EarthMethods.isEarthbendable(player, blocki) && !earth && blocki.getType() != Material.AIR) {
						break;
					}
				}
				if (foundair) {
					smokeblock.getWorld().playEffect(smokeblock.getRelative(BlockFace.UP).getLocation(), Effect.SMOKE, 4, radius);
				}
			}
		}

	}

	@SuppressWarnings("deprecation")
	private void set() {
		Block standblock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);

		BendingPlayer bp = GeneralMethods.getBendingPlayer(player.getName());
		if (!bp.isTremorSensing()) {
			if (block != null)
				revert();
			return;
		}

		if (EarthMethods.isEarthbendable(player, standblock) && block == null) {
			block = standblock;
			player.sendBlockChange(block.getLocation(), 89, (byte) 1);
			instances.put(player, this);
		} else if (EarthMethods.isEarthbendable(player, standblock) && !block.equals(standblock)) {
			revert();
			block = standblock;
			player.sendBlockChange(block.getLocation(), 89, (byte) 1);
			instances.put(player, this);
		} else if (block == null) {
			return;
		} else if (!player.getWorld().equals(block.getWorld())) {
			revert();
		} else if (!EarthMethods.isEarthbendable(player, standblock)) {
			revert();
		}

		// Block standblock = player.getLocation().getBlock()
		// .getRelative(BlockFace.DOWN);
		//
		// if (Methods.isEarthbendable(player, Abilities.Tremorsense, standblock))
		// {
		// PotionEffect potion = new PotionEffect(
		// PotionEffectType.NIGHT_VISION, 70, 0);
		// new TempPotionEffect(player, potion);
		// }
	}

	@SuppressWarnings("deprecation")
	private void revert() {
		if (block != null) {
			player.sendBlockChange(block.getLocation(), block.getTypeId(), block.getData());
			instances.remove(player);
		}
	}

	public static void manage(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			if (instances.containsKey(player) && (!GeneralMethods.canBend(player.getName(), "Tremorsense") || player.getLocation().getBlock().getLightLevel() > lightthreshold)) {
				instances.get(player).revert();
			} else if (instances.containsKey(player)) {
				instances.get(player).set();
			} else if (GeneralMethods.canBend(player.getName(), "Tremorsense") && player.getLocation().getBlock().getLightLevel() < lightthreshold) {
				new Tremorsense(player, false);
			}
		}
	}

	public static void removeAll() {
		for (Player player : instances.keySet()) {
			instances.get(player).revert();
		}

	}

	public static String getDescription() {
		return "This is a pure utility ability for earthbenders. If you have this ability bound to any " + "slot whatsoever, then you are able to 'see' using the earth. If you are in an area of low-light " + "and are standing on top of an earthbendable block, this ability will automatically turn that block into " + "glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out, " + "as you have lost contact with the earth and cannot 'see' until you can touch earth again. " + "Additionally, if you click with this ability selected, smoke will appear above nearby earth " + "with pockets of air beneath them.";
	}

}
