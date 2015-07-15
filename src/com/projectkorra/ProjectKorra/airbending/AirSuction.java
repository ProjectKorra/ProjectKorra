package com.projectkorra.ProjectKorra.airbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.ProjectKorra.BendingPlayer;
import com.projectkorra.ProjectKorra.Commands;
import com.projectkorra.ProjectKorra.Flight;
import com.projectkorra.ProjectKorra.GeneralMethods;
import com.projectkorra.ProjectKorra.ProjectKorra;
import com.projectkorra.ProjectKorra.Ability.AvatarState;
import com.projectkorra.ProjectKorra.Ability.BaseAbility;
import com.projectkorra.ProjectKorra.Ability.StockAbilities;
import com.projectkorra.ProjectKorra.earthbending.EarthMethods;
import com.projectkorra.ProjectKorra.waterbending.WaterSpout;

public class AirSuction extends BaseAbility {
	
	private static ConcurrentHashMap<Player, Location> origins = new ConcurrentHashMap<Player, Location>();

	static final double maxspeed = AirBlast.maxspeed;
	
	private static final int maxticks = AirBlast.maxticks;

	//private static long soonesttime = config.getLong("Properties.GlobalCooldown");
	private static double SPEED = config.getDouble("Abilities.Air.AirSuction.Speed");
	private static double RANGE = config.getDouble("Abilities.Air.AirSuction.Range");
	private static double RADIUS = config.getDouble("Abilities.Air.AirSuction.Radius");
	private static double PUSH_FACTOR = config.getDouble("Abilities.Air.AirSuction.Push");
	private static double originselectrange = 10;

	private Location location;
	private Location origin;
	private Vector direction;
	private Player player;
	private boolean otherorigin = false;
	private int ticks = 0;
	private double speed = SPEED;
	private double range = RANGE;
	private double affectingradius = RADIUS;
	private double pushfactor = PUSH_FACTOR;
	// private long time;

	private double speedfactor;

	@SuppressWarnings("unused")
	private ArrayList<Entity> affectedentities = new ArrayList<Entity>();

	public AirSuction(Player player) {
		/* Initial Check */
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("AirSuction")) return;
		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		if (AirSpout.getPlayers().contains(player.getUniqueId())
				|| WaterSpout.getPlayers().contains(player)) //TODO: UPDATE THIS LINE
			return;
		/* End Initial Check */
		reloadVariables();
		this.player = player;
		if (origins.containsKey(player)) {
			origin = origins.get(player);
			otherorigin = true;
			origins.remove(player);
		} else {
			origin = player.getEyeLocation();
		}
		location = GeneralMethods.getTargetedLocation(player, range, GeneralMethods.nonOpaque);
		direction = GeneralMethods.getDirection(location, origin).normalize();
		Entity entity = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
		if (entity != null) {
			direction = GeneralMethods.getDirection(entity.getLocation(), origin)
					.normalize();
			location = getLocation(origin, direction.clone().multiply(-1));
			// location =
			// origin.clone().add(direction.clone().multiply(-range));
		}
		// }

		//instances.put(uuid, this);
		putInstance(player, this);
		bPlayer.addCooldown("AirSuction", GeneralMethods.getGlobalCooldown());
		// time = System.currentTimeMillis();
		// timers.put(player, System.currentTimeMillis());
	}

	public static String getDescription() {
		return "To use, simply left-click in a direction. "
				+ "A gust of wind will originate as far as it can in that direction"
				+ " and flow towards you, sucking anything in its path harmlessly with it."
				+ " Skilled benders can use this technique to pull items from precarious locations. "
				+ "Additionally, tapping sneak will change the origin of your next "
				+ "AirSuction to your targeted location.";
	}

	private static void playOriginEffect(Player player) {
		if (!origins.containsKey(player))
			return;
		Location origin = origins.get(player);
		if (!origin.getWorld().equals(player.getWorld())) {
			origins.remove(player);
			return;
		}

		if (GeneralMethods.getBoundAbility(player) == null) {
			origins.remove(player);
			return;
		}
		
		if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirSuction") || !GeneralMethods.canBend(player.getName(), "AirSuction")) {
			origins.remove(player);
			return;
		}

		if (origin.distance(player.getEyeLocation()) > originselectrange) {
			origins.remove(player);
			return;
		}
		
		AirMethods.playAirbendingParticles(origin, 10);
