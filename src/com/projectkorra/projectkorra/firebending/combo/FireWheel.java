package com.projectkorra.projectkorra.firebending.combo;

import java.util.ArrayList;

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
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.ClickType;
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
		
		this.damage = getConfig().getDouble("Abilities.Fire.FireWheel.Damage");
		this.range = getConfig().getDouble("Abilities.Fire.FireWheel.Range");
		this.speed = getConfig().getDouble("Abilities.Fire.FireWheel.Speed");
		this.cooldown = getConfig().getLong("Abilities.Fire.FireWheel.Cooldown");
		this.fireTicks = getConfig().getDouble("Abilities.Fire.FireWheel.FireTicks");
		this.height = getConfig().getInt("Abilities.Fire.FireWheel.Height");
		this.radius = getConfig().getDouble("Abilities.Fire.FireWheel.Radius");
		
		bPlayer.addCooldown(this);
		origin = player.getLocation();
		affectedEntities = new ArrayList<LivingEntity>();

		if (GeneralMethods.getTopBlock(player.getLocation(), 3, 3) == null) {
			remove();
			return;
		}

		location = player.getLocation().clone();
		location.setPitch(0);
		direction = location.getDirection().clone().normalize();
		direction.setY(0);
		
		if (bPlayer.isAvatarState()) {
			this.cooldown = 0;
			this.damage = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.Damage");
			this.range = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.Range");
			this.speed = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.Speed");
			this.fireTicks = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.FireTicks");
			this.height = getConfig().getInt("Abilities.Avatar.AvatarState.Fire.FireWheel.Height");
			this.radius = getConfig().getDouble("Abilities.Avatar.AvatarState.Fire.FireWheel.Radius");
		}
		
		start();
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new FireWheel(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> fireWheel = new ArrayList<>();
		fireWheel.add(new AbilityInformation("FireShield", ClickType.SHIFT_DOWN));
		fireWheel.add(new AbilityInformation("FireShield", ClickType.RIGHT_CLICK_BLOCK));
		fireWheel.add(new AbilityInformation("FireShield", ClickType.RIGHT_CLICK_BLOCK));
		fireWheel.add(new AbilityInformation("Blaze", ClickType.SHIFT_UP));
		return fireWheel;
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this) || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
			remove();
			return;
		}
		if (location.distanceSquared(origin) > range * range) {
			remove();
			return;
		}

		Block topBlock = GeneralMethods.getTopBlock(location, height);
		if (topBlock.getType().equals(Material.SNOW)) {
			topBlock.breakNaturally();
			topBlock = topBlock.getRelative(BlockFace.DOWN);
		}
		if (topBlock == null || isWater(topBlock)) {
			remove();
			return;
		} else if (topBlock.getType() == Material.FIRE) {
			topBlock = topBlock.getRelative(BlockFace.DOWN);
		}  else if (topBlock.getType() == Material.AIR) {
			remove();
			return;
		}
		location.setY(topBlock.getY() + height);

		for (double i = -180; i <= 180; i += 3) {
			Location tempLoc = location.clone();
			Vector newDir = direction.clone().multiply(radius * Math.cos(Math.toRadians(i)));
			tempLoc.add(newDir);
			tempLoc.setY(tempLoc.getY() + (radius * Math.sin(Math.toRadians(i))));
			ParticleEffect.FLAME.display(tempLoc, 0, 0, 0, 0, 1);
		}
		
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 1.5)) {
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
	
	@Override
	public String getInstructions() {
		return "FireShield (Hold Shift) > Right Click a block in front of you twice > Switch to Blaze > Release Shift";
	}
}