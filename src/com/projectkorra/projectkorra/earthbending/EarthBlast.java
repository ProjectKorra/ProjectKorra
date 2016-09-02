package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.firebending.Combustion;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class EarthBlast extends EarthAbility {

	private boolean isProgressing;
	private boolean isAtDestination;
	private boolean isSettingUp;
	private boolean canHitSelf;
	private long time;
	private long interval;
	private long cooldown;
	private double range;
	private double damage;
	private double speed;
	private double pushFactor;
	private double selectRange;
	private double deflectRange;
	private double collisionRadius;
	private byte sourceData;
	private Material sourceType;
	private Location location;
	private Location destination;
	private Location firstDestination;
	private Block sourceBlock;

	public EarthBlast(Player player) {
		super(player);
		
		this.isProgressing = false;
		this.isAtDestination = false;
		this.isSettingUp = true;
		this.deflectRange = getConfig().getDouble("Abilities.Earth.EarthBlast.DeflectRange");
		this.collisionRadius = getConfig().getDouble("Abilities.Earth.EarthBlast.CollisionRadius");
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthBlast.Cooldown");
		this.canHitSelf = getConfig().getBoolean("Abilities.Earth.EarthBlast.CanHitSelf");
		this.range = getConfig().getDouble("Abilities.Earth.EarthBlast.Range");
		this.damage = getConfig().getDouble("Abilities.Earth.EarthBlast.Damage");
		this.speed = getConfig().getDouble("Abilities.Earth.EarthBlast.Speed");
		this.pushFactor = getConfig().getDouble("Abilities.Earth.EarthBlast.Push");
		this.selectRange = getConfig().getDouble("Abilities.Earth.EarthBlast.SelectRange");		
		this.time = System.currentTimeMillis();
		this.interval = (long) (1000.0 / speed);
		
		if (prepare()) {
			start();
			time = System.currentTimeMillis();
		}
	}

	private void checkForCollision() {
		for (EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.player.equals(player)) {
				continue;
			} else if (!blast.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (!blast.isProgressing) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(this, blast.location)) {
				continue;
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			if (mloc.distanceSquared(location) <= range * range 
					&& GeneralMethods.getDistanceFromLine(vector, location, blast.location) < deflectRange
					&& mloc.distanceSquared(location.clone().add(vector)) < mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				blast.remove();
				remove();
				return;
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void focusBlock() {
		if (EarthPassive.isPassiveSand(sourceBlock)) {
			EarthPassive.revertSand(sourceBlock);
		}
		
		sourceData = sourceBlock.getData();
		if (sourceBlock.getType() == Material.SAND) {
			sourceType = Material.SAND;
			if (sourceBlock.getData() == (byte) 0x1) {
				sourceBlock.setType(Material.RED_SANDSTONE);
			} else {
				sourceBlock.setType(Material.SANDSTONE);
			} 
		} else if (sourceBlock.getType() == Material.STEP) {
			sourceBlock.setType(Material.STEP);
			sourceType = Material.STEP;
		} else if (sourceBlock.getType() == Material.STONE) {
			sourceBlock.setType(Material.COBBLESTONE);
			sourceType = Material.STONE;
		} else {
			sourceType = sourceBlock.getType();
			sourceBlock.setType(Material.STONE);
		}

		location = sourceBlock.getLocation();
	}

	private Location getTargetLocation() {
		Entity target = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
		Location location;
		
		if (target == null) {
			location = GeneralMethods.getTargetedLocation(player, range);
		} else {
			location = ((LivingEntity) target).getEyeLocation();
		}
		return location;
	}

	public boolean prepare() {
		Block block = BlockSource.getEarthSourceBlock(player, range, ClickType.SHIFT_DOWN);
		if (block == null || !isEarthbendable(block)) {
			return false;
		}
		
		boolean selectedABlockInUse = false;
		for (EarthBlast blast : getAbilities(player, EarthBlast.class)) {
			if (!blast.isProgressing) {
				blast.remove();
			} else if (blast.isProgressing && block.equals(blast.sourceBlock)) {
				selectedABlockInUse = true;
			}
		}
		
		if (selectedABlockInUse) {
			return false;
		}
		
		checkForCollision();
		if (block.getLocation().distanceSquared(player.getLocation()) > selectRange * selectRange) {
			return false;
		}
		sourceBlock = block;
		focusBlock();
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() - time >= interval) {
			time = System.currentTimeMillis();

			if (isAtDestination) {
				remove();
				return;
			} else if (!isEarthbendable(sourceBlock) && sourceBlock.getType() != Material.COBBLESTONE) {
				remove();
				return;
			}

			if (!isProgressing && !isAtDestination) {
				if (sourceBlock == null || !bPlayer.getBoundAbilityName().equals(getName())) {
					remove();
					return;
				} else if (!player.getWorld().equals(sourceBlock.getWorld())) {
					remove();
					return;
				} else if (sourceBlock.getLocation().distanceSquared(player.getLocation()) > selectRange * selectRange) {
					remove();
					return;
				}
			}

			if (isAtDestination) {
				remove();
				return;
			} else {
				if (!isProgressing) {
					return;
				}
				if (sourceBlock.getY() == firstDestination.getBlockY()) {
					isSettingUp = false;
				}

				Vector direction;
				if (isSettingUp) {
					direction = GeneralMethods.getDirection(location, firstDestination).normalize();
				} else {
					direction = GeneralMethods.getDirection(location, destination).normalize();
				}

				location = location.clone().add(direction);
				Block block = location.getBlock();	
				
				if (block.getLocation().equals(sourceBlock.getLocation())) {
					location = location.clone().add(direction);
					block = location.getBlock();
				}

				if (isTransparent(block) && !block.isLiquid()) {
					GeneralMethods.breakBlock(block);
				} else if (!isSettingUp) {
					remove();
					return;
				} else {
					location = location.clone().subtract(direction);
					direction = GeneralMethods.getDirection(location, destination).normalize();
					location = location.clone().add(direction);
					
					WaterAbility.removeWaterSpouts(location, player);
					AirAbility.removeAirSpouts(location, player);
					EarthAbility.removeSandSpouts(location, player);
					
					if (EarthBlast.annihilateBlasts(location, collisionRadius, player) 
							|| WaterManipulation.annihilateBlasts(location, collisionRadius, player)
							|| FireBlast.annihilateBlasts(location, collisionRadius, player)) {
						remove();
						return;
					}

					Combustion.removeAroundPoint(location, collisionRadius);

					Block block2 = location.getBlock();
					if (block2.getLocation().equals(sourceBlock.getLocation())) {
						location = location.clone().add(direction);
						block2 = location.getBlock();
					}

					if (isTransparent(block) && !block.isLiquid()) {
						GeneralMethods.breakBlock(block);
					} else {
						remove();
						return;
					}
				}

				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, collisionRadius)) {
					if (GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation())) {
						continue;
					}
					if (entity instanceof LivingEntity && (entity.getEntityId() != player.getEntityId() || canHitSelf)) {
						AirAbility.breakBreathbendingHold(entity);
						
						Location location = player.getEyeLocation();
						Vector vector = location.getDirection();
						entity.setVelocity(vector.normalize().multiply(pushFactor));
                        double damage = this.damage;
                        
						if (isMetal(sourceBlock) && bPlayer.canMetalbend()) {
							damage = getMetalAugment(damage);
						}
						DamageHandler.damageEntity(entity, damage, this);
						isProgressing = false;
					}
				}

				if (!isProgressing) {
					remove();
					return;
				}

				if (isEarthRevertOn()) {
					sourceBlock.setType(sourceType);
					sourceBlock.setData(sourceData);
					if (sourceBlock.getType() == Material.RED_SANDSTONE && sourceType == Material.SAND) {
						sourceBlock.setData((byte) 0x1);
					}

					moveEarthBlock(sourceBlock, block);
					
					if (block.getType() == Material.SAND) {
						block.setType(Material.SANDSTONE);
					}
					if (block.getType() == Material.GRAVEL) {
						block.setType(Material.STONE);
					}
				} else {
					block.setType(sourceType);
					sourceBlock.setType(Material.AIR);
				}

				sourceBlock = block;

				if (location.distanceSquared(destination) < 1) {
					if (sourceType == Material.SAND || sourceType == Material.GRAVEL) {
						isProgressing = false;
						if (sourceBlock.getType() == Material.RED_SANDSTONE) {
							sourceType = Material.SAND;
							sourceBlock.setType(sourceType);
							sourceBlock.setData((byte) 0x1);
						}
						else {
							sourceBlock.setType(sourceType);
						}
					}

					isAtDestination = true;
					isProgressing = false;
				}
				return;
			}
		}
	}

	private void redirect(Player player, Location targetlocation) {
		if (isProgressing) {
			if (location.distanceSquared(player.getLocation()) <= range * range) {
				isSettingUp = false;
				destination = targetlocation;
			}
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void remove() {
		super.remove();
		if(destination != null && sourceBlock != null) {
			sourceBlock.setType(Material.AIR);
		} else if (sourceBlock != null) {
			if (sourceBlock.getType() == Material.SAND) {
				if (sourceBlock.getData() == (byte) 0x1) {
					sourceBlock.setType(sourceType);
					sourceBlock.setData((byte) 0x1);
				} else {
					sourceBlock.setType(sourceType);
				}
			} else {
				sourceBlock.setType(sourceType);
				sourceBlock.setData(sourceData);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void throwEarth() {
		if (sourceBlock == null || !sourceBlock.getWorld().equals(player.getWorld())) {
			return;
		}
		
		if (getMovedEarth().containsKey(sourceBlock)) {
			if (!isEarthRevertOn()) {
				removeRevertIndex(sourceBlock);
			}
		}
		
		Entity target = GeneralMethods.getTargetedEntity(player, range, new ArrayList<Entity>());
		if (target == null) {
			destination = getTargetEarthBlock((int) range).getLocation();
			firstDestination = sourceBlock.getLocation().clone();
			firstDestination.setY(destination.getY());
		} else {
			destination = ((LivingEntity) target).getEyeLocation();
			firstDestination = sourceBlock.getLocation().clone();
			firstDestination.setY(destination.getY());
			destination = GeneralMethods.getPointOnLine(firstDestination, destination, range);
		}
		
		if (destination.distanceSquared(location) <= 1) {
			isProgressing = false;
			destination = null;
		} else {
			isProgressing = true;			
			playEarthbendingSound(sourceBlock.getLocation());

			Material currentType = sourceBlock.getType();
			sourceBlock.setType(sourceType);
			sourceBlock.setData(sourceData);
			if (isEarthRevertOn()) {
				addTempAirBlock(sourceBlock);
			} else {
				sourceBlock.breakNaturally();
			}			
			sourceBlock.setType(currentType);
		}
	}
	
	public static boolean annihilateBlasts(Location location, double radius, Player source) {
		boolean broke = false;
		for (EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.location.getWorld().equals(location.getWorld()) && !source.equals(blast.player)) {
				if (blast.location.distanceSquared(location) <= radius * radius) {
					blast.remove();
					broke = true;
				}
			}
		}
		return broke;
	}	

	public static ArrayList<EarthBlast> getAroundPoint(Location location, double radius) {
		ArrayList<EarthBlast> list = new ArrayList<EarthBlast>();
		for (EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distanceSquared(location) <= radius * radius) {
					list.add(blast);
				}
			}
		}
		return list;
	}

	public static EarthBlast getBlastFromSource(Block block) {
		for (EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.sourceBlock.equals(block)) {
				return blast;
			}
		}
		return null;
	}

	private static void redirectTargettedBlasts(Player player, ArrayList<EarthBlast> ignore) {
		for (EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (!blast.isProgressing || ignore.contains(blast)) {
				continue;
			} else if (!blast.location.getWorld().equals(player.getWorld())) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(blast, blast.location)) {
				continue;
			} else if (blast.player.equals(player)) {
				blast.redirect(player, blast.getTargetLocation());
			}

			Location location = player.getEyeLocation();
			Vector vector = location.getDirection();
			Location mloc = blast.location;
			
			if (mloc.distanceSquared(location) <= blast.range * blast.range 
					&& GeneralMethods.getDistanceFromLine(vector, location, blast.location) < blast.deflectRange
					&& mloc.distanceSquared(location.clone().add(vector)) 
						< mloc.distanceSquared(location.clone().add(vector.clone().multiply(-1)))) {
				blast.redirect(player, blast.getTargetLocation());
			}

		}
	}

	public static void removeAroundPoint(Location location, double radius) {
		for (EarthBlast blast : getAbilities(EarthBlast.class)) {
			if (blast.location.getWorld().equals(location.getWorld())) {
				if (blast.location.distanceSquared(location) <= radius * radius) {
					blast.remove();
				}
			}
		}
	}

	public static void throwEarth(Player player) {
		ArrayList<EarthBlast> ignore = new ArrayList<EarthBlast>();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		EarthBlast earthBlast = null;
		
		if (bPlayer == null) {
			return;
		}
		
		for (EarthBlast blast : getAbilities(player, EarthBlast.class)) {
			if (!blast.isProgressing && bPlayer.canBend(blast)) {
				blast.throwEarth();
				ignore.add(blast);
				earthBlast = blast;
			}
		}

		if (earthBlast != null) {
			bPlayer.addCooldown(earthBlast);
		}
		redirectTargettedBlasts(player, ignore);
	}

	@Override
	public String getName() {
		return "EarthBlast";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isProgressing() {
		return isProgressing;
	}

	public void setProgressing(boolean isProgressing) {
		this.isProgressing = isProgressing;
	}

	public boolean isAtDestination() {
		return isAtDestination;
	}

	public void setAtDestination(boolean isAtDestination) {
		this.isAtDestination = isAtDestination;
	}

	public boolean isSettingUp() {
		return isSettingUp;
	}

	public void setSettingUp(boolean isSettingUp) {
		this.isSettingUp = isSettingUp;
	}

	public boolean isCanHitSelf() {
		return canHitSelf;
	}

	public void setCanHitSelf(boolean canHitSelf) {
		this.canHitSelf = canHitSelf;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public double getSelectRange() {
		return selectRange;
	}

	public void setSelectRange(double selectRange) {
		this.selectRange = selectRange;
	}

	public double getDeflectRange() {
		return deflectRange;
	}

	public void setDeflectRange(double deflectRange) {
		this.deflectRange = deflectRange;
	}

	public double getCollisionRadius() {
		return collisionRadius;
	}

	public void setCollisionRadius(double collisionRadius) {
		this.collisionRadius = collisionRadius;
	}

	public Material getSourcetype() {
		return sourceType;
	}

	public void setSourcetype(Material sourcetype) {
		this.sourceType = sourcetype;
	}

	public Location getDestination() {
		return destination;
	}

	public void setDestination(Location destination) {
		this.destination = destination;
	}

	public Location getFirstDestination() {
		return firstDestination;
	}

	public void setFirstDestination(Location firstDestination) {
		this.firstDestination = firstDestination;
	}

	public Block getSourceBlock() {
		return sourceBlock;
	}

	public void setSourceBlock(Block sourceBlock) {
		this.sourceBlock = sourceBlock;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}