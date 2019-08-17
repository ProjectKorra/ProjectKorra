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
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.configuration.configs.abilities.air.TwisterConfig;
import com.projectkorra.projectkorra.util.ClickType;

public class Twister extends AirAbility<TwisterConfig> implements ComboAbility {

	public static enum AbilityState {
		TWISTER_MOVING, TWISTER_STATIONARY
	}

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	private long time;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.RANGE)
	private double range;
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

	public Twister(final TwisterConfig config, final Player player) {
		super(config, player);

		this.affectedEntities = new ArrayList<>();

		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (this.bPlayer.isOnCooldown(this)) {
			return;
		}

		this.range = config.Range;
		this.speed = config.Speed;
		this.cooldown = config.Cooldown;
		this.twisterHeight = config.Height;
		this.twisterRadius = config.Radius;
		this.twisterDegreeParticles = config.DegreesPerParticle;
		this.twisterHeightParticles = config.HeightPerParticle;
		this.twisterRemoveDelay = config.RemoveDelay;

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = config.AvatarState_Damage;
			this.range = config.AvatarState_Range;
		}

		this.bPlayer.addCooldown(this);
		this.start();
	}

	@Override
	public String getName() {
		return "Twister";
	}

	@Override
	public void progress() {
		if (this.player.isDead() || !this.player.isOnline()) {
			this.remove();
			return;
		} else if (this.currentLoc != null && GeneralMethods.isRegionProtectedFromBuild(this, this.currentLoc)) {
			this.remove();
			return;
		}

		if (this.destination == null) {
			this.state = AbilityState.TWISTER_MOVING;
			this.direction = this.player.getEyeLocation().getDirection().clone().normalize();
			this.direction.setY(0);
			this.origin = this.player.getLocation().add(this.direction.clone().multiply(2));
			this.destination = this.player.getLocation().add(this.direction.clone().multiply(this.range));
			this.currentLoc = this.origin.clone();
		}
		if (this.origin.distanceSquared(this.currentLoc) < this.origin.distanceSquared(this.destination) && this.state == AbilityState.TWISTER_MOVING) {
			this.currentLoc.add(this.direction.clone().multiply(this.speed));
		} else if (this.state == AbilityState.TWISTER_MOVING) {
			this.state = AbilityState.TWISTER_STATIONARY;
			this.time = System.currentTimeMillis();
		} else if (System.currentTimeMillis() - this.time >= this.twisterRemoveDelay) {
			this.remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, this.currentLoc)) {
			this.remove();
			return;
		}

		final Block topBlock = GeneralMethods.getTopBlock(this.currentLoc, 3, -3);
		if (topBlock == null) {
			this.remove();
			return;
		}
		this.currentLoc.setY(topBlock.getLocation().getY());

		final double height = this.twisterHeight;
		final double radius = this.twisterRadius;
		for (double y = 0; y < height; y += this.twisterHeightParticles) {
			final double animRadius = ((radius / height) * y);
			for (double i = -180; i <= 180; i += this.twisterDegreeParticles) {
				final Vector animDir = GeneralMethods.rotateXZ(new Vector(1, 0, 1), i);
				final Location animLoc = this.currentLoc.clone().add(animDir.multiply(animRadius));
				animLoc.add(0, y, 0);
				playAirbendingParticles(animLoc, 1, 0, 0, 0);
			}
		}
		playAirbendingSound(this.currentLoc);

		for (int i = 0; i < height; i += 3) {
			for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.currentLoc.clone().add(0, i, 0), radius * 0.75)) {
				if (!this.affectedEntities.contains(entity) && !entity.equals(this.player)) {
					this.affectedEntities.add(entity);
				}
			}
		}

		for (final Entity entity : this.affectedEntities) {
			if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) || ((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
				continue;
			}
			final Vector forceDir = GeneralMethods.getDirection(entity.getLocation(), this.currentLoc.clone().add(0, height, 0));
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
		return this.cooldown;
	}

	public void setCooldown(final long cooldown) {
		this.cooldown = cooldown;
	}

	@Override
	public Location getLocation() {
		return this.origin;
	}

	public void setLocation(final Location location) {
		this.origin = location;
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new Twister(ConfigManager.getConfig(TwisterConfig.class), player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		final ArrayList<AbilityInformation> twister = new ArrayList<>();
		twister.add(new AbilityInformation("AirShield", ClickType.SHIFT_DOWN));
		twister.add(new AbilityInformation("AirShield", ClickType.SHIFT_UP));
		twister.add(new AbilityInformation("Tornado", ClickType.SHIFT_DOWN));
		twister.add(new AbilityInformation("AirBlast", ClickType.LEFT_CLICK));
		return twister;
	}
	
	@Override
	public Class<TwisterConfig> getConfigType() {
		return TwisterConfig.class;
	}
}
