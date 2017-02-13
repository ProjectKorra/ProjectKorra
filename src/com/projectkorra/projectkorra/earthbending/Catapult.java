package com.projectkorra.projectkorra.earthbending;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;

public class Catapult extends EarthAbility {

	private boolean catapult;
	private boolean moving;
	private boolean flying;
	private int length;
	private int distance;
	private long cooldown;
	private double push;
	//private double shiftModifier;
	private Location origin;
	private Location location;
	private Vector direction;
	
	private int stage;
	private long stageStart;
	private boolean charging;
	private boolean activationHandled;

	public Catapult(Player player) {
		super(player);
		setFields();
		if (!bPlayer.canBend(this)) {
			return;
		}
		if (bPlayer.isAvatarState()) {
			this.length = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.Catapult.Length");
			this.push = getConfig().getDouble("Abilities.Avatar.AvatarState.Earth.Catapult.Push");
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.Catapult.Cooldown");
		}
		start();
	}

	public Catapult(Player player, Catapult source) {
		super(player);
		setFields();
		this.charging = false;
		this.activationHandled = true;
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
		//this.shiftModifier = getConfig().getDouble("Abilities.Earth.Catapult.ShiftModifier");
		this.distance = 0;
		this.cooldown = getConfig().getLong("Abilities.Earth.Catapult.Cooldown");
		this.catapult = false;
		this.moving = false;
		this.flying = false;
		this.activationHandled = false;
		this.stage = 1;
		this.stageStart = System.currentTimeMillis();
		this.charging = true;
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
			if (location.getWorld().equals(origin.getWorld()) && location.distance(origin) < 0.5) {
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
		
		if (charging)
		{
			if (stage == 4 || !player.isSneaking())
			{
				charging = false;
			}
			else
			{
				if ((System.currentTimeMillis() - this.stageStart) >= ((Math.max(0, 2 * (this.stage - 1))) * 1000))
				{
					this.stage++;
					this.stageStart = System.currentTimeMillis();
					Random random = new Random();
					ParticleEffect.BLOCK_DUST.display(new BlockData(Material.DIRT, (byte)0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 0, 15, player.getLocation(), 257);
					player.getWorld().playEffect(player.getLocation(), Effect.GHAST_SHOOT, 0, 10);
				}
				return;
			}
		}
		
		if (!this.activationHandled)
		{
			this.origin = player.getEyeLocation().clone();
			this.direction = origin.getDirection().clone().normalize();

			if (!bPlayer.canBend(this)) {
				this.activationHandled = true;
				remove();
				return;
			}

			Vector neg = direction.clone().multiply(-1);
			Block block;

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
				
				distance = (int) (distance * (0.25 * this.stage));
				
				moving = true;
				
				this.activationHandled = true;

				bPlayer.addCooldown(this);
			}
			else
			{
				this.activationHandled = true;
				remove();
				return;
			}
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