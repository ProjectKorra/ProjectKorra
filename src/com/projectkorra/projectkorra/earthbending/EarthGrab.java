package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class EarthGrab {

	private static double range = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.EarthGrab.Range");
	private static long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Earth.EarthGrab.Cooldown");

	public EarthGrab(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("EarthGrab"))
			return;

		Location origin = player.getEyeLocation();
		Vector direction = origin.getDirection();
		double lowestdistance = range + 1;
		Entity closestentity = null;
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, range)) {
			if (GeneralMethods.getDistanceFromLine(direction, origin, entity.getLocation()) <= 3
					&& (entity instanceof LivingEntity) && (entity.getEntityId() != player.getEntityId())) {
				double distance = origin.distance(entity.getLocation());
				if (distance < lowestdistance) {
					closestentity = entity;
					lowestdistance = distance;
				}
			}
		}

		if (closestentity != null) {
			// Methods.verbose("grabbing");
			ArrayList<Block> blocks = new ArrayList<Block>();
			Location location = closestentity.getLocation();
			Location loc1 = location.clone();
			Location loc2 = location.clone();
			Location testloc, testloc2;
			double factor = 3;
			double factor2 = 4;
			int height1 = 3;
			int height2 = 2;
			for (double angle = 0; angle <= 360; angle += 20) {
				testloc = loc1.clone().add(factor * Math.cos(Math.toRadians(angle)), 1, factor * Math.sin(Math.toRadians(angle)));
				testloc2 = loc2.clone().add(factor2 * Math.cos(Math.toRadians(angle)), 1,
						factor2 * Math.sin(Math.toRadians(angle)));
				for (int y = 0; y < EarthColumn.standardheight - height1; y++) {
					testloc = testloc.clone().add(0, -1, 0);
					if (EarthMethods.isEarthbendable(player, testloc.getBlock())) {
						if (!blocks.contains(testloc.getBlock())) {
							new EarthColumn(player, testloc, height1 + y - 1);
						}
						blocks.add(testloc.getBlock());
						break;
					}
				}
				for (int y = 0; y < EarthColumn.standardheight - height2; y++) {
					testloc2 = testloc2.clone().add(0, -1, 0);
					if (EarthMethods.isEarthbendable(player, testloc2.getBlock())) {
						if (!blocks.contains(testloc2.getBlock())) {
							new EarthColumn(player, testloc2, height2 + y - 1);
						}
						blocks.add(testloc2.getBlock());
						break;
					}
				}
			}

			if (!blocks.isEmpty())
				bPlayer.addCooldown("EarthGrab", cooldown);
		}
	}

	public static void EarthGrabSelf(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("EarthGrab"))
			return;

		Entity closestentity = player;

		if (closestentity != null) {
			// Methods.verbose("grabbing");
			ArrayList<Block> blocks = new ArrayList<Block>();
			Location location = closestentity.getLocation();
			Location loc1 = location.clone();
			Location loc2 = location.clone();
			Location testloc, testloc2;
			double factor = 3;
			double factor2 = 4;
			int height1 = 3;
			int height2 = 2;
			for (double angle = 0; angle <= 360; angle += 20) {
				testloc = loc1.clone().add(factor * Math.cos(Math.toRadians(angle)), 1, factor * Math.sin(Math.toRadians(angle)));
				testloc2 = loc2.clone().add(factor2 * Math.cos(Math.toRadians(angle)), 1,
						factor2 * Math.sin(Math.toRadians(angle)));
				for (int y = 0; y < EarthColumn.standardheight - height1; y++) {
					testloc = testloc.clone().add(0, -1, 0);
					if (EarthMethods.isEarthbendable(player, testloc.getBlock())) {
						if (!blocks.contains(testloc.getBlock())) {
							new EarthColumn(player, testloc, height1 + y - 1);
						}
						blocks.add(testloc.getBlock());
						break;
					}
				}
				for (int y = 0; y < EarthColumn.standardheight - height2; y++) {
					testloc2 = testloc2.clone().add(0, -1, 0);
					if (EarthMethods.isEarthbendable(player, testloc2.getBlock())) {
						if (!blocks.contains(testloc2.getBlock())) {
							new EarthColumn(player, testloc2, height2 + y - 1);
						}
						blocks.add(testloc2.getBlock());
						break;
					}
				}
			}

			if (!blocks.isEmpty())
				bPlayer.addCooldown("EarthGrab", GeneralMethods.getGlobalCooldown());
		}
	}
}