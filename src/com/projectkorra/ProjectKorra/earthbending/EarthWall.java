package com.projectkorra.ProjectKorra.earthbending;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Methods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;

public class EarthWall {

	private static final int range = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Range");
	private static final int defaultheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Height");
	private static final int defaulthalfwidth = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Width") / 2;

	private int height = defaultheight;
	private int halfwidth = defaulthalfwidth;

	public EarthWall(Player player) {
		if (EarthColumn.cooldowns.containsKey(player.getName())) {
			if (EarthColumn.cooldowns.get(player.getName()) + ProjectKorra.plugin.getConfig().getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return;
			} else {
				EarthColumn.cooldowns.remove(player.getName());
			}
		}

		if (AvatarState.isAvatarState(player)) {
			height = (int) (2. / 5. * (double) AvatarState.getValue(height));
			halfwidth = AvatarState.getValue(halfwidth);
		}

		Vector direction = player.getEyeLocation().getDirection().normalize();

		double ox, oy, oz;
		ox = -direction.getZ();
		oy = 0;
		oz = direction.getX();

		Vector orth = new Vector(ox, oy, oz);
		orth = orth.normalize();

		Block sblock = Methods.getEarthSourceBlock(player, range);
		Location origin;
		if (sblock == null) {
			origin = player.getTargetBlock(Methods.getTransparentEarthbending(),
					range).getLocation();
		} else {
			origin = sblock.getLocation();
		}
		World world = origin.getWorld();

		boolean cooldown = false;

		for (int i = -halfwidth; i <= halfwidth; i++) {
			Block block = world.getBlockAt(origin.clone().add(
					orth.clone().multiply((double) i)));
			// if (block.getType() == Material.AIR || block.isLiquid()) {
			if (Methods.isTransparentToEarthbending(player, block)) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.DOWN);
					if (Methods.isEarthbendable(player, block)) {
						cooldown = true;
						new EarthColumn(player, block.getLocation(), height);
						// } else if (block.getType() != Material.AIR
						// && !block.isLiquid()) {
					} else if (!Methods
							.isTransparentToEarthbending(player, block)) {
						break;
					}
				}
			} else if (Methods.isEarthbendable(player,
					block.getRelative(BlockFace.UP))) {
				for (int j = 1; j < height; j++) {
					block = block.getRelative(BlockFace.UP);
					// if (block.getType() == Material.AIR || block.isLiquid())
					// {
					if (Methods.isTransparentToEarthbending(player, block)) {
						cooldown = true;
						new EarthColumn(player, block.getRelative(
								BlockFace.DOWN).getLocation(), height);
					} else if (!Methods.isEarthbendable(player, block)) {
						break;
					}
				}
			} else if (Methods.isEarthbendable(player, block)) {
				cooldown = true;
				new EarthColumn(player, block.getLocation(), height);
			}
		}

		if (cooldown)
			EarthColumn.cooldowns.put(player.getName(), System.currentTimeMillis());

	}

}