//
//		origin.getWorld().playEffect(origin, Effect.SMOKE, 4,
//				(int) originselectrange);
	}

	public static void progressAll() {
		BaseAbility.progressAll(StockAbilities.AirSuction);
		for (Player player : origins.keySet()) {
			playOriginEffect(player);
		}
	}

	public static void setOrigin(Player player) {
		Location location = GeneralMethods.getTargetedLocation(player,
				originselectrange, GeneralMethods.nonOpaque);
		if (location.getBlock().isLiquid()
				|| GeneralMethods.isSolid(location.getBlock()))
			return;

		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction",
				location))
			return;

		if (origins.containsKey(player)) {
			origins.replace(player, location);
		} else {
			origins.put(player, location);
		}
	}

	private void advanceLocation() {
		AirMethods.playAirbendingParticles(location, 10);
		if (GeneralMethods.rand.nextInt(4) == 0) {
			AirMethods.playAirbendingSound(location);
		}
//		location.getWorld().playEffect(location, Effect.SMOKE, 4,
//				(int) AirBlast.defaultrange);
		location = location.add(direction.clone().multiply(speedfactor));
	}

	public double getAffectingradius() {
		return affectingradius;
	}

	private Location getLocation(Location origin, Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if (!EarthMethods.isTransparentToEarthbending(player, location.getBlock()) || GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction", location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}

	public Player getPlayer() {
		return player;
	}

	public double getPushfactor() {
		return pushfactor;
	}

	public double getRange() {
		return range;
	}

	public double getSpeed() {
		return speed;
	}

	@Override
	public StockAbilities getStockAbility() {
		return StockAbilities.AirSuction;
	}

	@Override
	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction",
				location)) {
			remove();
			return false;
		}
		speedfactor = speed * (ProjectKorra.time_step / 1000.);

		ticks++;

		if (ticks > maxticks) {
			remove();
			return false;
		}
		// if (player.isSneaking()
		// && Methods.getBendingAbility(player) == Abilities.AirSuction) {
		// new AirSuction(player);
		// }

		if ((location.distance(origin) > range)
				|| (location.distance(origin) <= 1)) {
			remove();
			return false;
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location,
				affectingradius)) {
			// if (affectedentities.contains(entity))
			// continue;
			// affectedentities.add(entity);
			if (entity.getEntityId() != player.getEntityId() || otherorigin) {
				Vector velocity = entity.getVelocity();
				double max = maxspeed;
				double factor = pushfactor;
				if (AvatarState.isAvatarState(player)) {
					max = AvatarState.getValue(maxspeed);
					factor = AvatarState.getValue(factor);
				}

				Vector push = direction.clone();
				if (Math.abs(push.getY()) > max
						&& entity.getEntityId() != player.getEntityId()) {
					if (push.getY() < 0)
						push.setY(-max);
					else
						push.setY(max);
				}

				factor *= 1 - location.distance(origin) / (2 * range);

				double comp = velocity.dot(push.clone().normalize());
				if (comp > factor) {
					velocity.multiply(.5);
					velocity.add(push
							.clone()
							.normalize()
							.multiply(
									velocity.clone().dot(
											push.clone().normalize())));
				} else if (comp + factor * .5 > factor) {
					velocity.add(push.clone().multiply(factor - comp));
				} else {
					velocity.add(push.clone().multiply(factor * .5));
				}
				
				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName())) continue;
				}
				GeneralMethods.setVelocity(entity, velocity);
				entity.setFallDistance(0);
				if (entity.getEntityId() != player.getEntityId()
						&& entity instanceof Player) {
					new Flight((Player) entity, player);
				}
				if (entity.getFireTicks() > 0)
					entity.getWorld().playEffect(entity.getLocation(),
							Effect.EXTINGUISH, 0);
				entity.setFireTicks(0);
				AirMethods.breakBreathbendingHold(entity);

			}
		}

		advanceLocation();
		return true;
	}

	@Override
	public void reloadVariables() {
		SPEED = config.getDouble("Abilities.Air.AirSuction.Speed");
		RANGE = config.getDouble("Abilities.Air.AirSuction.Range");
		RADIUS = config.getDouble("Abilities.Air.AirSuction.Radius");
		PUSH_FACTOR = config.getDouble("Abilities.Air.AirSuction.Push");
		speed = SPEED;
		range = RANGE;
		affectingradius = RADIUS;
		pushfactor = PUSH_FACTOR;
	}

	public void setAffectingradius(double affectingradius) {
		this.affectingradius = affectingradius;
	}

	public void setPushfactor(double pushfactor) {
		this.pushfactor = pushfactor;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

}