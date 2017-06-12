package com.projectkorra.projectkorra.airbending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;

public class Twister extends AirAbility implements ComboAbility {

	public static enum AbilityState {
		TWISTER_MOVING, TWISTER_STATIONARY
	}

	private long cooldown;
	private long time;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.POWER)
	private double knockback;
	private double airStreamMaxEntityHeight;
	private double airStreamEntityCarryDuration;
	@Attribute(Attribute.HEIGHT)
	private double twisterHeight;
	@Attribute(Attribute.RADIUS)
	private double twisterRadius;
	private double twisterDegreeParticles;
	private double twisterHeightParticles;
	private double twisterRemoveDelay;
	private AbilityState state;
	private Location origin;
	private Location currentLoc;
	private Location destination;
	private Vector direction;
	private ArrayList<Entity> affectedEntities;
	
	public Twister(Player player) {
		super(player);

		this.affectedEntities = new ArrayList<>();

		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		this.range = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Range");
		this.speed = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Speed");
		this.cooldown = getConfig().getLong("Abilities.Air.AirCombo.Twister.Cooldown");
		this.twisterHeight = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Height");
		this.twisterRadius = getConfig().getDouble("Abilities.Air.AirCombo.Twister.Radius");
		this.twisterDegreeParticles = getConfig().getDouble("Abilities.Air.AirCombo.Twister.DegreesPerParticle");
		this.twisterHeightParticles = getConfig().getDouble("Abilities.Air.AirCombo.Twister.HeightPerParticle");
		this.twisterRemoveDelay = getConfig().getLong("Abilities.Air.AirCombo.Twister.RemoveDelay");

		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
			this.knockback = knockback * 1.4;
			this.airStreamMaxEntityHeight = AvatarState.getValue(airStreamMaxEntityHeight);
			this.airStreamEntityCarryDuration = AvatarState.getValue(airStreamEntityCarryDuration);
		}
		
		bPlayer.addCooldown(this);
		start();
	}

	@Override
	public String getName() {
		return "Twister";
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (currentLoc != null && GeneralMethods.isRegionProtectedFromBuild(this, currentLoc)) {
			remove();
			return;
		}
		
		if (destination == null) {
			state = AbilityState.TWISTER_MOVING;
			direction = player.getEyeLocation().getDirection().clone().normalize();
			direction.setY(0);
			origin = player.getLocation().add(direction.clone().multiply(2));
			destination = player.getLocation().add(direction.clone().multiply(range));
			currentLoc = origin.clone();
		}
		if (origin.distanceSquared(currentLoc) < origin.distanceSquared(destination) && state == AbilityState.TWISTER_MOVING) {
			currentLoc.add(direction.clone().multiply(speed));
		} else if (state == AbilityState.TWISTER_MOVING) {
			state = AbilityState.TWISTER_STATIONARY;
			time = System.currentTimeMillis();
		} else if (System.currentTimeMillis() - time >= twisterRemoveDelay) {
			remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, currentLoc)) {
			remove();
			return;
		}

		Block topBlock = GeneralMethods.getTopBlock(currentLoc, 3, -3);
		if (topBlock == null) {
			remove();
			return;
		}
		currentLoc.setY(topBlock.getLocation().getY());

		double height = twisterHeight;
		double radius = twisterRadius;
		for (double y = 0; y < height; y += twisterHeightParticles) {
			double animRadius = ((radius / height) * y);
			for (double i = -180; i <= 180; i += twisterDegreeParticles) {
				Vector animDir = GeneralMethods.rotateXZ(new Vector(1, 0, 1), i);
				Location animLoc = currentLoc.clone().add(animDir.multiply(animRadius));
				animLoc.add(0, y, 0);
				playAirbendingParticles(animLoc, 1, 0, 0, 0);
			}
		}
		playAirbendingSound(currentLoc);

		for (int i = 0; i < height; i += 3) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(currentLoc.clone().add(0, i, 0), radius * 0.75)) {
				if (!affectedEntities.contains(entity) && !entity.equals(player)) {
					affectedEntities.add(entity);
				}
			}
		}

		for (Entity entity : affectedEntities) {
			Vector forceDir = GeneralMethods.getDirection(entity.getLocation(), currentLoc.clone().add(0, height, 0));
			if (entity instanceof Player) {
				if (Commands.invincible.contains(((Player) entity).getName())) {
					break;
				}
			}
			entity.setVelocity(forceDir.clone().normalize().multiply(0.3));
		}
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public Location getLocation() {
		return origin;
	}
	
	public void setLocation(Location location) {
		this.origin = location;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}
}
