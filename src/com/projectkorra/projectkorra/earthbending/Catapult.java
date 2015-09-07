package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.util.Flight;

public class Catapult {

	public static ConcurrentHashMap<Integer, Catapult> instances = new ConcurrentHashMap<Integer, Catapult>();

	private static int LENGTH = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Catapult.Length");
	private static double SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Catapult.Speed");
	private static double PUSH = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Catapult.Push");

	private static long interval = (long) (1000. / SPEED);

	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private int distance;
	private boolean catapult = false;
	private boolean moving = false;
	private boolean flying = false;
	private int length = LENGTH;
	private double speed = SPEED;
	private double push = PUSH;
	private long time;
	private long starttime;
	private int ticks = 0;

	public Catapult(Player player) {
		BendingPlayer bPlayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bPlayer.isOnCooldown("Catapult"))
			return;

		this.player = player;
		origin = player.getEyeLocation().clone();
		direction = origin.getDirection().clone().normalize();
		Vector neg = direction.clone().multiply(-1);

		Block block;
		distance = 0;
		for (int i = 0; i <= length; i++) {
			location = origin.clone().add(neg.clone().multiply((double) i));
			block = location.getBlock();
			if (EarthMethods.isEarthbendable(player, block)) {
				distance = EarthMethods.getEarthbendableBlocksLength(player, block, neg, length - i);
				break;
			} else if (!EarthMethods.isTransparentToEarthbending(player, block)) {
				break;
			}
		}

		if (distance != 0) {
			if ((double) distance >= location.distance(origin)) {
				catapult = true;
			}
			if (player.isSneaking())
				distance = distance / 2;
			time = System.currentTimeMillis() - interval;
			starttime = System.currentTimeMillis();
			moving = true;
			instances.put(player.getEntityId(), this);
			bPlayer.addCooldown("Catapult", GeneralMethods.getGlobalCooldown());
		}

	}

	public Catapult(Player player, Catapult source) {
		flying = true;
		this.player = player;
		moving = false;
		location = source.location.clone();
		starttime = source.starttime;
		direction = source.direction.clone();
		distance = source.distance;
		time = source.time;
		instances.put(player.getEntityId(), this);
		EarthMethods.playEarthbendingSound(player.getLocation());
		fly();
	}

	public static void progressAll() {
		for (int ID : instances.keySet()) {
			instances.get(ID).progress();
		}
	}

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}

		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();
			if (moving)
				if (!moveEarth()) {
					moving = false;
				}
		}

		if (flying)
			fly();

		if (!flying && !moving && System.currentTimeMillis() > starttime + 1000)
			remove();
		return true;
	}

	private void fly() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		// Methods.verbose(player.getLocation().distance(location));
		if (!player.getWorld().equals(location.getWorld())) {
			remove();
			return;
		}

		if (player.getLocation().distance(location) < 3) {
			if (!moving && System.currentTimeMillis() > starttime + 1000)
				flying = false;
			return;
		}

		for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation(), 1.5)) {
			if ((GeneralMethods.isSolid(block) || block.isLiquid())) {
				// Methods.verbose("Catapulting stopped");
				flying = false;
				return;
			}
		}
		Vector vector = direction.clone().multiply(push * distance / length);
		vector.setY(player.getVelocity().getY());
		player.setVelocity(vector);
		//Methods.verbose("Fly!");
		
	}

	private void remove() {
		instances.remove(player.getEntityId());
	}

	private boolean moveEarth() {
		// Methods.verbose(distance);
		// Methods.verbose(direction);
		// Location loc = location.clone().add(direction);
		if (ticks > distance) {
			return false;
		} else {
			ticks++;
		}

		// Methods.moveEarth(player, location, direction, distance, false);
		location = location.clone().add(direction);

		if (catapult) {
			if (location.distance(origin) < .5) {
				boolean remove = false;
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, 2)) {
					if (entity instanceof Player) {
						Player target = (Player) entity;
						boolean equal = target.getEntityId() == player.getEntityId();
						if (equal) {
							remove();
							remove = true;
						}
						if (equal || target.isSneaking()) {
							new Flight(target);
							target.setAllowFlight(true);
							new Catapult(target, this);
						}
					}
					entity.setVelocity(direction.clone().multiply(push * distance / length));

				}
				return remove;
			}
		} else {
			if (location.distance(origin) <= length - distance) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					entity.setVelocity(direction.clone().multiply(push * distance / length));
				}
				return false;
			}
		}
		EarthMethods.moveEarth(player, location.clone().subtract(direction), direction, distance, false);
		return true;
	}

	public static List<Player> getPlayers() {
		List<Player> players = new ArrayList<Player>();
		for (int id : instances.keySet()) {
			Player player = instances.get(id).player;
			if (!players.contains(player))
				players.add(player);
		}
		return players;
	}

	public static void removeAll() {
		for (int id : instances.keySet()) {
			instances.remove(id);
		}
	}

	public static String getDescription() {
		return "To use, left-click while looking in the direction you want to be launched. " + "A pillar of earth will jut up from under you and launch you in that direction - " + "if and only if there is enough earth behind where you're looking to launch you. " + "Skillful use of this ability takes much time and work, and it does result in the " + "death of certain gung-ho earthbenders. If you plan to use this ability, be sure " + "you've read about your passive ability you innately have as an earthbender.";
	}

	public Player getPlayer() {
		return player;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getPush() {
		return push;
	}

	public void setPush(double push) {
		this.push = push;
	}
}
