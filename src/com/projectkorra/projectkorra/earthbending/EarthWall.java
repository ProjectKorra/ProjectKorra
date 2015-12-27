package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class EarthWall {

	private static final int defaultheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Height");
	private static final int defaulthalfwidth = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.RaiseEarth.Wall.Width") / 2;
	private static int selectRange = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.RaiseEarth.SelectRange");
	private static boolean dynamic = ProjectKorra.plugin.getConfig().getBoolean("Abilities.Earth.RaiseEarth.DynamicSourcing.Enabled");
	
	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.RaiseEarth.Cooldown");
	private int height = defaultheight;
	private int halfwidth = defaulthalfwidth;

	public EarthWall(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("RaiseEarth"))
			return;

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
		Block sblock = BlockSource.getEarthSourceBlock(player, selectRange, selectRange, ClickType.SHIFT_DOWN, false, dynamic, true, EarthMethods.canSandbend(player), EarthMethods.canMetalbend(player));
		if (sblock != null) {
			Location origin = sblock.getLocation();
			World world = origin.getWorld();

			boolean cd = false;

			for (int i = -halfwidth; i <= halfwidth; i++) {
				Block block = world.getBlockAt(origin.clone().add(orth.clone().multiply((double) i)));
				// if (block.getType() == Material.AIR || block.isLiquid()) {
				if (EarthMethods.isTransparentToEarthbending(player, block)) {
					for (int j = 1; j < height; j++) {
						block = block.getRelative(BlockFace.DOWN);
						if (EarthMethods.isEarthbendable(player, block)) {
							cd = true;
							new EarthColumn(player, block.getLocation(), height);
							// } else if (block.getType() != Material.AIR
							// && !block.isLiquid()) {
						} else if (!EarthMethods.isTransparentToEarthbending(player, block)) {
							break;
						}
					}
				} else if (EarthMethods.isEarthbendable(player, block.getRelative(BlockFace.UP))) {
					for (int j = 1; j < height; j++) {
						block = block.getRelative(BlockFace.UP);
						// if (block.getType() == Material.AIR || block.isLiquid())
						// {
						if (EarthMethods.isTransparentToEarthbending(player, block)) {
							cd = true;
							new EarthColumn(player, block.getRelative(BlockFace.DOWN).getLocation(), height);
						} else if (!EarthMethods.isEarthbendable(player, block)) {
							break;
						}
					}
				} else if (EarthMethods.isEarthbendable(player, block)) {
					cd = true;
					new EarthColumn(player, block.getLocation(), height);
				}
			}

			if (cd)
				bPlayer.addCooldown("RaiseEarth", cooldown);
		} else {
			return;
		}
	}

}
