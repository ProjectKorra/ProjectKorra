package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.waterbending.WaterArms.Arm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WaterArmsSpear extends WaterAbility {

	private static final Map<Block, Long> ICE_BLOCKS = new ConcurrentHashMap<Block, Long>();
	
	private boolean hitEntity;
	private boolean canFreeze;
	private boolean usageCooldownEnabled;
	private boolean spearDamageEnabled;
	private int spearLength;
	private int spearRange;
	private int spearRangeNight;
	private int spearRangeFullMoon;
	private int spearSphere;
	private int spearSphereNight;
	private int spearSphereFullMoon;
	private int distanceTravelled;
	private int layer;
	private long spearDuration;
	private long spearDurationNight;
	private long spearDurationFullMoon;
	private long usageCooldown;
	private double spearDamage;
	private Arm arm;
	private Location location;
	private Location initLocation;
	private WaterArms waterArms;
	private List<Location> spearLocations;

	public WaterArmsSpear(Player player, boolean freeze) {
		super(player);
		this.canFreeze = freeze;
		
		this.usageCooldownEnabled = getConfig().getBoolean("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldownEnabled");
		this.spearDamageEnabled = getConfig().getBoolean("Abilities.Water.WaterArms.Spear.DamageEnabled");
		this.spearLength = getConfig().getInt("Abilities.Water.WaterArms.Spear.Length");
		this.spearRange = getConfig().getInt("Abilities.Water.WaterArms.Spear.Range");
		this.spearRangeNight = getConfig().getInt("Abilities.Water.WaterArms.Spear.NightAugments.Range.Normal");
		this.spearRangeFullMoon = getConfig().getInt("Abilities.Water.WaterArms.Spear.NightAugments.Range.FullMoon");
		this.spearSphere = getConfig().getInt("Abilities.Water.WaterArms.Spear.Sphere");
		this.spearSphereNight = getConfig().getInt("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.Normal");
		this.spearSphereFullMoon = getConfig().getInt("Abilities.Water.WaterArms.Spear.NightAugments.Sphere.FullMoon");
		this.spearDuration = getConfig().getLong("Abilities.Water.WaterArms.Spear.Duration");
		this.spearDurationNight = getConfig().getLong("Abilities.Water.WaterArms.Spear.NightAugments.Duration.Normal");
		this.spearDurationFullMoon = getConfig().getLong("Abilities.Water.WaterArms.Spear.NightAugments.Duration.FullMoon");
		this.usageCooldown = getConfig().getLong("Abilities.Water.WaterArms.Arms.Cooldowns.UsageCooldown");
		this.spearDamage = getConfig().getDouble("Abilities.Water.WaterArms.Spear.Damage");
		this.spearLocations = new ArrayList<>();
		
		getNightAugments();
		createInstance();
	}

	private void getNightAugments() {
		World world = player.getWorld();
		if (isNight(world)) {
			if (GeneralMethods.hasRPG()) {
				if (isLunarEclipse(world)) {
					spearRange = spearRangeFullMoon;
					spearSphere = spearSphereFullMoon;
					spearDuration = spearDurationFullMoon;
				} else if (isFullMoon(world)) {
					spearRange = spearRangeFullMoon;
					spearSphere = spearSphereFullMoon;
					spearDuration = spearDurationFullMoon;
				} else {
					spearRange = spearRangeNight;
					spearSphere = spearSphereNight;
					spearDuration = spearDurationNight;
				}
			} else {
				if (isFullMoon(world)) {
					spearRange = spearRangeFullMoon;
					spearSphere = spearSphereFullMoon;
					spearDuration = spearDurationFullMoon;
				} else {
					spearRange = spearRangeNight;
					spearSphere = spearSphereNight;
					spearDuration = spearDurationNight;
				}
			}
		}
	}

	private void createInstance() {
		waterArms = getAbility(player, WaterArms.class);
		if (waterArms != null) {
			waterArms.switchPreferredArm();
			arm = waterArms.getActiveArm();
			
			if (arm.equals(Arm.LEFT)) {
				if (waterArms.isLeftArmCooldown() || bPlayer.isOnCooldown("WaterArms_LEFT") || !waterArms.displayLeftArm()) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_LEFT", usageCooldown);
					}
					waterArms.setLeftArmConsumed(true);
					waterArms.setLeftArmCooldown(true);
				}
			}
			if (arm.equals(Arm.RIGHT)) {
				if (waterArms.isRightArmCooldown() || bPlayer.isOnCooldown("WaterArms_RIGHT") || !waterArms.displayRightArm()) {
					return;
				} else {
					if (usageCooldownEnabled) {
						bPlayer.addCooldown("WaterArms_RIGHT", usageCooldown);
					}
					waterArms.setRightArmConsumed(true);
					waterArms.setRightArmCooldown(true);
				}
			}
			Vector dir = player.getLocation().getDirection();
			location = waterArms.getActiveArmEnd().add(dir.normalize().multiply(1));
			initLocation = location.clone();
		} else {
			return;
		}
		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (distanceTravelled > spearRange) {
			remove();
			return;
		}
		
		if (!hitEntity) {
			progressSpear();
		} else {
			createIceBall();
			remove();
		}
		
		if (layer >= spearSphere) {
			remove();
			return;
		} else if (!canPlaceBlock(location.getBlock())) {
			if (canFreeze) {
				createSpear();
			}
			remove();
			return;
		}
	}

	private void progressSpear() {
		for (int i = 0; i < 2; i++) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand)) {
					hitEntity = true;
					location = entity.getLocation();

					if (spearDamageEnabled) {
						DamageHandler.damageEntity(entity, spearDamage, this);
					}

					return;
				}
			}
			
			new TempBlock(location.getBlock(), Material.STATIONARY_WATER, (byte) 8);
			getIceBlocks().put(location.getBlock(), System.currentTimeMillis() + 600L);
			Vector direction = GeneralMethods.getDirection(initLocation, GeneralMethods.getTargetedLocation(player, spearRange, new Integer[] { 8, 9, 79, 174 })).normalize();
			
			location = location.add(direction.clone().multiply(1));
			spearLocations.add(location.clone());
			
			if (!canPlaceBlock(location.getBlock())) {
				return;
			}
			distanceTravelled++;
		}
	}

	private void createSpear() {
		for (int i = spearLocations.size() - spearLength; i < spearLocations.size(); i++) {
			if (i >= 0) {
				Block block = spearLocations.get(i).getBlock();
				if (canPlaceBlock(block)) {
					playIcebendingSound(block.getLocation());
					if (getIceBlocks().containsKey(block)) {
						getIceBlocks().remove(block);
					}
					
					TempBlock tempBlock = new TempBlock(block, Material.AIR, (byte) 0);
					tempBlock.setType(Material.ICE);
					
					getIceBlocks().put(block, System.currentTimeMillis() + spearDuration + (long) (Math.random() * 500));
				}
			}
		}
	}
	
	public static boolean canThaw(Block block) {
		return getIceBlocks().containsKey(block) && block.getType() == Material.ICE;
	}
	
	public static void thaw(Block block) {
		if (canThaw(block)) {
			getIceBlocks().remove(block);
			block.setType(Material.AIR);
		}
	}

	private void createIceBall() {
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, spearSphere)) {
			if (isTransparent(player, block) && block.getType() != Material.ICE && !WaterArms.isUnbreakable(block)) {
				playIcebendingSound(block.getLocation());
				new TempBlock(block, Material.ICE, (byte) 0);
				getIceBlocks().put(block, System.currentTimeMillis() + spearDuration + (long) (Math.random() * 500));
			}
		}
	}

	private boolean canPlaceBlock(Block block) {
		if (!isTransparent(player, block) 
				&& !((isWater(block) || isIcebendable(block)) && (TempBlock.isTempBlock(block) && !getIceBlocks().containsKey(block)))) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return false;
		} else if (WaterArms.isUnbreakable(block) && !isWater(block)) {
			return false;
		}
		return true;
	}

	@Override
	public void remove() {
		super.remove();
		if (hasAbility(player, WaterArms.class)) {
			if (arm.equals(Arm.LEFT)) {
				waterArms.setLeftArmCooldown(false);
			} else {
				waterArms.setRightArmCooldown(false);
			}
			waterArms.setMaxUses(waterArms.getMaxUses() - 1);
		}
	}

	@Override
	public String getName() {
		return "WaterArms";
	}

	@Override
	public Location getLocation() {
		if (location != null) {
			return location;
		} else {
			return initLocation;
		}
	}

	@Override
	public long getCooldown() {
		return usageCooldown;
	}
	
	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	public boolean isHitEntity() {
		return hitEntity;
	}

	public void setHitEntity(boolean hitEntity) {
		this.hitEntity = hitEntity;
	}

	public boolean isCanFreeze() {
		return canFreeze;
	}

	public void setCanFreeze(boolean canFreeze) {
		this.canFreeze = canFreeze;
	}

	public boolean isUsageCooldownEnabled() {
		return usageCooldownEnabled;
	}

	public void setUsageCooldownEnabled(boolean usageCooldownEnabled) {
		this.usageCooldownEnabled = usageCooldownEnabled;
	}

	public boolean isSpearDamageEnabled() {
		return spearDamageEnabled;
	}

	public void setSpearDamageEnabled(boolean spearDamageEnabled) {
		this.spearDamageEnabled = spearDamageEnabled;
	}

	public int getSpearLength() {
		return spearLength;
	}

	public void setSpearLength(int spearLength) {
		this.spearLength = spearLength;
	}

	public int getSpearRange() {
		return spearRange;
	}

	public void setSpearRange(int spearRange) {
		this.spearRange = spearRange;
	}

	public int getSpearRangeNight() {
		return spearRangeNight;
	}

	public void setSpearRangeNight(int spearRangeNight) {
		this.spearRangeNight = spearRangeNight;
	}

	public int getSpearRangeFullMoon() {
		return spearRangeFullMoon;
	}

	public void setSpearRangeFullMoon(int spearRangeFullMoon) {
		this.spearRangeFullMoon = spearRangeFullMoon;
	}

	public int getSpearSphere() {
		return spearSphere;
	}

	public void setSpearSphere(int spearSphere) {
		this.spearSphere = spearSphere;
	}

	public int getSpearSphereNight() {
		return spearSphereNight;
	}

	public void setSpearSphereNight(int spearSphereNight) {
		this.spearSphereNight = spearSphereNight;
	}

	public int getSpearSphereFullMoon() {
		return spearSphereFullMoon;
	}

	public void setSpearSphereFullMoon(int spearSphereFullMoon) {
		this.spearSphereFullMoon = spearSphereFullMoon;
	}

	public int getDistanceTravelled() {
		return distanceTravelled;
	}

	public void setDistanceTravelled(int distanceTravelled) {
		this.distanceTravelled = distanceTravelled;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public long getSpearDuration() {
		return spearDuration;
	}

	public void setSpearDuration(long spearDuration) {
		this.spearDuration = spearDuration;
	}

	public long getSpearDurationNight() {
		return spearDurationNight;
	}

	public void setSpearDurationNight(long spearDurationNight) {
		this.spearDurationNight = spearDurationNight;
	}

	public long getSpearDurationFullMoon() {
		return spearDurationFullMoon;
	}

	public void setSpearDurationFullMoon(long spearDurationFullMoon) {
		this.spearDurationFullMoon = spearDurationFullMoon;
	}

	public long getUsageCooldown() {
		return usageCooldown;
	}

	public void setUsageCooldown(long usageCooldown) {
		this.usageCooldown = usageCooldown;
	}

	public double getSpearDamage() {
		return spearDamage;
	}

	public void setSpearDamage(double spearDamage) {
		this.spearDamage = spearDamage;
	}

	public Arm getArm() {
		return arm;
	}

	public void setArm(Arm arm) {
		this.arm = arm;
	}

	public Location getInitLocation() {
		return initLocation;
	}

	public void setInitLocation(Location initLocation) {
		this.initLocation = initLocation;
	}

	public WaterArms getWaterArms() {
		return waterArms;
	}

	public void setWaterArms(WaterArms waterArms) {
		this.waterArms = waterArms;
	}

	public List<Location> getSpearLocations() {
		return spearLocations;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public static Map<Block, Long> getIceBlocks() {
		return ICE_BLOCKS;
	}
	
}
