package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FireWheel extends FireAbility implements ComboAbility {

	private Location origin;
	private Location location;
	private Vector direction;
	private long cooldown;
	private double range;
	private int height;
	private double radius;
	private double speed;
	private double fireTicks;
	private double damage;
	private ArrayList<LivingEntity> affectedEntities;

	public FireWheel(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown("FireWheel") && !bPlayer.isAvatarState()) {
			remove();
			return;
		}
		
		this.damage = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.Range");
		this.speed = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireCombo.FireWheel.Cooldown");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.FireTicks");
		this.height = getConfig().getInt("Abilities.Fire.FireCombo.FireWheel.Height");
		this.radius = getConfig().getDouble("Abilities.Fire.FireCombo.FireWheel.Radius");
		
		bPlayer.addCooldown(this);
		origin = player.getLocation();
		affectedEntities = new ArrayList<LivingEntity>();

		if (GeneralMethods.getTopBlock(player.getLocation(), 3, 3) == null) {
			remove();
			return;
		}

		location = player.getLocation();
		direction = player.getEyeLocation().getDirection().clone().normalize();
		direction.setY(0);
		
		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = AvatarState.getValue(damage);
			this.range = AvatarState.getValue(range);
		}
		
		start();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		return null;
	}

	@Override
	public void progress() {
		if (location.distanceSquared(origin) > range * range) {
			remove();
			return;
		}

		Block topBlock = GeneralMethods.getTopBlock(location, 2, -4);
		if (topBlock.getType().equals(Material.SNOW)) {

			topBlock = topBlock.getLocation().add(0, -1, 0).getBlock();
		}
		if (topBlock == null || (WaterAbility.isWaterbendable(player, getName(), topBlock) && !isPlant(topBlock))) {
			remove();
			return;
		} else if (topBlock.getType() == Material.FIRE || ElementalAbility.isPlant(topBlock)) {
			topBlock = topBlock.getLocation().add(0, -1, 0).getBlock();
		}
		location.setY(topBlock.getY() + height);

		for (double i = -180; i <= 180; i += 3) {
			Location tempLoc = location.clone();
			Vector newDir = direction.clone().multiply(radius * Math.cos(Math.toRadians(i)));
			tempLoc.add(newDir);
			tempLoc.setY(tempLoc.getY() + (radius * Math.sin(Math.toRadians(i))));
			ParticleEffect.FLAME.display(tempLoc, 0, 0, 0, 0, 1);
		}
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), 2)) {
			if (entity instanceof LivingEntity && !entity.equals(player)) {
				if (!affectedEntities.contains(entity)) {
					affectedEntities.add((LivingEntity) entity);
					DamageHandler.damageEntity(entity, damage, this);
					entity.setFireTicks((int) (fireTicks * 20));
					new FireDamageTimer(entity, player);
				}
			}
		}

		location = location.add(direction.clone().multiply(speed));
		location.getWorld().playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1, 1);
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public String getName() {
		return "FireWheel";
	}

	@Override
	public Location getLocation() {
		return location;
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
		return affectedEntities;
	}
}
