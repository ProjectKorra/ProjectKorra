package com.projectkorra.projectkorra.firebending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingManager;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;

public class FireBurst implements ConfigLoadable {

	public static final ConcurrentHashMap<Player, FireBurst> instances = new ConcurrentHashMap<>();
	
	private static double PARTICLES_PERCENTAGE = 5;

	private Player player;
	private long starttime;
	private int damage = config.get().getInt("Abilities.Fire.FireBurst.Damage");
	private long chargetime = config.get().getLong("Abilities.Fire.FireBurst.ChargeTime");
	private long range = config.get().getLong("Abilities.Fire.FireBurst.Range");
	private double deltheta = 10;
	private double delphi = 10;
	private boolean charged = false;
	private ArrayList<FireBlast> blasts = new ArrayList<FireBlast>();

	public FireBurst(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("FireBurst"))
			return;
		if (instances.containsKey(player))
			return;
		/* End Initial Checks */
		// reloadVariables();

		starttime = System.currentTimeMillis();
		if (FireMethods.isDay(player.getWorld())) {
			chargetime /= config.get().getDouble("Properties.Fire.DayFactor");
		}
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		if (BendingManager.events.containsKey(player.getWorld())) {
			if (BendingManager.events.get(player.getWorld()).equalsIgnoreCase("SozinsComet"))
				chargetime = 0;
		}
		this.player = player;
		instances.put(player, this);
	}

	public static void coneBurst(Player player) {
		if (instances.containsKey(player)) {
			((FireBurst) instances.get(player)).coneBurst();
		}
	}

	public static String getDescription() {
		return "FireBurst is a very powerful firebending ability. " + "To use, press and hold sneak to charge your burst. "
				+ "Once charged, you can either release sneak to launch a cone-shaped burst "
				+ "of flames in front of you, or click to release the burst in a sphere around you. ";
	}

	private void coneBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = GeneralMethods.getBlocksAroundPoint(player.getLocation(), 2);
			Vector vector = location.getDirection();
			double angle = Math.toRadians(30);
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					if (direction.angle(vector) <= angle) {
						// Methods.verbose(direction.angle(vector));
						// Methods.verbose(direction);
						FireBlast fblast = new FireBlast(location, direction.normalize(), player, damage, safeblocks);
						fblast.setRange(this.range);
					}
				}
			}
		}
		// Methods.verbose("--" + AirBlast.instances.size() + "--");
		remove();
	}

	public long getChargetime() {
		return chargetime;
	}

	public int getDamage() {
		return damage;
	}

	public Player getPlayer() {
		return player;
	}

	public long getRange() {
		return range;
	}

	public void remove() {
		instances.remove(player);
	}

	public static void removeAll() {
		for (FireBurst ability : instances.values()) {
			ability.remove();
		}
	}

	/**
	 * To combat the sphere FireBurst lag we are only going to show a certain percentage of
	 * FireBurst particles at a time per tick. As the bursts spread out then we can show more at a
	 * time.
	 */
	public void handleSmoothParticles() {
		for (int i = 0; i < blasts.size(); i++) {
			final FireBlast fblast = blasts.get(i);
			int toggleTime = (int) (i % (100 / PARTICLES_PERCENTAGE));
			new BukkitRunnable() {
				public void run() {
					fblast.setShowParticles(true);
				}
			}.runTaskLater(ProjectKorra.plugin, toggleTime);
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}
		if (!GeneralMethods.canBend(player.getName(), "FireBurst")) {
			remove();
			return false;
		}
		if (GeneralMethods.getBoundAbility(player) == null) {
			remove();
			return false;
		}

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("FireBurst")) {
			remove();
			return false;
		}

		if (System.currentTimeMillis() > starttime + chargetime && !charged) {
			charged = true;
		}

		if (!player.isSneaking()) {
			if (charged) {
				sphereBurst();
			} else {
				remove();
			}
		} else if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 4, 3);
		}
		return true;
	}

	public static void progressAll() {
		for (FireBurst ability : instances.values()) {
			ability.progress();
		}
	}

	@Override
	public void reloadVariables() {
		// No need for this because there are no static variables.
		// All instance variables are gotten newly from config
	}

	public void setChargetime(long chargetime) {
		this.chargetime = chargetime;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public void setRange(long range) {
		this.range = range;
	}

	private void sphereBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
			List<Block> safeblocks = GeneralMethods.getBlocksAroundPoint(player.getLocation(), 2);
			double x, y, z;
			double r = 1;
			for (double theta = 0; theta <= 180; theta += deltheta) {
				double dphi = delphi / Math.sin(Math.toRadians(theta));
				for (double phi = 0; phi < 360; phi += dphi) {
					double rphi = Math.toRadians(phi);
					double rtheta = Math.toRadians(theta);
					x = r * Math.cos(rphi) * Math.sin(rtheta);
					y = r * Math.sin(rphi) * Math.sin(rtheta);
					z = r * Math.cos(rtheta);
					Vector direction = new Vector(x, z, y);
					FireBlast fblast = new FireBlast(location, direction.normalize(), player, damage, safeblocks);
					fblast.setRange(this.range);
					fblast.setShowParticles(false);
					blasts.add(fblast);
				}
			}
		}
		// Methods.verbose("--" + AirBlast.instances.size() + "--");
		remove();
		handleSmoothParticles();
	}
}
