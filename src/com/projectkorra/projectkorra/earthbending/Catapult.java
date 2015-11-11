package com.projectkorra.projectkorra.earthbending;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;

public class Catapult implements ConfigLoadable {

	public static final ConcurrentHashMap<Player, Catapult> instances = new ConcurrentHashMap<>();

	private static int LENGTH = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Catapult.Length");
	private static double SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Catapult.Speed");
	private static double PUSH = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Catapult.Push");

	private int length = LENGTH;
	private double speed = SPEED;
	private double push = PUSH;
	private Player player;
	private Location origin;
	private Location location;
	private Vector direction;
	private int distance;
	private boolean catapult = false;
	private boolean moving = false;
	private boolean flying = false;

	public Catapult(Player player) {
		/* Initial Checks */
		BendingPlayer bplayer = GeneralMethods.getBendingPlayer(player.getName());
		if (bplayer.isOnCooldown("Catapult"))
			return;
		/* End Initial Checks */
		// reloadVariables();
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

			moving = true;
			instances.put(player, this);
			bplayer.addCooldown("Catapult", GeneralMethods.getGlobalCooldown());
		}

	}

	public Catapult(Player player, Catapult source) {
		this.player = player;
		// reloadVariables();
		flying = true;
		moving = false;

		location = source.location.clone();
		direction = source.direction.clone();
		distance = source.distance;

		instances.put(player, this);
		EarthMethods.playEarthbendingSound(player.getLocation());
		fly();
	}

	public static String getDescription() {
		return "To use, left-click while looking in the direction you want to be launched. "
				+ "A pillar of earth will jut up from under you and launch you in that direction - "
				+ "if and only if there is enough earth behind where you're looking to launch you. "
				+ "Skillful use of this ability takes much time and work, and it does result in the "
				+ "death of certain gung-ho earthbenders. If you plan to use this ability, be sure "
				+ "you've read about your passive ability you innately have as an earthbender.";
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Catapult cata : instances.values()) {
			players.add(cata.getPlayer());
		}
		return players;
	}

	private void fly() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}

		if (!player.getWorld().equals(location.getWorld())) {
			remove();
			return;
		}

		if (player.getLocation().distance(location) < 3) {
			if (!moving)
				flying = false;
			return;
		}

		for (Block block : GeneralMethods.getBlocksAroundPoint(player.getLocation(), 1.5)) {
			if ((GeneralMethods.isSolid(block) || block.isLiquid())) {
				flying = false;
				return;
			}
		}
		Vector vector = direction.clone().multiply(push * distance / length);
		vector.setY(player.getVelocity().getY());
		player.setVelocity(vector);
	}

	public int getLength() {
		return length;
	}

	public Player getPlayer() {
		return player;
	}

	public double getPush() {
		return push;
	}

	public double getSpeed() {
		return speed;
	}

	private boolean moveEarth() {
		location = location.clone().add(direction);
		if (catapult) {
			if (location.distance(origin) < .5) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, 2)) {
					if (entity instanceof Player) {
						Player target = (Player) entity;
						new Catapult(target, this);
					}
					entity.setVelocity(direction.clone().multiply(push * distance / length));
				}
				return false;
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

	public boolean progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return false;
		}

		if (moving)
			if (!moveEarth()) {
				moving = false;
			}

		if (flying)
			fly();

		if (!flying && !moving)
			remove();
		return true;
	}
	
	public static void progressAll() {
		for (Catapult ability : instances.values()) {
			ability.progress();
		}
	}
	
	public static void removeAll() {
		for (Catapult ability : instances.values()) {
			ability.remove();
		}
	}

	@Override
	public void reloadVariables() {
		LENGTH = ProjectKorra.plugin.getConfig().getInt("Abilities.Earth.Catapult.Length");
		SPEED = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Catapult.Speed");
		PUSH = ProjectKorra.plugin.getConfig().getDouble("Abilities.Earth.Catapult.Push");
	}

	public void remove() {
		instances.remove(player);
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setPush(double push) {
		this.push = push;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

}
