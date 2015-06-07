package com.projectkorra.ProjectKorra.firebending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Utilities.ParticleEffect;
import com.projectkorra.ProjectKorra.earthbending.EarthBlast;
import com.projectkorra.ProjectKorra.waterbending.WaterManipulation;

public class FireShield {

	public static ConcurrentHashMap<Player, FireShield> instances = new ConcurrentHashMap<Player, FireShield>();

	private static long interval = 100;
	private static long DURATION = ProjectKorra.plugin.getConfig().getLong("Abilities.Fire.FireShield.Duration");
	private static double RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireShield.Radius");
	private static double DISC_RADIUS = ProjectKorra.plugin.getConfig().getDouble("Abilities.Fire.FireShield.DiscRadius");
	private static boolean ignite = true;

	private Player player;
	private long time;
	private long starttime;
	private boolean shield = false;
	private long duration = DURATION;
	private double radius = RADIUS;
	private double discradius = DISC_RADIUS;

	public FireShield(Player player) {
		this(player, false);
	}

	public FireShield(Player player, boolean shield) {
		this.player = player;
		this.shield = shield;
		if (instances.containsKey(player))
			return;
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());

		if (bPlayer.isOnCooldown("FireShield")) return;

		if (!player.getEyeLocation().getBlock().isLiquid()) {
			time = System.currentTimeMillis();
			starttime = time;
			instances.put(player, this);
			if (!shield)
				bPlayer.addCooldown("FireShield", GeneralMethods.getGlobalCooldown());
		}
	}

	public static void shield(Player player) {
		new FireShield(player, true);
	}

	private void remove() {
		instances.remove(player);
	}

	private void progress() {
		if (((!player.isSneaking()) && shield) || !GeneralMethods.canBend(player.getName(), "FireShield")) {
			remove();
			return;
		}

		if (!player.isOnline() || player.isDead()) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > starttime + duration && !shield) {
			remove();
			return;
		}

		if (System.currentTimeMillis() > time + interval) {
			time = System.currentTimeMillis();

			if (shield) {

				ArrayList<Block> blocks = new ArrayList<Block>();
				Location location = player.getEyeLocation().clone();

				for (double theta = 0; theta < 180; theta += 20) {
					for (double phi = 0; phi < 360; phi += 20) {
						double rphi = Math.toRadians(phi);
						double rtheta = Math.toRadians(theta);
						Block block = location.clone().add(
								radius * Math.cos(rphi) * Math.sin(rtheta),
								radius * Math.cos(rtheta),
								radius * Math.sin(rphi)	* Math.sin(rtheta)).getBlock();
						if (!blocks.contains(block) && !GeneralMethods.isSolid(block) && !block.isLiquid())
							blocks.add(block);
					}
				}

				for (Block block : blocks) {
					if (!GeneralMethods.isRegionProtectedFromBuild(player,	"FireShield", block.getLocation())) {
						ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 10);
						ParticleEffect.SMOKE.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 10);
						if (GeneralMethods.rand.nextInt(7) == 0) {
							FireMethods.playFirebendingSound(block.getLocation());
						}
					}
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
					if (GeneralMethods.isRegionProtectedFromBuild(player, "FireShield", entity.getLocation()))
						continue;
					if (player.getEntityId() != entity.getEntityId() && ignite) {
						entity.setFireTicks(120);
						new Enflamed(entity, player);
					}
				}

				FireBlast.removeFireBlastsAroundPoint(location, radius);
				// WaterManipulation.removeAroundPoint(location, radius);
				// EarthBlast.removeAroundPoint(location, radius);
				// FireStream.removeAroundPoint(location, radius);

			} else {

				ArrayList<Block> blocks = new ArrayList<Block>();
				Location location = player.getEyeLocation().clone();
				Vector direction = location.getDirection();
				location = location.clone().add(direction.multiply(radius));

				if (GeneralMethods.isRegionProtectedFromBuild(player, "FireShield", location)) {
					remove();
					return;
				}

				for (double theta = 0; theta < 360; theta += 20) {
					Vector vector = GeneralMethods.getOrthogonalVector(direction, theta, discradius);
					Block block = location.clone().add(vector).getBlock();
					if (!blocks.contains(block) && !GeneralMethods.isSolid(block) && !block.isLiquid())
						blocks.add(block);
				}

				for (Block block : blocks) {
					if (!GeneralMethods.isRegionProtectedFromBuild(player, "FireShield", block.getLocation())) {
						ParticleEffect.FLAME.display(block.getLocation(), 0.6F, 0.6F, 0.6F, 0, 20);
						if (GeneralMethods.rand.nextInt(4) == 0) {
							FireMethods.playFirebendingSound(block.getLocation());
						}
					}
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, discradius)) {
					if (GeneralMethods.isRegionProtectedFromBuild(player, "FireShield", entity.getLocation()))
						continue;
					if (player.getEntityId() != entity.getEntityId() && ignite) {
						entity.setFireTicks(120);
						if (!(entity instanceof LivingEntity)) {
							entity.remove();
						}
					}
				}

				FireBlast.removeFireBlastsAroundPoint(location, discradius);
				WaterManipulation.removeAroundPoint(location, discradius);
				EarthBlast.removeAroundPoint(location, discradius);
				FireStream.removeAroundPoint(location, discradius);
				Combustion.removeAroundPoint(location, discradius);
				for (Entity entity: GeneralMethods.getEntitiesAroundPoint(location, discradius)) {
					if (entity instanceof Projectile) {
						entity.remove();
					}
				}
			}
		}
	}

	public static boolean isWithinShield(Location loc) {
		for (Player player : instances.keySet()){
			FireShield fshield = instances.get(player);
			Location playerLoc = fshield.player.getLocation();

			if(fshield.shield) {
				if (!playerLoc.getWorld().equals(loc.getWorld()))
					return false;
				if(playerLoc.distance(loc) <= fshield.radius)
					return true;
			} else {
				Location tempLoc = playerLoc.clone().add(playerLoc.multiply(fshield.discradius));
				if (!tempLoc.getWorld().equals(loc.getWorld())) 
					return false;
				if(tempLoc.distance(loc) <= fshield.discradius)
					return true;
			}
		}
		return false;
	}

	public static void progressAll() {
		for (Player player : instances.keySet())
			instances.get(player).progress();
	}

	public static String getDescription() {
		return "FireShield is a basic defensive ability. "
				+ "Clicking with this ability selected will create a "
				+ "small disc of fire in front of you, which will block most "
				+ "attacks and bending. Alternatively, pressing and holding "
				+ "sneak creates a very small shield of fire, blocking most attacks. "
				+ "Creatures that contact this fire are ignited.";
	}

	public static void removeAll() {
		instances.clear();
	}

	public Player getPlayer() {
		return player;
	}

	public boolean isShield() {
		return shield;
	}

	public void setShield(boolean shield) {
		this.shield = shield;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getDiscradius() {
		return discradius;
	}

	public void setDiscradius(double discradius) {
		this.discradius = discradius;
	}
}
