package com.projectkorra.projectkorra.earthbending;

import java.util.Random;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.ParticleEffect.BlockData;

public class Catapult extends EarthAbility {
	
	private int maxDistance;
	private double stageMult;
	private double stageTimeMult;
	private int distance;
	private long cooldown;
	private Location origin;
	private Location target;
	
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
			this.maxDistance = getConfig().getInt("Abilities.Avatar.AvatarState.Earth.Catapult.MaxDistance");
			this.cooldown = getConfig().getLong("Abilities.Avatar.AvatarState.Earth.Catapult.Cooldown");
		}
		start();
	}

	private void setFields() {
		this.maxDistance = getConfig().getInt("Abilities.Earth.Catapult.MaxDistance");
		this.stageMult = getConfig().getDouble("Abilities.Earth.Catapult.StageMult");
		this.stageTimeMult = getConfig().getDouble("Abilities.Earth.Catapult.StageTimeMult");
		this.distance = 0;
		this.cooldown = getConfig().getLong("Abilities.Earth.Catapult.Cooldown");
		this.activationHandled = false;
		this.stage = 1;
		this.stageStart = System.currentTimeMillis();
		this.charging = true;
	}

	private void moveEarth(Vector apply, Vector direction) {
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, 2)) {
			if (entity.getEntityId() != player.getEntityId()) {
				entity.setVelocity(apply);
			}
		}
		moveEarth(this.origin.clone().subtract(direction), direction, 3, false);
	}

	@Override
	public void progress() {
		if (player.getEyeLocation().getPitch() > -20f) {
			remove();
			return;
		}
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
				if ((System.currentTimeMillis() - this.stageStart) >= ((Math.max(0, this.stageTimeMult * (this.stage - 1))) * 1000))
				{
					this.stage++;
					this.stageStart = System.currentTimeMillis();
					Random random = new Random();
					ParticleEffect.BLOCK_DUST.display(new BlockData(Material.DIRT, (byte)0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 0, 20, player.getLocation(), 257);
					ParticleEffect.BLOCK_DUST.display(new BlockData(Material.DIRT, (byte)0), random.nextFloat(), random.nextFloat(), random.nextFloat(), 0, 20, player.getLocation().add(0, 0.5, 0), 257);
					player.getWorld().playEffect(player.getLocation(), Effect.GHAST_SHOOT, 0, 10);
				}
				return;
			}
		}
		
		Vector direction = null;
		if (!this.activationHandled)
		{
			this.origin = player.getLocation().clone();
			direction = player.getEyeLocation().getDirection().clone().normalize();

			if (!bPlayer.canBend(this)) {
				this.activationHandled = true;
				remove();
				return;
			}
			
			if (isEarthbendable(player.getLocation().getBlock()) || isEarthbendable(player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
				distance = this.maxDistance;
			}
			if (distance != 0) {
				distance = (int) (distance * (this.stageMult * this.stage));
				this.activationHandled = true;
				bPlayer.addCooldown(this);
			} else {
				remove();
				return;
			}
		}
		Location tar = this.origin.clone();
		while (tar.distanceSquared(this.origin) <= Math.pow(this.distance, 2))
		{
			tar.add(direction.clone().normalize());
		}
		this.target = tar;
		Vector apply = this.target.clone().toVector().subtract(this.origin.clone().toVector());
		player.setVelocity(apply);
		moveEarth(apply, direction);
		remove();
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
	
	public Location getTarget() {
		return target;
	}
	
	public void setTarget(Location target) {
		this.target = target;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
}