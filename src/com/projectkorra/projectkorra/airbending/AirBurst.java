package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class AirBurst extends CoreAbility {

	private static double PARTICLES_PERCENTAGE = 50;

	private static double threshold = config.get().getDouble("Abilities.Air.AirBurst.FallThreshold");
	private static double pushfactor = config.get().getDouble("Abilities.Air.AirBurst.PushFactor");
	private static double damage = config.get().getDouble("Abilities.Air.AirBurst.Damage");
	private static double deltheta = 10;
	private static double delphi = 10;

	private Player player;
	private long starttime;
	private long chargetime = config.get().getLong("Abilities.Air.AirBurst.ChargeTime");
	private boolean charged = false;
	private ArrayList<AirBlast> blasts = new ArrayList<AirBlast>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirBurst() {
		reloadVariables();
	}

	public AirBurst(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("AirBurst"))
			return;
		if (containsPlayer(player, AirBurst.class))
			return;
		/* End Initial Checks */
		reloadVariables();
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		//instances.put(player.getUniqueId(), this);
		putInstance(player, this);
	}

	public static void coneBurst(Player player) {
		if (containsPlayer(player, AirBurst.class)) {
			((AirBurst) getAbilityFromPlayer(player, AirBurst.class)).coneBurst();
		}
	}

	public static void fallBurst(Player player) {
		if (!GeneralMethods.canBend(player.getName(), "AirBurst")) {
			return;
		}
		if (player.getFallDistance() < threshold) {
			return;
		}
		if (GeneralMethods.getBoundAbility(player) == null) {
			return;
		}
		if (containsPlayer(player, AirBurst.class)) {
			return;
		}
		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirBurst")) {
			return;
		}

		Location location = player.getLocation();
		double x, y, z;
		double r = 1;
		for (double theta = 75; theta < 105; theta += deltheta) {
			double dphi = delphi / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);
				x = r * Math.cos(rphi) * Math.sin(rtheta);
				y = r * Math.sin(rphi) * Math.sin(rtheta);
				z = r * Math.cos(rtheta);
				Vector direction = new Vector(x, z, y);
				AirBlast blast = new AirBlast(location, direction.normalize(), player, pushfactor, new AirBurst());
				blast.setDamage(damage);
			}
		}
	}

	void addAffectedEntity(Entity entity) {
		affectedentities.add(entity);
	}

	private void coneBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
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
						AirBlast blast = new AirBlast(location, direction.normalize(), player, pushfactor, this);
						blast.setDamage(damage);
					}
				}
			}
		}
		remove();
	}

	@Override
	public StockAbility getStockAbility() {
		return StockAbility.AirBurst;
	}

	public void handleSmoothParticles() {
		for (int i = 0; i < blasts.size(); i++) {
			final AirBlast blast = blasts.get(i);
			int toggleTime = 0;
			if (i % 4 != 0)
				toggleTime = (int) (i % (100 / PARTICLES_PERCENTAGE)) + 3;
			new BukkitRunnable() {
				public void run() {
					blast.setShowParticles(true);
				}
			}.runTaskLater(ProjectKorra.plugin, toggleTime);
		}
	}

	boolean isAffectedEntity(Entity entity) {
		return affectedentities.contains(entity);
	}

	@Override
	public boolean progress() {
		if (!GeneralMethods.canBend(player.getName(), "AirBurst")) {
			remove();
			return false;
		}
		if (GeneralMethods.getBoundAbility(player) == null) {
			remove();
			return false;
		}

		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirBurst")) {
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
				return false;
			}
		} else if (charged) {
			Location location = player.getEyeLocation();
			// location = location.add(location.getDirection().normalize());
			AirMethods.playAirbendingParticles(location, 10);
			//			location.getWorld().playEffect(
			//					location,
			//					Effect.SMOKE,
			//					Methods.getIntCardinalDirection(player.getEyeLocation()
			//							.getDirection()), 3);
		}
		return true;
	}

	@Override
	public void reloadVariables() {
		threshold = config.get().getDouble("Abilities.Air.AirBurst.FallThreshold");
		pushfactor = config.get().getDouble("Abilities.Air.AirBurst.PushFactor");
		damage = config.get().getDouble("Abilities.Air.AirBurst.Damage");
		chargetime = config.get().getLong("Abilities.Air.AirBurst.ChargeTime");
	}

	private void sphereBurst() {
		if (charged) {
			Location location = player.getEyeLocation();
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
					AirBlast blast = new AirBlast(location, direction.normalize(), player, pushfactor, this);
					blast.setDamage(damage);
					blast.setShowParticles(false);
					blasts.add(blast);
				}
			}
		}
		// Methods.verbose("--" + AirBlast.instances.size() + "--");
		remove();
		handleSmoothParticles();
	}
}
