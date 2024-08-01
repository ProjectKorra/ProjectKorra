package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import com.projectkorra.projectkorra.ability.util.ComboUtil;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.LightManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;

public class FireWheel extends FireAbility implements ComboAbility {

	private Location origin;
	private Location location;
	private Vector direction;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.HEIGHT)
	private double height;
	private double radius;
	@Attribute(Attribute.SPEED)
	private double speed;
	@Attribute(Attribute.FIRE_TICK)
	private double fireTicks;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	private ArrayList<LivingEntity> affectedEntities;

	public FireWheel(final Player player) {
		super(player);

		if (this.bPlayer.isOnCooldown("FireWheel") && !this.bPlayer.isAvatarState()) {
			this.remove();
			return;
		}

		this.damage = applyModifiersDamage(getConfig().getDouble("Abilities.Fire.FireWheel.Damage"));
		this.range = applyModifiersRange(getConfig().getDouble("Abilities.Fire.FireWheel.Range"));
		this.speed = getConfig().getDouble("Abilities.Fire.FireWheel.Speed");
		this.cooldown = applyModifiersCooldown(getConfig().getLong("Abilities.Fire.FireWheel.Cooldown"));
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireWheel.FireTicks");
		this.height = applyModifiers(getConfig().getInt("Abilities.Fire.FireWheel.Height"));

		this.bPlayer.addCooldown(this);
		this.affectedEntities = new ArrayList<LivingEntity>();

		if (GeneralMethods.getTopBlock(player.getLocation(), 3, 3) == null) {
			this.remove();
			return;
		}

		this.location = player.getLocation().clone();
		this.location.setPitch(0);
		this.direction = this.location.getDirection().clone().normalize();
		this.direction.setY(0);

		if (this.bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.Damage");
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.Range");
			this.speed = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.Speed");
			this.fireTicks = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.FireTicks");
			this.height = getConfig().getInt("Abilities.Avatar.AvatarState.Fire.FireWheel.Height");
		}

		this.radius = this.height / 2;
		this.origin = player.getLocation().clone().add(0, this.radius, 0);

		this.start();
	}

	@Override
	public Object createNewComboInstance(final Player player) {
		return new FireWheel(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return ComboUtil.generateCombinationFromList(this, ConfigManager.defaultConfig.get().getStringList("Abilities.Fire.FireWheel.Combination"));
	}

	@Override
	public void progress() {
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this) || GeneralMethods.isRegionProtectedFromBuild(this.player, this.location)) {
			this.remove();
			return;
		}
		if (this.location.distanceSquared(this.origin) > this.range * this.range) {
			this.remove();
			return;
		}

		Block topBlock = GeneralMethods.getTopBlock(this.location, (int) this.radius, (int)this.radius + 2);
		if (topBlock.getType().equals(Material.SNOW)) {
			topBlock.breakNaturally();
			topBlock = topBlock.getRelative(BlockFace.DOWN);
		}
		if (isWater(topBlock)) {
			this.remove();
			return;
		} else if (topBlock.getType() == Material.FIRE) {
			topBlock = topBlock.getRelative(BlockFace.DOWN);
		} else if (ElementalAbility.isPlant(topBlock)) {
			topBlock.breakNaturally();
			topBlock = topBlock.getRelative(BlockFace.DOWN);
		} else if (ElementalAbility.isAir(topBlock.getType())) {
			this.remove();
			return;
		} else if (GeneralMethods.isSolid(topBlock.getRelative(BlockFace.UP)) || isWater(topBlock.getRelative(BlockFace.UP))) {
			this.remove();
			return;
		}
		this.location.setY(topBlock.getY() + this.height);

		for (double i = -180; i <= 180; i += 3) {
			final Location tempLoc = this.location.clone();
			final Vector newDir = this.direction.clone().multiply(this.radius * Math.cos(Math.toRadians(i)));
			tempLoc.add(newDir);
			tempLoc.setY(tempLoc.getY() + (this.radius * Math.sin(Math.toRadians(i))));
			playFirebendingParticles(tempLoc, 0, 0, 0, 0);
			LightManager.createLight(tempLoc).brightness(13).timeUntilFadeout(600).emit();
		}

		for (final Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.radius + 0.5)) {
			if (entity instanceof LivingEntity && !entity.equals(this.player)) {
				if (!this.affectedEntities.contains(entity)) {
					this.affectedEntities.add((LivingEntity) entity);
					DamageHandler.damageEntity(entity, this.damage, this);
					entity.setFireTicks((int) (this.fireTicks * 20));
					new FireDamageTimer(entity, this.player, this);
				}
			}
		}

		this.location = this.location.add(this.direction.clone().multiply(this.speed));
		this.location.getWorld().playSound(this.location, Sound.BLOCK_FIRE_AMBIENT, 1, 1);
	}

	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public String getName() {
		return "FireWheel";
	}

	@Override
	public Location getLocation() {
		return this.location;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public ArrayList<LivingEntity> getAffectedEntities() {
		return this.affectedEntities;
	}
}
