package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.BaseAbility;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;

public class AirBurst extends BaseAbility {
	
	private static double PARTICLES_PERCENTAGE = 50;
	
	private static double threshold = config.getDouble("Abilities.Air.AirBurst.FallThreshold");
	private static double pushfactor = config.getDouble("Abilities.Air.AirBurst.PushFactor");
	private static double damage = config.getDouble("Abilities.Air.AirBurst.Damage");
	private static double deltheta = 10;
	private static double delphi = 10;

	private Player player;
	private UUID uuid;
	private long starttime;
	private long chargetime = config.getLong("Abilities.Air.AirBurst.ChargeTime");
	private boolean charged = false;
	public ArrayList<AirBlast> blasts = new ArrayList<AirBlast>();
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();
	
	public AirBurst() {
		reloadVariables();
	}

	public AirBurst(Player player) {
		/* Initial Checks */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("AirBurst")) 
			return;
		if (getInstance(StockAbilities.AirBurst).containsKey(player.getUniqueId()))
			return;
		/* End Initial Checks */
		reloadVariables();
		starttime = System.currentTimeMillis();
		if (AvatarState.isAvatarState(player))
			chargetime = 0;
		this.player = player;
		this.uuid = player.getUniqueId();
		//instances.put(player.getUniqueId(), this);
		putInstance(StockAbilities.AirBurst, uuid, this);
	}

	public static void coneBurst(Player player) {
		if (getInstance(StockAbilities.AirBurst).containsKey(player.getUniqueId()))
			((AirBurst) getInstance(StockAbilities.AirBurst).get(player.getUniqueId())).coneBurst();
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
		if (getInstance(StockAbilities.AirBurst).containsKey(player.getUniqueId())) {
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
				AirBlast blast = new AirBlast(location, direction.normalize(), player,
						pushfactor, new AirBurst());
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
						AirBlast blast = new AirBlast(location, direction.normalize(), player,
								pushfactor, this);
						blast.setDamage(damage);
					}
				}
			}
		}
		remove();
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
	public void progress() {
		if (!GeneralMethods.canBend(player.getName(), "AirBurst")) {
			remove();
		}
		if (GeneralMethods.getBoundAbility(player) == null) {
			remove();
		}
		
		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirBurst")) {
			remove();
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
			AirMethods.playAirbendingParticles(location, 10);
//			location.getWorld().playEffect(
//					location,
//					Effect.SMOKE,
//					Methods.getIntCardinalDirection(player.getEyeLocation()
//							.getDirection()), 3);
		}
	}

	@Override
	public void reloadVariables() {
		threshold = config.getDouble("Abilities.Air.AirBurst.FallThreshold");
		pushfactor = config.getDouble("Abilities.Air.AirBurst.PushFactor");
		damage = config.getDouble("Abilities.Air.AirBurst.Damage");
		chargetime = config.getLong("Abilities.Air.AirBurst.ChargeTime");
	}

	@Override
	public void remove() {
		//instances.remove(uuid);
		removeInstance(StockAbilities.AirBurst, uuid);
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
					AirBlast blast = new AirBlast(location, direction.normalize(), player,
							pushfactor, this);
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