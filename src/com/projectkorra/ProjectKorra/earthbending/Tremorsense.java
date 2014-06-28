package com.projectkorra.ProjectKorra.earthbending;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;

public class Tremorsense {

	public static ConcurrentHashMap<Player, Tremorsense> instances = new ConcurrentHashMap<Player, Tremorsense>();
	public static ConcurrentHashMap<Block, Player> blocks = new ConcurrentHashMap<Block, Player>();
	public static Map<String, Long> cooldowns = new HashMap<String, Long>();

	// private static final long cooldown = ConfigManager.tremorsenseCooldown;
	private static final int maxdepth = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Tremorsense.MaxDepth");
	private static final int radius = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Tremorsense.Radius");
	private static final byte lightthreshold = (byte) ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Tremorsense.LightThreshold");
	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.Tremorsense.Cooldown");

	private Player player;
	private Block block;

	// private static ConcurrentHashMap<Player, Long> timers = new
	// ConcurrentHashMap<Player, Long>();

	public Tremorsense(Player player) {
		// if (timers.containsKey(player)) {
		// if (System.currentTimeMillis() < timers.get(player) + cooldown)
		// return;
		// }
		if (cooldowns.containsKey(player.getName())) {
			if (cooldowns.get(player.getName()) + cooldown >= System.currentTimeMillis()) {
				return;
			} else {
				cooldowns.remove(player.getName());
			}
		}

		if (Methods.isEarthbendable(player, player
				.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
			this.player = player;
			cooldowns.put(player.getName(), System.currentTimeMillis());
			// timers.put(player, System.currentTimeMillis());
			activate();
		}
	}

	public Tremorsense(Player player, boolean value) {
		this.player = player;
		set();
	}

	private void activate() {
		Block block = player.getLocation().getBlock()
				.getRelative(BlockFace.DOWN);
		for (int i = -radius; i <= radius; i++) {
			for (int j = -radius; j <= radius; j++) {
				boolean earth = false;
				boolean foundair = false;
				Block smokeblock = null;
				for (int k = 0; k <= maxdepth; k++) {
					Block blocki = block.getRelative(BlockFace.EAST, i)
							.getRelative(BlockFace.NORTH, j)
							.getRelative(BlockFace.DOWN, k);
					if (Methods.isRegionProtectedFromBuild(player,
							"RaiseEarth", blocki.getLocation()))
						continue;
					if (Methods.isEarthbendable(player,
							blocki) && !earth) {
						earth = true;
						smokeblock = blocki;
					} else if (!Methods.isEarthbendable(player, blocki) && earth) {
						foundair = true;
						break;
					} else if (!Methods.isEarthbendable(player, blocki)
							&& !earth
							&& blocki.getType() != Material.AIR) {
						break;
					}
				}
				if (foundair) {
					smokeblock.getWorld().playEffect(
							smokeblock.getRelative(BlockFace.UP).getLocation(),
							Effect.SMOKE, 4, radius);
				}
			}
		}

	}

	private void set() {
		Block standblock = player.getLocation().getBlock()
				.getRelative(BlockFace.DOWN);

		BendingPlayer bp = Methods.getBendingPlayer(player.getName());
		if (!bp.isTremorsensing()) {
			if (block != null)
				revert();
			return;
		}

		if (Methods.isEarthbendable(player, standblock)
				&& block == null) {
			block = standblock;
			player.sendBlockChange(block.getLocation(), 89, (byte) 1);
			instances.put(player, this);
		} else if (Methods.isEarthbendable(player,
				standblock) && !block.equals(standblock)) {
			revert();
			block = standblock;
			player.sendBlockChange(block.getLocation(), 89, (byte) 1);
			instances.put(player, this);
		} else if (block == null) {
			return;
		} else if (player.getWorld() != block.getWorld()) {
			revert();
		} else if (!Methods.isEarthbendable(player,
				standblock)) {
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

	private void revert() {
		if (block != null) {
			player.sendBlockChange(block.getLocation(), block.getTypeId(),
					block.getData());
			instances.remove(player);
		}
	}

	public static void manage(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			if (instances.containsKey(player)
					&& (!Methods.canBend(player.getName(), "Tremorsense") || player
							.getLocation().getBlock().getLightLevel() > lightthreshold)) {
				instances.get(player).revert();
			} else if (instances.containsKey(player)) {
				instances.get(player).set();
			} else if (Methods.canBend(player.getName(), "Tremorsense")
					&& player.getLocation().getBlock().getLightLevel() < lightthreshold) {
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
		return "This is a pure utility ability for earthbenders. If you have this ability bound to any "
				+ "slot whatsoever, then you are able to 'see' using the earth. If you are in an area of low-light "
				+ "and are standing on top of an earthbendable block, this ability will automatically turn that block into "
				+ "glowstone, visible *only by you*. If you lose contact with a bendable block, the light will go out, "
				+ "as you have lost contact with the earth and cannot 'see' until you can touch earth again. "
				+ "Additionally, if you click with this ability selected, smoke will appear above nearby earth "
				+ "with pockets of air beneath them.";
	}

}