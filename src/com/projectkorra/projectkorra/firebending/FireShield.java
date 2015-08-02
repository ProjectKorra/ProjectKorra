package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class FireShield extends CoreAbility {

	private static long interval = 100;
	private static long DURATION = config.get().getLong("Abilities.Fire.FireShield.Duration");
	private static double RADIUS = config.get().getDouble("Abilities.Fire.FireShield.Radius");
	private static double DISC_RADIUS = config.get().getDouble("Abilities.Fire.FireShield.DiscRadius");
	private static double fireticks = config.get().getDouble("Abilities.Fire.FireShield.FireTicks");
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
		/* Initial Checks */
		if (containsPlayer(player, FireShield.class))
			return;
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("FireShield"))
			return;
		/* End Initial Checks */
		reloadVariables();
		this.player = player;
		this.shield = shield;

		if (!player.getEyeLocation().getBlock().isLiquid()) {
			time = System.currentTimeMillis();
			starttime = time;
			//instances.put(player, this);
			putInstance(player, this);
			if (!shield)
				bPlayer.addCooldown("FireShield", GeneralMethods.getGlobalCooldown());
		}
	}

	public static String getDescription() {
		return "FireShield is a basic defensive ability. " + "Clicking with this ability selected will create a " + "small disc of fire in front of you, which will block most " + "attacks and bending. Alternatively, pressing and holding " + "sneak creates a very small shield of fire, blocking most attacks. " + "Creatures that contact this fire are ignited.";
	}

	public static boolean isWithinShield(Location loc) {
		for (Integer id : getInstances(StockAbility.FireShield).keySet()) {
			FireShield fshield = (FireShield) getInstances(StockAbility.FireShield).get(id);
			Location playerLoc = fshield.player.getLocation();

			if (fshield.shield) {
				if (!playerLoc.getWorld().equals(loc.getWorld()))
					return false;
				if (playerLoc.distance(loc) <= fshield.radius)
					return true;
			} else {
				Location tempLoc = playerLoc.clone().add(playerLoc.multiply(fshield.discradius));
				if (!tempLoc.getWorld().equals(loc.getWorld()))
					return false;
				if (tempLoc.distance(loc) <= fshield.discradius)
					return true;
			}
		}
		return false;
	}

	public static void shield(Player player) {
		new FireShield(player, true);
	}

	public double getDiscradius() {
		return discradius;
	}

	public long getDuration() {
		return duration;
	}

	public Player getPlayer() {
		return player;
	}

	public double getRadius() {
		return radius;
	}

	@Override
	public StockAbility getStockAbility() {
		return StockAbility.FireShield;
	}

	public boolean isShield() {
		return shield;
	}

	@Override
	public boolean progress() {
		if (((!player.isSneaking()) && shield) || !GeneralMethods.canBend(player.getName(), "FireShield")) {
			remove();
			return false;
		}

		if (!player.isOnline() || player.isDead()) {
			remove();
			return false;
		}

		if (System.currentTimeMillis() > starttime + duration && !shield) {
			remove();
			return false;
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
						Block block = location.clone().add(radius * Math.cos(rphi) * Math.sin(rtheta), radius * Math.cos(rtheta), radius * Math.sin(rphi) * Math.sin(rtheta)).getBlock();
						if (!blocks.contains(block) && !GeneralMethods.isSolid(block) && !block.isLiquid())
							blocks.add(block);
					}
				}

				for (Block block : blocks) {
					if (!GeneralMethods.isRegionProtectedFromBuild(player, "FireShield", block.getLocation())) {
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
					return false;
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
						entity.setFireTicks((int) (fireticks * 20));
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
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, discradius)) {
					if (entity instanceof Projectile) {
						entity.remove();
					}
				}
			}
		}
		return true;
	}

	@Override
	public void reloadVariables() {
		DURATION = config.get().getLong("Abilities.Fire.FireShield.Duration");
		RADIUS = config.get().getDouble("Abilities.Fire.FireShield.Radius");
		DISC_RADIUS = config.get().getDouble("Abilities.Fire.FireShield.DiscRadius");
		fireticks = config.get().getDouble("Abilities.Fire.FireShield.FireTicks");
		duration = DURATION;
		radius = RADIUS;
		discradius = DISC_RADIUS;
	}

	public void setDiscradius(double discradius) {
		this.discradius = discradius;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setShield(boolean shield) {
		this.shield = shield;
	}
}
