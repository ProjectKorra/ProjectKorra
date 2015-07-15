package com.projectkorra.ProjectKorra.airbending;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.Ability.BaseAbility;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.firebending.Combustion;
import com.projectkorra.ProjectKorra.firebending.FireBlast;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class AirShield extends BaseAbility {

	private static double MAX_RADIUS = config.getDouble("Abilities.Air.AirShield.Radius");
	private static boolean isToggle = config.getBoolean("Abilities.Air.AirShield.IsAvatarStateToggle");
	private static int numberOfStreams = (int) (.75 * (double) MAX_RADIUS);

	private double maxradius = MAX_RADIUS;
	private double radius = 2;
	private double speedfactor;

	private Player player;
	private HashMap<Integer, Integer> angles = new HashMap<Integer, Integer>();

	public AirShield(Player player) {
		/* Initial Check */
		if (AvatarState.isAvatarState(player)
				&& getInstance(StockAbilities.AirShield).containsKey(player.getUniqueId()) && isToggle) {
			//instances.remove(player.getUniqueId());
			super.remove();
			return;
		}
		/* End Initial Check */
		reloadVariables();
		this.player = player;
		int angle = 0;
		int di = (int) (maxradius * 2 / numberOfStreams);
		for (int i = -(int) maxradius + di; i < (int) maxradius; i += di) {
			angles.put(i, angle);
			angle += 90;
			if (angle == 360)
				angle = 0;
		}

		//instances.put(player.getUniqueId(), this);
		putInstance(player, this);
	}

	public static String getDescription() {
		return "Air Shield is one of the most powerful defensive techniques in existence. "
				+ "To use, simply sneak (default: shift). "
				+ "This will create a whirlwind of air around the user, "
				+ "with a small pocket of safe space in the center. "
				+ "This wind will deflect all projectiles and will prevent any creature from "
				+ "entering it for as long as its maintained. ";
	}
	
	public static boolean isWithinShield(Location loc){
		for (UUID uuid : getInstance(StockAbilities.AirShield).keySet()) {
			AirShield ashield = (AirShield) getInstance(StockAbilities.AirShield).get(uuid);
			if (ashield.player.getLocation().getWorld() != loc.getWorld()) 
				return false;
			if(ashield.player.getLocation().distance(loc) <= ashield.radius)
				return true;
		}
		return false;
	}
	public double getMaxradius() {
		return maxradius;
	}
	public Player getPlayer() {
		return player;
	}

	@Override
	public StockAbilities getStockAbility() {
		return StockAbilities.AirShield;
	}

	@Override
	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirShield",
				player.getLocation())) {
			remove();
			return false;
		}
		speedfactor = 1;
		if (!GeneralMethods.canBend(player.getName(), "AirShield")
				|| player.getEyeLocation().getBlock().isLiquid()) {
			remove();
			return false;
		}

		if (GeneralMethods.getBoundAbility(player) == null) {
			remove();
			return false;
		}

		if (isToggle) {
			if (((!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirShield")) || (!player
					.isSneaking())) && !AvatarState.isAvatarState(player)) {
				remove();
				return false;
			}
		} else {
			if (((!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirShield")) || (!player
					.isSneaking()))) {
				remove();
				return false;
			}
		}

		//
		//		if (((!Methods.getBoundAbility(player).equalsIgnoreCase("AirShield")) || (!player
		//				.isSneaking()))) {
		//			remove();
		//			return false;
		//		}
		rotateShield();
		return true;
	}

	@Override
	public void reloadVariables() {
		MAX_RADIUS = config.getDouble("Abilities.Air.AirShield.Radius");
		isToggle = config.getBoolean("Abilities.Air.AirShield.IsAvatarStateToggle");
		numberOfStreams = (int) (.75 * (double) MAX_RADIUS);

		maxradius = MAX_RADIUS;
	}

	private void rotateShield() {
		Location origin = player.getLocation();

		FireBlast.removeFireBlastsAroundPoint(origin, radius);
		Combustion.removeAroundPoint(origin, radius);
		WaterManipulation.removeAroundPoint(origin, radius);
		EarthBlast.removeAroundPoint(origin, radius);

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, radius)) {
			if (GeneralMethods.isRegionProtectedFromBuild(player, "AirShield",
					entity.getLocation()))
				continue;
			if (origin.distance(entity.getLocation()) > 2) {
				double x, z, vx, vz, mag;
				double angle = 50;
				angle = Math.toRadians(angle);

				x = entity.getLocation().getX() - origin.getX();
				z = entity.getLocation().getZ() - origin.getZ();

				mag = Math.sqrt(x * x + z * z);

				vx = (x * Math.cos(angle) - z * Math.sin(angle)) / mag;
				vz = (x * Math.sin(angle) + z * Math.cos(angle)) / mag;

				Vector velocity = entity.getVelocity();
				if (AvatarState.isAvatarState(player)) {
					velocity.setX(AvatarState.getValue(vx));
					velocity.setZ(AvatarState.getValue(vz));
				} else {
					velocity.setX(vx);
					velocity.setZ(vz);
				}
				
				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}
				}

				velocity.multiply(radius / maxradius);
				GeneralMethods.setVelocity(entity, velocity);
				entity.setFallDistance(0);
			}
		}

		Set<Integer> keys = angles.keySet();
		for (int i : keys) {
			double x, y, z;
			double angle = (double) angles.get(i);
			angle = Math.toRadians(angle);

			double factor = radius / maxradius;

			y = origin.getY() + factor * (double) i;

			//double theta = Math.asin(y/radius);
			double f = Math.sqrt(1 - factor * factor * ((double) i / radius)
					* ((double) i / radius));

			x = origin.getX() + radius * Math.cos(angle) * f;
			z = origin.getZ() + radius * Math.sin(angle) * f;

			Location effect = new Location(origin.getWorld(), x, y, z);
			if (!GeneralMethods.isRegionProtectedFromBuild(player, "AirShield",
					effect)) {
				AirMethods.playAirbendingParticles(effect, 10);
				if (GeneralMethods.rand.nextInt(4) == 0) {
					AirMethods.playAirbendingSound(effect);
				}		
			}
			
//				origin.getWorld().playEffect(effect, Effect.SMOKE, 4,
//						(int) AirBlast.defaultrange);

			angles.put(i, angles.get(i) + (int) (10 * speedfactor));
		}

		if (radius < maxradius) {
			radius += .3;
		}

		if (radius > maxradius)
			radius = maxradius;

	}

	public void setMaxradius(double maxradius) {
		this.maxradius = maxradius;
	}
}