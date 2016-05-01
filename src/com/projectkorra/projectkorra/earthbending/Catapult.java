package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class Catapult extends EarthAbility {

	private boolean catapult;
	private boolean moving;
	private boolean flying;
	private int length;
	private int distance;
	private long cooldown;
	private double push;
	private double shiftModifier;
	private Location origin;
	private Location location;
	private Vector direction;
	
	public Catapult(Player player) {
		super(player);
		setFields();
		this.origin = player.getEyeLocation().clone();
		this.direction = origin.getDirection().clone().normalize();
		
		if (!bPlayer.canBend(this)) {
			return;
		}

		Vector neg = direction.clone().multiply(-1);
		Block block;
		distance = 0;
		
		for (int i = 0; i <= length; i++) {
			location = origin.clone().add(neg.clone().multiply((double) i));
			block = location.getBlock();
			if (isEarthbendable(block)) {
				distance = getEarthbendableBlocksLength(block, neg, length - i);
				break;
			} else if (!isTransparent(block)) {
				break;
			}
		}

		if (distance != 0) {
			if ((double) distance * distance >= location.distanceSquared(origin)) {
				catapult = true;
			}
			if (player.isSneaking()) {
				distance = (int) (distance / shiftModifier);
			}

			moving = true;
			start();
			bPlayer.addCooldown(this);
		}
	}

	public Catapult(Player player, Catapult source) {
		super(player);
		flying = true;
		moving = false;
		setFields();
		location = source.location.clone();
		direction = source.direction.clone();
		distance = source.distance;

		start();
		playEarthbendingSound(player.getLocation());
		fly();
	}
	
	private void setFields() {
		this.length = getConfig().getInt("Abilities.Earth.Catapult.Length");
		this.push = getConfig().getDouble("Abilities.Earth.Catapult.Push");
		this.shiftModifier = getConfig().getDouble("Abilities.Earth.Catapult.ShiftModifier");
		this.distance = 0;
		this.cooldown = getConfig().getLong("Abilities.Earth.Catapult.Cooldown");
		this.catapult = false;
		this.moving = false;
		this.flying = false;
	}

	private void fly() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (!player.getWorld().equals(location.getWorld())) {
			remove();
			return;
		} else if (player.getLocation().distanceSquared(location) < 9) {
			if (!moving) {
				flying = false;
			}
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

	private boolean moveEarth() {
		location = location.clone().add(direction);
		if (catapult) {
			if (location.distance(origin) < 0.5) {
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
			double lengthSquared = (length - distance) * (length - distance);
			if (location.distanceSquared(origin) <= lengthSquared) {
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					entity.setVelocity(direction.clone().multiply(push * distance / length));
				}
				return false;
			}
		}
		moveEarth(location.clone().subtract(direction), direction, distance, false);
		return true;
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		if (moving) {
			if (!moveEarth()) {
				moving = false;
			}
		}

		if (flying) {
			fly();
		} else if (!moving) {
			remove();
			return;
		}
	}

	@Override
	public String getName() {
		return "Catapult";
	}

	@Override
	public Location getLocation() {
		if (player != null) {
			return player.getLocation();
		}
		return null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public boolean isCatapult() {
		return catapult;
	}

	public void setCatapult(boolean catapult) {
		this.catapult = catapult;
	}

	public boolean isMoving() {
		return moving;
	}

	public void setMoving(boolean moving) {
		this.moving = moving;
	}

	public boolean isFlying() {
		return flying;
	}

	public void setFlying(boolean flying) {
		this.flying = flying;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public double getPush() {
		return push;
	}

	public void setPush(double push) {
		this.push = push;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
